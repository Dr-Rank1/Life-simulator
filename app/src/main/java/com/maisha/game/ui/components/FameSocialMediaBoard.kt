// app/src/main/java/com/maisha/game/ui/components/FameSocialMediaBoard.kt
package com.maisha.game.ui.components

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
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
import com.maisha.game.R
import com.maisha.game.data.model.CareerTrack
import com.maisha.game.data.model.Character
import com.maisha.game.data.model.FameTier
import com.maisha.game.data.model.TalentTraining
import com.maisha.game.domain.CareerEngine
import com.maisha.game.domain.SocialMediaEngine
import com.maisha.game.ui.theme.AppIcons
import com.maisha.game.ui.theme.BorderCardSlate
import com.maisha.game.ui.theme.CoralNegative
import com.maisha.game.ui.theme.CreamBg
import com.maisha.game.ui.theme.GoldAccent
import com.maisha.game.ui.theme.InkPrimary
import com.maisha.game.ui.theme.InkSecondary
import com.maisha.game.ui.theme.InkTertiary
import com.maisha.game.ui.theme.PaperWhite
import com.maisha.game.ui.theme.SuccessGreen
import com.maisha.game.ui.theme.TealPrimary
import com.maisha.game.util.formatMoney

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FameSocialMediaBoard(
    character: Character,
    careerEngine: CareerEngine,
    onCreateAccount: () -> Unit,
    onPostContent: () -> Unit,
    onBrandSponsorship: () -> Unit,
    onCreatorCollab: () -> Unit,
    onDigitalDetox: () -> Unit,
    onMonetizeAds: () -> Unit,
    onHandleDrama: (Boolean) -> Unit,
    onTrainTalent: (TalentTraining) -> Unit,
    onSignEndorsement: () -> Unit,
    onDropEndorsement: () -> Unit,
    onLeaveCareerTrack: () -> Unit,
    onStartCareerTrack: (CareerTrack) -> Unit,
    onPracticeCareerTrack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val social = character.socialMedia
    val career = character.career
    val countryCode = character.countryCode

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Section 1: Social Media & Creator Hub
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = PaperWhite),
            border = BorderStroke(1.dp, BorderCardSlate)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header with Fame Tier & Follower Count
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(GoldAccent.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = AppIcons.BrandSponsor,
                                contentDescription = null,
                                tint = GoldAccent,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Column {
                            Text(
                                text = fameTierLabel(social.fameTier),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = InkPrimary
                            )
                            Text(
                                text = if (social.hasAccount) {
                                    "${social.followers} followers" + if (social.isVerified) " (Verified)" else ""
                                } else {
                                    "No Account Yet"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = if (social.isVerified) TealPrimary else InkTertiary
                            )
                        }
                    }

                    // Fame Rating Pill
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = CreamBg,
                        border = BorderStroke(1.dp, BorderCardSlate)
                    ) {
                        Text(
                            text = "Fame: ${career.fame}%",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = TealPrimary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                if (social.hasAccount) {
                    // Audience Trust & Burnout Gauges
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${stringResource(R.string.label_creator_reputation)}: ${social.reputation}%",
                                style = MaterialTheme.typography.labelSmall,
                                color = InkSecondary
                            )
                            Text(
                                text = if (social.reputation >= 80) "High Goodwill" else "Polarized",
                                style = MaterialTheme.typography.labelSmall,
                                color = InkTertiary
                            )
                        }
                        LinearProgressIndicator(
                            progress = { social.reputation / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = if (social.reputation >= 60) SuccessGreen else CoralNegative,
                            trackColor = BorderCardSlate
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${stringResource(R.string.label_creator_burnout)}: ${social.creatorBurnout}%",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (social.creatorBurnout >= 75) CoralNegative else InkSecondary
                            )
                            Text(
                                text = if (social.creatorBurnout >= 75) "Severe Burnout Risk" else "Healthy",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (social.creatorBurnout >= 75) CoralNegative else InkTertiary
                            )
                        }
                        LinearProgressIndicator(
                            progress = { social.creatorBurnout / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = if (social.creatorBurnout >= 75) CoralNegative else TealPrimary,
                            trackColor = BorderCardSlate
                        )
                    }

                    // Drama Resolution Banner (if active)
                    if (social.dramaActive) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = CoralNegative.copy(alpha = 0.08f),
                            border = BorderStroke(1.dp, CoralNegative.copy(alpha = 0.3f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(10.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = stringResource(R.string.dialog_drama_title),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = CoralNegative
                                )
                                Text(
                                    text = stringResource(R.string.dialog_drama_desc),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = InkTertiary
                                )
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Button(
                                        onClick = { onHandleDrama(true) },
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
                                    ) {
                                        Text(stringResource(R.string.btn_apologize_humble))
                                    }
                                    OutlinedButton(
                                        onClick = { onHandleDrama(false) },
                                        shape = RoundedCornerShape(8.dp),
                                        border = BorderStroke(1.dp, CoralNegative)
                                    ) {
                                        Text(stringResource(R.string.btn_double_down), color = CoralNegative)
                                    }
                                }
                            }
                        }
                    }

                    // Creator Actions Flow
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Post Content
                        Button(
                            onClick = onPostContent,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
                        ) {
                            Icon(AppIcons.Looks, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Post Viral Update")
                        }

                        // Brand Deal / Sponsorship
                        if (social.followers >= SocialMediaEngine.SPONSORSHIP_MIN_FOLLOWERS) {
                            OutlinedButton(
                                onClick = onBrandSponsorship,
                                enabled = !social.brandDealDoneThisYear,
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, GoldAccent)
                            ) {
                                Icon(AppIcons.BrandSponsor, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text(stringResource(R.string.btn_brand_sponsorship), color = GoldAccent)
                            }
                        }

                        // Creator Collaboration
                        OutlinedButton(
                            onClick = onCreatorCollab,
                            enabled = !social.collabDoneThisYear,
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, TealPrimary)
                        ) {
                            Icon(AppIcons.CreatorCollab, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(stringResource(R.string.btn_creator_collab), color = TealPrimary)
                        }

                        // Digital Detox
                        OutlinedButton(
                            onClick = onDigitalDetox,
                            enabled = !social.detoxDoneThisYear,
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(AppIcons.DigitalDetox, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(stringResource(R.string.btn_digital_detox))
                        }

                        // Monetize Ads
                        if (social.followers >= SocialMediaEngine.MONETIZATION_FOLLOWER_THRESHOLD) {
                            OutlinedButton(
                                onClick = onMonetizeAds,
                                enabled = !social.monetizedThisYear,
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, SuccessGreen)
                            ) {
                                Icon(AppIcons.Money, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text(stringResource(R.string.btn_monetize_ads), color = SuccessGreen)
                            }
                        }
                    }
                } else {
                    // Create Account CTA
                    Button(
                        onClick = onCreateAccount,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
                    ) {
                        Text("Create Social Media Account")
                    }
                }
            }
        }

        // Section 2: Talent & Track Drills (Athleticism & Music)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = PaperWhite),
            border = BorderStroke(1.dp, BorderCardSlate)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Talent & Celebrity Tracks",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = InkPrimary
                )

                // Athleticism Meter
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Athleticism: ${career.athleticism}%", style = MaterialTheme.typography.labelSmall)
                    }
                    LinearProgressIndicator(
                        progress = { career.athleticism / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = TealPrimary,
                        trackColor = BorderCardSlate
                    )
                }

                // Musical Talent Meter
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Musical Talent: ${career.musicalTalent}%", style = MaterialTheme.typography.labelSmall)
                    }
                    LinearProgressIndicator(
                        progress = { career.musicalTalent / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = GoldAccent,
                        trackColor = BorderCardSlate
                    )
                }

                // Practice & Drill Buttons
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { onTrainTalent(TalentTraining.GYM_SESSION) },
                        enabled = !career.gymTrainedThisYear,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(stringResource(R.string.btn_gym_session))
                    }

                    OutlinedButton(
                        onClick = { onTrainTalent(TalentTraining.MUSIC_LESSON) },
                        enabled = !career.musicTrainedThisYear,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(stringResource(R.string.btn_music_lesson))
                    }

                    OutlinedButton(
                        onClick = {
                            val training = if (career.careerTrack == CareerTrack.PRO_SPORTS) {
                                TalentTraining.EXHIBITION_MATCH
                            } else {
                                TalentTraining.STREET_BUSK
                            }
                            onTrainTalent(training)
                        },
                        enabled = !career.talentGigDoneThisYear,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            if (career.careerTrack == CareerTrack.PRO_SPORTS) {
                                stringResource(R.string.btn_exhibition_match)
                            } else {
                                stringResource(R.string.btn_street_busk)
                            }
                        )
                    }
                }

                // Pro Track Progression
                if (career.careerTrack == CareerTrack.PRO_SPORTS || career.careerTrack == CareerTrack.ENTERTAINMENT) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = CreamBg,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = careerEngine.trackStageLabel(career.careerTrack, career.trackLevel),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = InkPrimary
                                )
                                Text(
                                    text = "Practice Progress: ${career.trackProgress}/100",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = InkTertiary
                                )
                            }
                            Button(
                                onClick = onPracticeCareerTrack,
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
                            ) {
                                Text("Train Track")
                            }
                        }
                    }
                } else if (character.age >= 16) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = { onStartCareerTrack(CareerTrack.PRO_SPORTS) },
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Start Pro Sports")
                        }
                        OutlinedButton(
                            onClick = { onStartCareerTrack(CareerTrack.ENTERTAINMENT) },
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Start Entertainment")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun fameTierLabel(tier: FameTier): String = when (tier) {
    FameTier.UNKNOWN -> stringResource(R.string.tier_unknown)
    FameTier.LOCAL -> stringResource(R.string.tier_local)
    FameTier.REGIONAL -> stringResource(R.string.tier_regional)
    FameTier.NATIONAL -> stringResource(R.string.tier_national)
    FameTier.GLOBAL -> stringResource(R.string.tier_global)
}
