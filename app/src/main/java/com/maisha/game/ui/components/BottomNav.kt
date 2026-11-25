// app/src/main/java/com/maisha/game/ui/components/BottomNav.kt
package com.maisha.game.ui.components

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.maisha.game.R
import com.maisha.game.ui.theme.AppIcons
import com.maisha.game.ui.theme.Hairline
import com.maisha.game.ui.theme.InkPrimary
import com.maisha.game.ui.theme.InkSecondary
import com.maisha.game.ui.theme.InkTertiary
import com.maisha.game.ui.theme.PaperWhite
import com.maisha.game.ui.theme.RoyalIndigo
import com.maisha.game.ui.theme.RoyalIndigoSoft


enum class MainTab {
    LIFE,
    FAMILY,
    CAREER,
    ASSETS,
    ACTIONS
}

private data class TabItem(
    val tab: MainTab,
    @StringRes val labelRes: Int,
    val icon: ImageVector
)

private val tabs = listOf(
    TabItem(MainTab.LIFE, R.string.nav_life, AppIcons.NavLife),
    TabItem(MainTab.FAMILY, R.string.nav_family, AppIcons.NavFamily),
    TabItem(MainTab.CAREER, R.string.nav_career, AppIcons.NavCareer),
    TabItem(MainTab.ASSETS, R.string.nav_assets, AppIcons.NavAssets),
    TabItem(MainTab.ACTIONS, R.string.nav_actions, AppIcons.NavActions)
)

@Composable
fun MaishaBottomNav(
    selectedTab: MainTab,
    onTabSelected: (MainTab) -> Unit,
    disabledTabs: Set<MainTab> = emptySet(),
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier
            .fillMaxWidth()
            .border(width = 1.dp, color = Hairline),
        containerColor = PaperWhite,
        tonalElevation = 2.dp,
        contentColor = InkPrimary
    ) {
        tabs.forEach { item ->
            val selected = selectedTab == item.tab
            val disabled = item.tab in disabledTabs
            val label = stringResource(item.labelRes)
            NavigationBarItem(
                selected = selected,
                onClick = { if (!disabled) onTabSelected(item.tab) },
                enabled = !disabled,
                icon = {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (selected) RoyalIndigoSoft
                                else Color.Transparent
                            )
                            .padding(horizontal = 12.dp, vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        MaishaNavIcon(
                            tab = item.tab,
                            contentDescription = label,
                            size = 24.dp
                        )
                    }
                },
                label = {
                    Text(
                        text = label,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                        style = MaterialTheme.typography.labelSmall
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = RoyalIndigo,
                    selectedTextColor = RoyalIndigo,
                    indicatorColor = Color.Transparent,
                    unselectedIconColor = InkTertiary,
                    unselectedTextColor = InkSecondary
                )
            )
        }
    }
}

