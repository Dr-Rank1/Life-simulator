package com.maisha.game.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.maisha.game.R
import com.maisha.game.data.model.AssetType
import com.maisha.game.data.model.Character
import com.maisha.game.domain.LeisureActivity
import com.maisha.game.domain.LeisureEngine
import com.maisha.game.ui.theme.AppIcons
import com.maisha.game.ui.theme.BorderCardSlate
import com.maisha.game.ui.theme.CoralNegative
import com.maisha.game.ui.theme.CreamBg
import com.maisha.game.ui.theme.GoldAccent
import com.maisha.game.ui.theme.InkPrimary
import com.maisha.game.ui.theme.InkSecondary
import com.maisha.game.ui.theme.InkTertiary
import com.maisha.game.ui.theme.LifeGreen
import com.maisha.game.ui.theme.PaperWhite
import com.maisha.game.ui.theme.SuccessGreen
import com.maisha.game.ui.theme.TealPrimary
import com.maisha.game.util.formatMoney

private enum class LeisureCategory {
    VACATIONS,
    HOBBIES,
    WELLNESS,
    LUXURY
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LeisureLifestyleBoard(
    character: Character,
    onPerformLeisure: (LeisureActivity) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val leisureEngine = remember { LeisureEngine() }
    var selectedCategory by remember { mutableStateOf(LeisureCategory.VACATIONS) }
    var activityToConfirm by remember { mutableStateOf<LeisureActivity?>(null) }

    val categoryActivities = remember(selectedCategory, character.age, character.assets) {
        when (selectedCategory) {
            LeisureCategory.VACATIONS -> listOf(
                LeisureActivity.WEEKEND_RETREAT,
                LeisureActivity.NATURE_DAY,
                LeisureActivity.CITY_SHOW,
                LeisureActivity.NIGHT_OUT,
                LeisureActivity.PARK_CAREGIVER,
                LeisureActivity.PLAYGROUND,
                LeisureActivity.STORYTIME
            )
            LeisureCategory.HOBBIES -> listOf(
                LeisureActivity.VISIT_MUSEUM,
                LeisureActivity.LIBRARY_VISIT,
                LeisureActivity.MEMOIR_WRITING,
                LeisureActivity.CHILDHOOD_HOBBY,
                LeisureActivity.STUDY_BUDDY,
                LeisureActivity.PLAY_WITH_TOYS,
                LeisureActivity.GAMING_MARATHON,
                LeisureActivity.MALL_HANGOUT,
                LeisureActivity.SNEAK_OUT,
                LeisureActivity.CHORES
            )
            LeisureCategory.WELLNESS -> listOf(
                LeisureActivity.MEDITATION,
                LeisureActivity.SPA_DAY,
                LeisureActivity.ANNUAL_CHECKUP,
                LeisureActivity.REUNION_DINNER,
                LeisureActivity.COMMUNITY_CLUB,
                LeisureActivity.GRANDKIDS_DAY,
                LeisureActivity.NAP_ROUTINE,
                LeisureActivity.PLAYDATE,
                LeisureActivity.THROW_TANTRUM,
                LeisureActivity.ASK_ALLOWANCE
            )
            LeisureCategory.LUXURY -> listOf(
                LeisureActivity.YACHT_DAY,
                LeisureActivity.PRIVATE_JET_TRIP
            )
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Leisure Board Header
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = PaperWhite),
                border = BorderStroke(1.dp, BorderCardSlate),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(GoldAccent.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = AppIcons.Travel,
                                contentDescription = null,
                                tint = GoldAccent,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Column {
                            Text(
                                text = stringResource(R.string.label_leisure_hub_title),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = InkPrimary
                            )
                            Text(
                                text = stringResource(R.string.label_leisure_hub_desc),
                                style = MaterialTheme.typography.bodySmall,
                                color = InkSecondary
                            )
                        }
                    }
                }
            }
        }

        // Category Filter Chips
        item {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedCategory == LeisureCategory.VACATIONS,
                    onClick = { selectedCategory = LeisureCategory.VACATIONS },
                    label = { Text(stringResource(R.string.tab_leisure_vacations)) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = TealPrimary.copy(alpha = 0.15f),
                        selectedLabelColor = TealPrimary
                    )
                )
                FilterChip(
                    selected = selectedCategory == LeisureCategory.HOBBIES,
                    onClick = { selectedCategory = LeisureCategory.HOBBIES },
                    label = { Text(stringResource(R.string.tab_leisure_hobbies)) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = TealPrimary.copy(alpha = 0.15f),
                        selectedLabelColor = TealPrimary
                    )
                )
                FilterChip(
                    selected = selectedCategory == LeisureCategory.WELLNESS,
                    onClick = { selectedCategory = LeisureCategory.WELLNESS },
                    label = { Text(stringResource(R.string.tab_leisure_wellness)) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = TealPrimary.copy(alpha = 0.15f),
                        selectedLabelColor = TealPrimary
                    )
                )
                FilterChip(
                    selected = selectedCategory == LeisureCategory.LUXURY,
                    onClick = { selectedCategory = LeisureCategory.LUXURY },
                    label = { Text(stringResource(R.string.tab_leisure_luxury)) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = GoldAccent.copy(alpha = 0.15f),
                        selectedLabelColor = GoldAccent
                    )
                )
            }
        }

        // Activity Cards
        items(categoryActivities, key = { it.name }) { activity ->
            val eligible = leisureEngine.isEligible(character, activity)
            val cost = leisureEngine.cost(activity, character.countryCode)
            val canAfford = cost == 0 || character.stats.money >= cost
            val hasRequiredAsset = when (activity) {
                LeisureActivity.YACHT_DAY -> character.assets.any { it.type == AssetType.BOAT }
                LeisureActivity.PRIVATE_JET_TRIP -> character.assets.any { it.type == AssetType.PRIVATE_JET }
                else -> true
            }

            LeisureActivityCard(
                activity = activity,
                cost = cost,
                countryCode = character.countryCode,
                eligible = eligible,
                canAfford = canAfford,
                hasRequiredAsset = hasRequiredAsset,
                onSelect = { activityToConfirm = activity }
            )
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // Confirmation Dialog
    activityToConfirm?.let { activity ->
        val cost = leisureEngine.cost(activity, character.countryCode)
        val canAfford = cost == 0 || character.stats.money >= cost
        val title = activityDisplayName(activity)
        val description = activityDescription(activity)

        Dialog(onDismissRequest = { activityToConfirm = null }) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = PaperWhite,
                border = BorderStroke(1.dp, BorderCardSlate),
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "Enjoy $title?",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = InkPrimary
                    )
                    Text(
                        text = description + if (cost > 0) " Cost: ${formatMoney(cost, character.countryCode)}." else " Free activity.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = InkSecondary
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = { activityToConfirm = null },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(stringResource(R.string.btn_cancel))
                        }
                        Button(
                            onClick = {
                                activityToConfirm = null
                                onPerformLeisure(activity)
                            },
                            enabled = canAfford,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
                        ) {
                            Text(
                                text = if (canAfford) "Participate" else "Insufficient Funds",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun LeisureActivityCard(
    activity: LeisureActivity,
    cost: Int,
    countryCode: String,
    eligible: Boolean,
    canAfford: Boolean,
    hasRequiredAsset: Boolean,
    onSelect: () -> Unit
) {
    val enabled = eligible && canAfford && hasRequiredAsset
    val icon = activityIcon(activity)
    val payoffs = activityPayoffs(activity)

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (enabled) PaperWhite else CreamBg.copy(alpha = 0.6f)
        ),
        border = BorderStroke(1.dp, if (enabled) BorderCardSlate else BorderCardSlate.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(
                            if (enabled) TealPrimary.copy(alpha = 0.12f) else InkTertiary.copy(alpha = 0.1f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (enabled) TealPrimary else InkTertiary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = activityDisplayName(activity),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (enabled) InkPrimary else InkSecondary
                    )
                    Text(
                        text = activityDescription(activity),
                        style = MaterialTheme.typography.bodySmall,
                        color = InkSecondary,
                        maxLines = 1
                    )
                }

                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = if (cost > 0) formatMoney(cost, countryCode) else stringResource(R.string.label_activity_free),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (canAfford) TealPrimary else CoralNegative
                    )

                    Button(
                        onClick = onSelect,
                        enabled = enabled,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Enjoy",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Badges row: Payoffs + Requirements
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                payoffs.forEach { (label, isPositive) ->
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (isPositive) SuccessGreen.copy(alpha = 0.12f) else CoralNegative.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isPositive) SuccessGreen else CoralNegative,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                        )
                    }
                }

                if (!hasRequiredAsset) {
                    val assetName = if (activity == LeisureActivity.YACHT_DAY) {
                        stringResource(R.string.badge_asset_boat)
                    } else {
                        stringResource(R.string.badge_asset_jet)
                    }
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = CoralNegative.copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, CoralNegative.copy(alpha = 0.35f))
                    ) {
                        Text(
                            text = stringResource(R.string.badge_requires_asset, assetName),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = CoralNegative,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                        )
                    }
                } else if (!eligible) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = InkTertiary.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = "Age Ineligible",
                            style = MaterialTheme.typography.labelSmall,
                            color = InkTertiary,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                        )
                    }
                }
            }
        }
    }
}

private fun activityDisplayName(activity: LeisureActivity): String = when (activity) {
    LeisureActivity.WEEKEND_RETREAT -> "Weekend Retreat"
    LeisureActivity.NATURE_DAY -> "Day in Nature"
    LeisureActivity.CITY_SHOW -> "City Theatre & Show"
    LeisureActivity.NIGHT_OUT -> "Night Out"
    LeisureActivity.PARK_CAREGIVER -> "Caregiver Park Outing"
    LeisureActivity.PLAYGROUND -> "Playground Run"
    LeisureActivity.STORYTIME -> "Bedtime Storytime"
    LeisureActivity.VISIT_MUSEUM -> "High Museum Visit"
    LeisureActivity.LIBRARY_VISIT -> "Library Book Reading"
    LeisureActivity.MEMOIR_WRITING -> "Memoir Writing"
    LeisureActivity.CHILDHOOD_HOBBY -> "Youth Hobby & Passion"
    LeisureActivity.STUDY_BUDDY -> "Study Buddy Session"
    LeisureActivity.PLAY_WITH_TOYS -> "Play with Toys"
    LeisureActivity.GAMING_MARATHON -> "Gaming Marathon"
    LeisureActivity.MALL_HANGOUT -> "Mall Hangout"
    LeisureActivity.SNEAK_OUT -> "Sneak Out After Hours"
    LeisureActivity.CHORES -> "Household Chores"
    LeisureActivity.MEDITATION -> "Mindful Meditation"
    LeisureActivity.SPA_DAY -> "Spa & Thermal Bath"
    LeisureActivity.ANNUAL_CHECKUP -> "Annual Health Checkup"
    LeisureActivity.REUNION_DINNER -> "Old Friends Reunion"
    LeisureActivity.COMMUNITY_CLUB -> "Community Club Meet"
    LeisureActivity.GRANDKIDS_DAY -> "Day with Grandchildren"
    LeisureActivity.NAP_ROUTINE -> "Recharge Nap Routine"
    LeisureActivity.PLAYDATE -> "Social Playdate"
    LeisureActivity.THROW_TANTRUM -> "Toddler Tantrum"
    LeisureActivity.ASK_ALLOWANCE -> "Ask for Allowance"
    LeisureActivity.YACHT_DAY -> "Yacht Cruise Day"
    LeisureActivity.PRIVATE_JET_TRIP -> "Private Jet Getaway"
}

private fun activityDescription(activity: LeisureActivity): String = when (activity) {
    LeisureActivity.WEEKEND_RETREAT -> "Escape the city routine for a tranquil wellness retreat."
    LeisureActivity.NATURE_DAY -> "Hike through forests and breathe in fresh outdoor air."
    LeisureActivity.CITY_SHOW -> "Attend live theater, concerts, or stand-up comedy."
    LeisureActivity.NIGHT_OUT -> "Hit late-night clubs and restaurants with friends."
    LeisureActivity.PARK_CAREGIVER -> "Fresh air and stroller strolls in the city park."
    LeisureActivity.PLAYGROUND -> "Climb jungle gyms and run outdoors with neighborhood friends."
    LeisureActivity.STORYTIME -> "Curl up and listen to fairy tales and folklore."
    LeisureActivity.VISIT_MUSEUM -> "Admire historical artifacts and collect fine art prints."
    LeisureActivity.LIBRARY_VISIT -> "Quietly read renowned literature and expand your mind."
    LeisureActivity.MEMOIR_WRITING -> "Document your rich journey and wisdom for future heirs."
    LeisureActivity.CHILDHOOD_HOBBY -> "Practice instruments, paint canvases, or hone athletic skills."
    LeisureActivity.STUDY_BUDDY -> "Collaborate on homework and exam prep with peers."
    LeisureActivity.PLAY_WITH_TOYS -> "Spark your imagination with toy sets and figurines."
    LeisureActivity.GAMING_MARATHON -> "Hours of fast-paced console and computer gaming."
    LeisureActivity.MALL_HANGOUT -> "Stroll shopping corridors and drink bubble tea with classmates."
    LeisureActivity.SNEAK_OUT -> "Slip past curfew into the night. Watch out for parents!"
    LeisureActivity.CHORES -> "Wash dishes, sweep, and earn a tidy pocket tip."
    LeisureActivity.MEDITATION -> "Center your thoughts and breathe away tension for free."
    LeisureActivity.SPA_DAY -> "Deep-tissue massages, hot springs, and facial care."
    LeisureActivity.ANNUAL_CHECKUP -> "Thorough preventative doctor checkup and health maintenance."
    LeisureActivity.REUNION_DINNER -> "Toast old times and renew deep lifelong friendships."
    LeisureActivity.COMMUNITY_CLUB -> "Play cards, discuss local politics, and share coffee."
    LeisureActivity.GRANDKIDS_DAY -> "Spoil the next generation with sweet treats and stories."
    LeisureActivity.NAP_ROUTINE -> "Restore mental focus and energy with quiet rest."
    LeisureActivity.PLAYDATE -> "Share building blocks and games with a toddler peer."
    LeisureActivity.THROW_TANTRUM -> "Wail and stomp your feet when things do not go your way."
    LeisureActivity.ASK_ALLOWANCE -> "Politely ask your parents for extra spending cash."
    LeisureActivity.YACHT_DAY -> "Sail your personal luxury vessel on sparkling waters."
    LeisureActivity.PRIVATE_JET_TRIP -> "Fly high above the clouds in ultimate luxury comfort."
}

private fun activityIcon(activity: LeisureActivity): ImageVector = when (activity) {
    LeisureActivity.WEEKEND_RETREAT,
    LeisureActivity.NATURE_DAY,
    LeisureActivity.CITY_SHOW -> AppIcons.Travel
    LeisureActivity.NIGHT_OUT -> AppIcons.SpendTime
    LeisureActivity.PARK_CAREGIVER,
    LeisureActivity.PLAYGROUND,
    LeisureActivity.PLAY_WITH_TOYS,
    LeisureActivity.PLAYDATE -> AppIcons.Family
    LeisureActivity.STORYTIME,
    LeisureActivity.LIBRARY_VISIT,
    LeisureActivity.STUDY_BUDDY,
    LeisureActivity.MEMOIR_WRITING -> AppIcons.Smarts
    LeisureActivity.VISIT_MUSEUM -> AppIcons.Looks
    LeisureActivity.CHILDHOOD_HOBBY,
    LeisureActivity.GAMING_MARATHON,
    LeisureActivity.MALL_HANGOUT -> AppIcons.Happiness
    LeisureActivity.SNEAK_OUT,
    LeisureActivity.THROW_TANTRUM -> AppIcons.Mischief
    LeisureActivity.CHORES,
    LeisureActivity.ASK_ALLOWANCE -> AppIcons.Money
    LeisureActivity.MEDITATION,
    LeisureActivity.SPA_DAY,
    LeisureActivity.NAP_ROUTINE -> AppIcons.Peaceful
    LeisureActivity.ANNUAL_CHECKUP -> AppIcons.Health
    LeisureActivity.REUNION_DINNER,
    LeisureActivity.COMMUNITY_CLUB,
    LeisureActivity.GRANDKIDS_DAY -> AppIcons.Relationship
    LeisureActivity.YACHT_DAY,
    LeisureActivity.PRIVATE_JET_TRIP -> AppIcons.Wealth
}

private fun activityPayoffs(activity: LeisureActivity): List<Pair<String, Boolean>> = when (activity) {
    LeisureActivity.WEEKEND_RETREAT -> listOf("+12 Mood" to true, "+5 Health" to true)
    LeisureActivity.NATURE_DAY -> listOf("+8 Mood" to true, "+4 Health" to true)
    LeisureActivity.CITY_SHOW -> listOf("+9 Mood" to true)
    LeisureActivity.NIGHT_OUT -> listOf("+10 Mood" to true, "-3 Health" to false)
    LeisureActivity.PARK_CAREGIVER -> listOf("+7 Mood" to true, "+3 Health" to true)
    LeisureActivity.PLAYGROUND -> listOf("+8 Mood" to true, "+3 Health" to true)
    LeisureActivity.STORYTIME -> listOf("+6 Mood" to true, "+2 Smarts" to true)
    LeisureActivity.VISIT_MUSEUM -> listOf("+6 Mood" to true, "+3 Smarts" to true, "Fine Art Print" to true)
    LeisureActivity.LIBRARY_VISIT -> listOf("+3 Mood" to true, "+4 Smarts" to true)
    LeisureActivity.MEMOIR_WRITING -> listOf("+5 Mood" to true, "+4 Smarts" to true)
    LeisureActivity.CHILDHOOD_HOBBY -> listOf("+7 Mood" to true, "+3 Smarts" to true, "+Aptitude" to true)
    LeisureActivity.STUDY_BUDDY -> listOf("+2 Mood" to true, "+5 Smarts" to true)
    LeisureActivity.PLAY_WITH_TOYS -> listOf("+6 Mood" to true, "+2 Smarts" to true)
    LeisureActivity.GAMING_MARATHON -> listOf("+8 Mood" to true, "-2 Health" to false, "+1 Smarts" to true)
    LeisureActivity.MALL_HANGOUT -> listOf("+7 Mood" to true, "+1 Smarts" to true)
    LeisureActivity.SNEAK_OUT -> listOf("+8 Mood" to true, "Curfew Risk" to false)
    LeisureActivity.CHORES -> listOf("+Cash Pay" to true, "+Trust" to true, "-2 Mood" to false)
    LeisureActivity.MEDITATION -> listOf("+4 Mood" to true, "+2 Health" to true)
    LeisureActivity.SPA_DAY -> listOf("+7 Mood" to true, "+6 Health" to true)
    LeisureActivity.ANNUAL_CHECKUP -> listOf("+6 Health" to true)
    LeisureActivity.REUNION_DINNER -> listOf("+10 Mood" to true, "+1 Smarts" to true)
    LeisureActivity.COMMUNITY_CLUB -> listOf("+6 Mood" to true, "+3 Health" to true)
    LeisureActivity.GRANDKIDS_DAY -> listOf("+11 Mood" to true, "+2 Health" to true)
    LeisureActivity.NAP_ROUTINE -> listOf("+2 Mood" to true, "+4 Health" to true)
    LeisureActivity.PLAYDATE -> listOf("+8 Mood" to true, "+2 Health" to true)
    LeisureActivity.THROW_TANTRUM -> listOf("+3 Mood" to true, "-1 Health" to false)
    LeisureActivity.ASK_ALLOWANCE -> listOf("+Allowance Cash" to true, "+4 Mood" to true)
    LeisureActivity.YACHT_DAY -> listOf("+15 Mood" to true, "+3 Health" to true, "+Prestige" to true)
    LeisureActivity.PRIVATE_JET_TRIP -> listOf("+18 Mood" to true, "+2 Health" to true, "+Prestige" to true)
}
