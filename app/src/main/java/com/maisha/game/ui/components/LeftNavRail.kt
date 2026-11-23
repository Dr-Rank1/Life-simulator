// app/src/main/java/com/maisha/game/ui/components/LeftNavRail.kt
package com.maisha.game.ui.components

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.maisha.game.R
import com.maisha.game.ui.theme.AppIcons
import com.maisha.game.ui.theme.DeepSlate
import com.maisha.game.ui.theme.DeepSlateCard
import com.maisha.game.ui.theme.LifeGreen

private data class NavRailItem(
    val tab: MainTab,
    @StringRes val labelRes: Int,
    val icon: ImageVector
)

private val railTabs = listOf(
    NavRailItem(MainTab.LIFE, R.string.nav_life, AppIcons.NavLife),
    NavRailItem(MainTab.FAMILY, R.string.nav_people, AppIcons.NavFamily),
    NavRailItem(MainTab.CAREER, R.string.nav_career, AppIcons.NavCareer),
    NavRailItem(MainTab.ASSETS, R.string.nav_assets, AppIcons.NavAssets),
    NavRailItem(MainTab.ACTIONS, R.string.nav_actions, AppIcons.NavActions)
)

/**
 * High-contrast left-side vertical navigation rail.
 * Anchors the primary game tabs along a 58dp docked vertical spine.
 */
@Composable
fun LeftNavRail(
    selectedTab: MainTab,
    onTabSelected: (MainTab) -> Unit,
    disabledTabs: Set<MainTab> = emptySet(),
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .widthIn(min = 60.dp, max = 76.dp)
            .fillMaxHeight()
            .background(DeepSlate)
            .drawBehind {
                // Subtle 1dp right border anchoring the HUD rail
                drawLine(
                    color = DeepSlateCard,
                    start = Offset(size.width, 0f),
                    end = Offset(size.width, size.height),
                    strokeWidth = 1.dp.toPx()
                )
            }
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        railTabs.forEach { item ->
            val selected = selectedTab == item.tab
            val disabled = item.tab in disabledTabs
            val label = stringResource(item.labelRes)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 56.dp)
                    .padding(vertical = 2.dp)
                    .semantics { role = Role.Tab }
                    .clickable(enabled = !disabled) { onTabSelected(item.tab) },
                contentAlignment = Alignment.Center
            ) {
                // Active indicator pill on the far-left edge
                if (selected) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .width(4.dp)
                            .height(28.dp)
                            .clip(RoundedCornerShape(topEnd = 3.dp, bottomEnd = 3.dp))
                            .background(LifeGreen)
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Soft highlight box behind icon when active
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (selected) Color(0x3322C55E)
                                else Color.Transparent
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = label,
                            tint = when {
                                disabled -> Color(0xFF475569)
                                selected -> LifeGreen
                                else -> Color(0xFF94A3B8)
                            },
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = label,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 10.sp,
                        color = when {
                            disabled -> Color(0xFF475569)
                            selected -> LifeGreen
                            else -> Color(0xFF94A3B8)
                        }
                    )
                }
            }
        }
    }
}
