package com.maisha.game.ui.charactercreation

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maisha.game.R
import com.maisha.game.data.CountryCatalog
import com.maisha.game.data.NamePool
import com.maisha.game.data.local.CharacterRepository
import com.maisha.game.data.local.MAX_SLOTS
import com.maisha.game.data.local.MetaBonusRepository
import com.maisha.game.data.model.AvatarConfig
import com.maisha.game.data.model.Character
import com.maisha.game.data.model.Gender
import com.maisha.game.data.model.Stats
import com.maisha.game.data.remote.RandomNameService
import com.maisha.game.domain.FamilyGenerator
import com.maisha.game.domain.StartingStatArchetype
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject
import kotlin.random.Random

data class CharacterCreationUiState(
    val name: String = "",
    val hasUserEditedName: Boolean = false,
    val selectedGender: Gender = Gender.MALE,
    val selectedCountryCode: String = "US",
    val countrySearchQuery: String = "",
    val avatarConfig: AvatarConfig = AvatarConfig.random().copy(facialHair = null, eyewear = null),
    val selectedArchetype: StartingStatArchetype? = StartingStatArchetype.BALANCED,
    val selectedStats: Stats = StartingStatArchetype.BALANCED.buildInitialStats(),
    val nameError: String? = null,
    val isSaving: Boolean = false,
    val navigateToAvatarPicker: Boolean = false,
    val navigateToStatsPicker: Boolean = false,
    val navigateToLife: Boolean = false,
    val secondWindBonusLabel: String? = null
)

@HiltViewModel
class CharacterCreationViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    savedStateHandle: SavedStateHandle,
    private val characterRepository: CharacterRepository,
    private val familyGenerator: FamilyGenerator,
    private val metaBonusRepository: MetaBonusRepository,
    private val randomNameService: RandomNameService
) : ViewModel() {

    private val slotId: Int = savedStateHandle.get<Int>("slotId")
        ?.takeIf { it in 0 until MAX_SLOTS }
        ?: 0

    private val _uiState = MutableStateFlow(
        run {
            val defaultCountry = resolveDefaultCountryCode()
            val defaultGender = if (Random.nextBoolean()) Gender.MALE else Gender.FEMALE
            val defaultName = NamePool.randomFullName(defaultGender, defaultCountry)
            val defaultAvatar = AvatarConfig.random().copy(facialHair = null, eyewear = null)
            val defaultArchetype = StartingStatArchetype.BALANCED
            val defaultStats = defaultArchetype.buildInitialStats()
            CharacterCreationUiState(
                name = defaultName,
                hasUserEditedName = false,
                selectedGender = defaultGender,
                selectedCountryCode = defaultCountry,
                avatarConfig = defaultAvatar,
                selectedArchetype = defaultArchetype,
                selectedStats = defaultStats
            )
        }
    )
    val uiState: StateFlow<CharacterCreationUiState> = _uiState.asStateFlow()

    private var randomNameGeneration = 0

    init {
        viewModelScope.launch {
            val pendingBonus = metaBonusRepository.peekSecondWindBonus()
            if (pendingBonus != null) {
                _uiState.update {
                    it.copy(secondWindBonusLabel = secondWindBonusLabel(pendingBonus))
                }
            }
        }
    }

    /**
     * Prefer the device locale country when it is in the 15-country roster;
     * otherwise pick a random roster country so Kenya is not always the default.
     */
    private fun resolveDefaultCountryCode(): String {
        val rosterCodes = CountryCatalog.all().map { it.code }
        val deviceCountry = Locale.getDefault().country.uppercase(Locale.US)
        if (deviceCountry in rosterCodes) return deviceCountry
        return rosterCodes.random()
    }

    fun filteredCountries() = CountryCatalog.search(_uiState.value.countrySearchQuery)

    fun onNameChange(name: String) {
        val trimmed = name.take(MAX_NAME_LENGTH)
        _uiState.update { it.copy(name = trimmed, hasUserEditedName = true, nameError = null) }
    }

    fun onGenderSelected(gender: Gender) {
        _uiState.update { state ->
            val newName = if (!state.hasUserEditedName) {
                NamePool.randomFullName(gender, state.selectedCountryCode)
            } else {
                state.name
            }
            state.copy(selectedGender = gender, name = newName)
        }
    }

    fun onCountrySelected(countryCode: String) {
        _uiState.update { state ->
            val newName = if (!state.hasUserEditedName) {
                NamePool.randomFullName(state.selectedGender, countryCode)
            } else {
                state.name
            }
            state.copy(selectedCountryCode = countryCode, name = newName, countrySearchQuery = "")
        }
    }

    fun onCountrySearchChange(query: String) {
        _uiState.update { it.copy(countrySearchQuery = query) }
    }

    fun onAvatarChange(config: AvatarConfig) {
        _uiState.update { it.copy(avatarConfig = config.copy(facialHair = null, eyewear = null)) }
    }

    fun onCycleAvatar() {
        _uiState.update { state ->
            val cur = state.avatarConfig
            val nextSkin = (cur.skinTone + 1) % AvatarConfig.SKIN_TONE_COUNT
            val nextHair = (cur.hairStyle + 1) % AvatarConfig.HAIR_STYLE_COUNT
            val nextColor = (cur.hairColor + 1) % AvatarConfig.HAIR_COLOR_COUNT
            state.copy(
                avatarConfig = cur.copy(
                    skinTone = nextSkin,
                    hairStyle = nextHair,
                    hairColor = nextColor,
                    facialHair = null,
                    eyewear = null
                )
            )
        }
    }

    fun onRandomName() {
        val state = _uiState.value
        val fallbackName = NamePool.randomFullName(state.selectedGender, state.selectedCountryCode)
        _uiState.update { it.copy(name = fallbackName, hasUserEditedName = false, nameError = null) }

        val requestId = ++randomNameGeneration
        viewModelScope.launch {
            val generated = randomNameService.fetchName(
                gender = state.selectedGender,
                countryCode = state.selectedCountryCode
            )
            if (requestId != randomNameGeneration) return@launch
            _uiState.update {
                it.copy(
                    name = generated.fullName.take(MAX_NAME_LENGTH),
                    hasUserEditedName = false,
                    nameError = null
                )
            }
        }
    }

    fun onRandomizeLife() {
        val country = CountryCatalog.all().random().code
        val gender = if (Random.nextBoolean()) Gender.MALE else Gender.FEMALE
        val name = NamePool.randomFullName(gender, country)
        val avatar = AvatarConfig.random().copy(facialHair = null, eyewear = null)
        val archetype = StartingStatArchetype.values().random()
        val stats = archetype.buildInitialStats()
        _uiState.update {
            it.copy(
                selectedCountryCode = country,
                selectedGender = gender,
                name = name,
                hasUserEditedName = false,
                avatarConfig = avatar,
                selectedArchetype = archetype,
                selectedStats = stats,
                countrySearchQuery = "",
                nameError = null
            )
        }
    }

    fun onArchetypeSelected(archetype: StartingStatArchetype) {
        _uiState.update {
            it.copy(
                selectedArchetype = archetype,
                selectedStats = archetype.buildInitialStats()
            )
        }
    }

    fun onContinueToAvatarPicker() {
        val trimmedName = _uiState.value.name.trim()
        if (trimmedName.isEmpty()) {
            _uiState.update { it.copy(nameError = context.getString(R.string.error_name_required)) }
            return
        }
        _uiState.update { it.copy(navigateToAvatarPicker = true) }
    }

    fun onAvatarPickerNavigationHandled() {
        _uiState.update { it.copy(navigateToAvatarPicker = false) }
    }

    fun onContinueToStatsPicker() {
        _uiState.update { it.copy(navigateToStatsPicker = true) }
    }

    fun onStatsPickerNavigationHandled() {
        _uiState.update { it.copy(navigateToStatsPicker = false) }
    }

    fun onStatsChange(stats: Stats) {
        _uiState.update {
            it.copy(
                selectedArchetype = null,
                selectedStats = stats.copy(
                    // Keep these fixed for new life creation.
                    money = 0,
                    karma = 50
                )
            )
        }
    }

    fun onStartLife() {
        val state = _uiState.value
        val trimmedName = state.name.trim().ifEmpty {
            NamePool.randomFullName(state.selectedGender, state.selectedCountryCode)
        }
        if (slotId !in 0 until MAX_SLOTS) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            val bonusStat = metaBonusRepository.consumeSecondWindBonus()
            val baseStats = state.selectedStats
            val stats = applySecondWindBonus(baseStats, bonusStat)
            val countryName = CountryCatalog.getCountry(state.selectedCountryCode).displayName
            val birthLog = context.getString(R.string.log_character_born, countryName)
            val character = Character(
                name = trimmedName,
                age = 0,
                gender = state.selectedGender,
                stats = stats,
                birthYear = Calendar.getInstance().get(Calendar.YEAR),
                alive = true,
                countryCode = state.selectedCountryCode,
                birthCountryCode = state.selectedCountryCode,
                citizenships = listOf(state.selectedCountryCode),
                avatarConfig = state.avatarConfig,
                eventLog = listOf("── Age 0 ──", birthLog),
                family = familyGenerator.generateFamily(
                    characterAge = 0,
                    countryCode = state.selectedCountryCode
                )
            )
            characterRepository.saveGame(slotId, character, triggeredEventIds = emptySet())
            _uiState.update {
                it.copy(
                    isSaving = false,
                    navigateToLife = true,
                    secondWindBonusLabel = null
                )
            }
        }
    }

    fun onNavigationHandled() {
        _uiState.update { it.copy(navigateToLife = false) }
    }

    private fun applySecondWindBonus(stats: Stats, bonusStat: String?): Stats {
        if (bonusStat == null) return stats
        val amount = MetaBonusRepository.SECOND_WIND_BONUS_AMOUNT
        return when (bonusStat) {
            MetaBonusRepository.STAT_HEALTH -> stats.copy(health = (stats.health + amount).coerceAtMost(100))
            MetaBonusRepository.STAT_HAPPINESS -> stats.copy(happiness = (stats.happiness + amount).coerceAtMost(100))
            MetaBonusRepository.STAT_SMARTS -> stats.copy(smarts = (stats.smarts + amount).coerceAtMost(100))
            MetaBonusRepository.STAT_LOOKS -> stats.copy(looks = (stats.looks + amount).coerceAtMost(100))
            else -> stats
        }
    }

    private fun secondWindBonusLabel(statKey: String): String = when (statKey) {
        MetaBonusRepository.STAT_HEALTH -> context.getString(R.string.second_wind_bonus_health)
        MetaBonusRepository.STAT_HAPPINESS -> context.getString(R.string.second_wind_bonus_happiness)
        MetaBonusRepository.STAT_SMARTS -> context.getString(R.string.second_wind_bonus_smarts)
        MetaBonusRepository.STAT_LOOKS -> context.getString(R.string.second_wind_bonus_looks)
        else -> context.getString(R.string.second_wind_bonus_random)
    }

    companion object {
        const val MAX_NAME_LENGTH = 40
    }
}
