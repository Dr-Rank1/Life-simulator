// app/src/main/java/com/maisha/game/ui/components/InsetCardContainer.kt
package com.maisha.game.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.maisha.game.ui.theme.BorderCardSlate
import com.maisha.game.ui.theme.PaperWhite
import com.maisha.game.ui.theme.SlateDivider

/**
 * Standard inset grouped card container (BitLife / iOS Settings style).
 * Groups related drill-down items inside a single elevated card container with dividers,
 * replacing scattered individual floating boxes.
 */
@Composable
fun InsetCardContainer(
    modifier: Modifier = Modifier,
    title: String? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        if (!title.isNullOrBlank()) {
            Text(
                text = title.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF475569),
                fontSize = 11.sp,
                letterSpacing = 0.8.sp,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
            )
        }
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = PaperWhite),
            border = BorderStroke(1.dp, BorderCardSlate),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                content()
            }
        }
    }
}

/**
 * Thin hairline divider between inset rows.
 */
@Composable
fun InsetDivider(
    modifier: Modifier = Modifier,
    insetStart: Boolean = true
) {
    HorizontalDivider(
        modifier = if (insetStart) modifier.padding(start = 56.dp) else modifier,
        thickness = 1.dp,
        color = SlateDivider
    )
}
