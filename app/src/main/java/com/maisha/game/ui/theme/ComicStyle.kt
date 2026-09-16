package com.maisha.game.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Thick ink outlines used across the life-sim chrome. */
object ComicStyle {
    val outline: Dp = 2.5.dp
    val ink: Color = OutlineInk
    val paper: Color = PaperWhite
    val cardShape: RoundedCornerShape = RoundedCornerShape(14.dp)
    val pillShape: RoundedCornerShape = RoundedCornerShape(18.dp)
    val bubbleShape: RoundedCornerShape = RoundedCornerShape(22.dp)
}

fun Modifier.comicOutline(
    shape: Shape = ComicStyle.cardShape,
    width: Dp = ComicStyle.outline,
    color: Color = ComicStyle.ink
): Modifier = border(width, color, shape)

fun Modifier.comicPanel(
    shape: Shape = ComicStyle.cardShape,
    fill: Color = ComicStyle.paper
): Modifier = clip(shape)
    .background(fill)
    .comicOutline(shape)

fun Modifier.comicChip(fill: Color = ComicStyle.paper): Modifier =
    clip(ComicStyle.pillShape)
        .background(fill)
        .border(2.dp, ComicStyle.ink, ComicStyle.pillShape)
        .padding(horizontal = 10.dp, vertical = 4.dp)
