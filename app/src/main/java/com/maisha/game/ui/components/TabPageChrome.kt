package com.maisha.game.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.maisha.game.ui.theme.CreamBg
import com.maisha.game.ui.theme.InkPrimary
import com.maisha.game.ui.theme.InkSecondary
import com.maisha.game.ui.theme.LifeGreen
import com.maisha.game.ui.theme.Hairline

import com.maisha.game.ui.theme.PaperWhite
import com.maisha.game.ui.theme.RoyalIndigo
import com.maisha.game.ui.theme.StatMoney

/** Other tabs: title on the left, cash on the right. */
@Composable
@Suppress("UNUSED_PARAMETER")
fun TabPageHero(
    title: String,
    subtitle: String,
    primaryChip: String,
    secondaryChip: String? = null,
    tertiaryChip: String? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(CreamBg)
            .padding(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Black,
                color = InkPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (subtitle.isNotBlank()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = InkSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
        Text(
            text = primaryChip,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Black,
            color = StatMoney,
            maxLines = 1
        )
    }
}

@Composable
fun CategoryFilterChipRow(
    labels: List<String>,
    selectedIndex: Int,
    onSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        labels.forEachIndexed { index, label ->
            val active = selectedIndex == index
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (active) FontWeight.Bold else FontWeight.Medium,
                color = if (active) Color.White else InkSecondary,
                modifier = Modifier
                    .clip(RoundedCornerShape(99.dp))
                    .background(if (active) RoyalIndigo else PaperWhite)
                    .border(1.dp, if (active) Color.Transparent else Hairline, RoundedCornerShape(99.dp))
                    .clickable { onSelected(index) }
                    .padding(horizontal = 14.dp, vertical = 7.dp)
            )

        }
    }
}
