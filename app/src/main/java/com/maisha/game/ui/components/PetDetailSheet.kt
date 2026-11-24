// app/src/main/java/com/maisha/game/ui/components/PetDetailSheet.kt
package com.maisha.game.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Fastfood
import androidx.compose.material.icons.rounded.Healing
import androidx.compose.material.icons.rounded.Pets
import androidx.compose.material.icons.rounded.SportsEsports
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.maisha.game.R
import com.maisha.game.data.model.Pet
import com.maisha.game.data.model.PetSpecies
import com.maisha.game.domain.PetCareAction
import com.maisha.game.ui.theme.AccentPink
import com.maisha.game.ui.theme.CoralNegative
import com.maisha.game.ui.theme.GoldAccent
import com.maisha.game.ui.theme.Hairline
import com.maisha.game.ui.theme.InkPrimary
import com.maisha.game.ui.theme.InkSecondary
import com.maisha.game.ui.theme.InkTertiary
import com.maisha.game.ui.theme.LifeGreen
import com.maisha.game.ui.theme.NavyDeep
import com.maisha.game.ui.theme.PaperWhite
import com.maisha.game.ui.theme.SurfaceMuted
import com.maisha.game.ui.theme.TealPrimary
import com.maisha.game.util.formatMoney

@Composable
fun PetDetailSheet(
    pet: Pet,
    speciesLabel: String,
    playerCountryCode: String,
    playerMoney: Int,
    feedCost: Int,
    vetCost: Int,
    onCare: (PetCareAction) -> Unit
) {
    val speciesColor = when (pet.species) {
        PetSpecies.DOG -> TealPrimary
        PetSpecies.CAT -> GoldAccent
        PetSpecies.BIRD -> AccentPink
        PetSpecies.FISH -> NavyDeep
        PetSpecies.EXOTIC -> LifeGreen
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // BitLife-Style Pet Header
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            color = SurfaceMuted.copy(alpha = 0.5f),
            border = BorderStroke(1.dp, Hairline)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Vector Pet Icon Box (Zero Emojis)
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(speciesColor.copy(alpha = 0.14f))
                        .border(2.dp, speciesColor.copy(alpha = 0.4f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Pets,
                        contentDescription = null,
                        tint = speciesColor,
                        modifier = Modifier.size(34.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = speciesColor.copy(alpha = 0.12f),
                        border = BorderStroke(0.5.dp, speciesColor.copy(alpha = 0.35f))
                    ) {
                        Text(
                            text = speciesLabel.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = speciesColor,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            fontSize = 10.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(3.dp))

                    Text(
                        text = pet.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = stringResource(R.string.format_age, pet.age),
                        style = MaterialTheme.typography.bodyMedium,
                        color = InkSecondary
                    )
                }
            }
        }

        // Dual Stat Bars: Bond & Health
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Bond Meter
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.stat_pet_bond),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = InkSecondary
                    )
                    Text(
                        text = "${pet.relationshipLevel}%",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (pet.relationshipLevel >= 70) TealPrimary else GoldAccent
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Hairline)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(fraction = (pet.relationshipLevel / 100f).coerceIn(0f, 1f))
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (pet.relationshipLevel >= 70) TealPrimary else GoldAccent)
                    )
                }
            }

            // Health Meter
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.stat_health),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = InkSecondary
                    )
                    Text(
                        text = "${pet.health}%",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (pet.health >= 70) LifeGreen else if (pet.health <= 35) CoralNegative else GoldAccent
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Hairline)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(fraction = (pet.health / 100f).coerceIn(0f, 1f))
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (pet.health >= 70) LifeGreen else if (pet.health <= 35) CoralNegative else GoldAccent)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = stringResource(R.string.section_pet_care).uppercase(),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = InkTertiary
        )

        // Tactile Care Opportunity Buttons
        PetCareTile(
            title = if (pet.playedThisYear) stringResource(R.string.btn_play_pet_done) else stringResource(R.string.btn_play_pet),
            icon = Icons.Rounded.SportsEsports,
            iconTint = TealPrimary,
            costLabel = stringResource(R.string.tile_cost_free),
            isDone = pet.playedThisYear,
            enabled = !pet.playedThisYear,
            onClick = { onCare(PetCareAction.PLAY) }
        )

        val feedAffordable = playerMoney >= feedCost
        PetCareTile(
            title = stringResource(R.string.btn_feed_pet, formatMoney(feedCost, playerCountryCode)),
            icon = Icons.Rounded.Fastfood,
            iconTint = GoldAccent,
            costLabel = formatMoney(feedCost, playerCountryCode),
            isDone = false,
            enabled = feedAffordable,
            onClick = { onCare(PetCareAction.FEED) }
        )

        val vetAffordable = playerMoney >= vetCost
        PetCareTile(
            title = if (pet.caredThisYear) stringResource(R.string.btn_vet_pet_done) else stringResource(R.string.btn_vet_pet, formatMoney(vetCost, playerCountryCode)),
            icon = Icons.Rounded.Healing,
            iconTint = CoralNegative,
            costLabel = formatMoney(vetCost, playerCountryCode),
            isDone = pet.caredThisYear,
            enabled = !pet.caredThisYear && vetAffordable,
            onClick = { onCare(PetCareAction.VET) }
        )

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun PetCareTile(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    costLabel: String,
    isDone: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        enabled = enabled && !isDone,
        shape = RoundedCornerShape(14.dp),
        color = if (isDone) SurfaceMuted.copy(alpha = 0.5f) else PaperWhite,
        border = BorderStroke(1.dp, if (isDone) Hairline.copy(alpha = 0.4f) else Hairline),
        shadowElevation = if (isDone || !enabled) 0.dp else 1.dp,
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        if (isDone) InkTertiary.copy(alpha = 0.1f)
                        else iconTint.copy(alpha = 0.14f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isDone) InkTertiary else iconTint,
                    modifier = Modifier.size(22.dp)
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isDone) InkSecondary else InkPrimary,
                    maxLines = 1
                )
                Text(
                    text = if (isDone) stringResource(R.string.tile_used_badge) else costLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isDone) InkTertiary else if (enabled) TealPrimary else CoralNegative,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
