// app/src/main/java/com/maisha/game/ui/avatar/ProceduralAvatarPortrait.kt
package com.maisha.game.ui.avatar

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import com.maisha.game.data.model.AgeStage
import com.maisha.game.data.model.AgingDetails
import com.maisha.game.data.model.AvatarConfig
import com.maisha.game.data.model.Expression
import com.maisha.game.data.model.EyewearStyle
import com.maisha.game.data.model.FacialHairStyle
import com.maisha.game.data.model.ageStageFor

/**
 * High-fidelity, BitLife / Duolingo-grade procedural 2D character portrait engine.
 * Renders 100% offline in Compose [Canvas] using scalable vector paths, 3D skin shading,
 * layered glossy eyes with specular catchlights, sculpted hairstyles, and dynamic expressions.
 */
@Composable
fun ProceduralAvatarPortrait(
    config: AvatarConfig,
    age: Int,
    expression: Expression,
    modifier: Modifier = Modifier
) {
    val stage = remember(age) { ageStageFor(age) }
    val safeConfig = remember(config) { AvatarAssetMapper.sanitize(config) }

    val rawSkinColor = remember(safeConfig.skinTone) {
        AvatarAssetMapper.skinTint(safeConfig.skinTone)
    }
    val rawHairColor = remember(safeConfig.hairColor, stage, safeConfig.agingDetails) {
        AvatarAssetMapper.hairTint(safeConfig.hairColor, stage, safeConfig.agingDetails)
    }
    val rawOutfitColor = remember(safeConfig.outfitColor) {
        AvatarAssetMapper.outfitTint(safeConfig.outfitColor)
    }

    // Blend hair towards silver/slate-gray for seniors or aging config
    val isSeniorOrGray = remember(stage, safeConfig.agingDetails, age) {
        stage == AgeStage.SENIOR ||
            safeConfig.agingDetails == AgingDetails.GRAYING ||
            safeConfig.agingDetails == AgingDetails.WRINKLES_AND_GRAYING ||
            age >= 60
    }

    val effectiveHairColor = remember(rawHairColor, isSeniorOrGray) {
        if (isSeniorOrGray) {
            lerpColor(rawHairColor, Color(0xFF94A3B8), 0.75f)
        } else {
            rawHairColor
        }
    }

    val allowAdultFeatures = remember(age, stage) {
        age >= 14 && stage != AgeStage.BABY && stage != AgeStage.CHILD
    }
    val allowGlasses = remember(stage, allowAdultFeatures, safeConfig.eyewear) {
        stage != AgeStage.BABY &&
            (allowAdultFeatures || safeConfig.eyewear != EyewearStyle.READING_GLASSES)
    }

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .clip(CircleShape)
    ) {
        val w = size.width
        val h = size.height
        val sx = w / 100f
        val sy = h / 100f

        scale(scaleX = sx, scaleY = sy, pivot = Offset.Zero) {
            // 1. Soft Portrait Backdrop Gradient
            drawCircle(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFFF8FAFC), Color(0xFFE2E8F0)),
                    startY = 0f,
                    endY = 100f
                ),
                radius = 50f,
                center = Offset(50f, 50f)
            )

            // 2. Layer 1: Back Hair Silhouette (Styles 3, 4, 5, 6, 7)
            if (stage != AgeStage.BABY) {
                drawBackHair(
                    hairStyle = safeConfig.hairStyle,
                    hairColor = effectiveHairColor.darken(0.18f)
                )
            }

            // 3. Layer 2: Sloped Shoulders, Outfit Collar, Neck & Chin Cast Shadow
            drawShouldersAndCollar(
                outfitColor = rawOutfitColor,
                stage = stage
            )

            drawNeckAndShadow(
                skinColor = rawSkinColor,
                stage = stage
            )

            // 4. Layer 3: Ears, Sculpted Head Silhouette & Soft Facial Shading
            drawEars(
                skinColor = rawSkinColor
            )

            drawHeadSilhouette(
                skinColor = rawSkinColor,
                stage = stage
            )

            drawSkinShadingAndBlush(
                skinColor = rawSkinColor,
                facialFeature = safeConfig.facialFeature
            )

            // 5. Layer 4: Expressive Eyes, Eyebrows & Illustrated Nose
            drawNose(
                skinColor = rawSkinColor
            )

            drawEyes(
                expression = expression,
                skinColor = rawSkinColor
            )

            drawEyebrows(
                expression = expression,
                hairColor = effectiveHairColor
            )

            // 6. Layer 5: Expressive Bezier Mouths
            drawMouth(
                expression = expression,
                skinColor = rawSkinColor
            )

            // 7. Layer 6: Contoured Facial Hair (Strictly Gated to age >= 14)
            if (allowAdultFeatures && safeConfig.facialHair != null) {
                drawFacialHair(
                    facialHair = safeConfig.facialHair,
                    hairColor = effectiveHairColor
                )
            }

            // 8. Senior Wrinkle Contours (Crow's feet, Forehead & Laugh Lines)
            if (isSeniorOrGray) {
                drawSeniorWrinkles(
                    skinColor = rawSkinColor
                )
            }

            // 9. Layer 7: Sculpted Front Hairstyles with Specular Sheen
            drawFrontHair(
                hairStyle = safeConfig.hairStyle,
                hairColor = effectiveHairColor,
                skinColor = rawSkinColor,
                stage = stage
            )

            // 10. Layer 8: Eyewear
            if (allowGlasses && safeConfig.eyewear != null) {
                drawEyewear(
                    eyewear = safeConfig.eyewear
                )
            }

            // 11. Accessories (Hat, Earring on lobe, Hair clip)
            safeConfig.accessoryId?.let { accId ->
                drawAccessory(
                    accessoryId = accId,
                    stage = stage
                )
            }

            // 12. Crisp Inner Ring Border
            drawCircle(
                color = Color(0xFF94A3B8).copy(alpha = 0.35f),
                radius = 49.25f,
                center = Offset(50f, 50f),
                style = Stroke(width = 1.5f)
            )
        }
    }
}

// ============================================================================
// LAYER DRAWING FUNCTIONS (0..100 NORMALIZED COORDINATE SPACE)
// ============================================================================

/** Layer 1: Back Hair Silhouette (Styles 3, 4, 5, 6, 7) */
private fun DrawScope.drawBackHair(hairStyle: Int, hairColor: Color) {
    when (hairStyle) {
        3 -> {
            // Crown Braids / Locs falling past shoulders
            val leftLocs = Path().apply {
                moveTo(28f, 38f)
                cubicTo(18f, 54f, 16f, 74f, 18f, 96f)
                lineTo(34f, 96f)
                cubicTo(32f, 76f, 32f, 54f, 35f, 44f)
                close()
            }
            val rightLocs = Path().apply {
                moveTo(72f, 38f)
                cubicTo(82f, 54f, 84f, 74f, 82f, 96f)
                lineTo(66f, 96f)
                cubicTo(68f, 76f, 68f, 54f, 65f, 44f)
                close()
            }
            drawPath(leftLocs, hairColor)
            drawPath(rightLocs, hairColor)
        }
        4 -> {
            // Sleek Chin-Length Bob: curved bell framing the neck
            val bob = Path().apply {
                moveTo(23f, 32f)
                cubicTo(15f, 46f, 15f, 66f, 25f, 75f)
                lineTo(75f, 75f)
                cubicTo(85f, 66f, 85f, 46f, 77f, 32f)
                cubicTo(68f, 20f, 32f, 20f, 23f, 32f)
                close()
            }
            drawPath(bob, hairColor)
        }
        5 -> {
            // Long Wavy Layers: cascading flowing locks
            val leftWaves = Path().apply {
                moveTo(24f, 30f)
                cubicTo(14f, 46f, 12f, 70f, 14f, 98f)
                lineTo(34f, 98f)
                cubicTo(30f, 75f, 28f, 55f, 32f, 38f)
                close()
            }
            val rightWaves = Path().apply {
                moveTo(76f, 30f)
                cubicTo(86f, 46f, 88f, 70f, 86f, 98f)
                lineTo(66f, 98f)
                cubicTo(70f, 75f, 72f, 55f, 68f, 38f)
                close()
            }
            drawPath(leftWaves, hairColor)
            drawPath(rightWaves, hairColor)
        }
        6 -> {
            // High Top-Knot / Afro Puff: voluminous crown puff
            val puff = Path().apply {
                moveTo(36f, 14f)
                cubicTo(30f, 8f, 32f, -2f, 42f, -2f)
                cubicTo(45f, -8f, 55f, -8f, 58f, -2f)
                cubicTo(68f, -2f, 70f, 8f, 64f, 14f)
                cubicTo(70f, 18f, 66f, 25f, 58f, 24f)
                lineTo(42f, 24f)
                cubicTo(34f, 25f, 30f, 18f, 36f, 14f)
                close()
            }
            drawPath(puff, hairColor)
        }
        7 -> {
            // Volumetric Curly Afro Volume Behind
            val afroBack = Path().apply {
                moveTo(22f, 38f)
                cubicTo(16f, 24f, 22f, 4f, 42f, 2f)
                cubicTo(46f, 0f, 54f, 0f, 58f, 2f)
                cubicTo(78f, 4f, 84f, 24f, 78f, 38f)
                close()
            }
            drawPath(afroBack, hairColor)
        }
    }
}

/** Layer 2: Sloped Shoulders & Outfit Collar */
private fun DrawScope.drawShouldersAndCollar(outfitColor: Color, stage: AgeStage) {
    val shoulders = Path().apply {
        moveTo(-5f, 105f)
        cubicTo(10f, 84f, 24f, 78f, 37f, 78f)
        lineTo(63f, 78f)
        cubicTo(76f, 78f, 90f, 84f, 105f, 105f)
        lineTo(105f, 105f)
        lineTo(-5f, 105f)
        close()
    }
    drawPath(shoulders, outfitColor)

    // Subtle bottom shadow gradient on clothing
    drawPath(
        path = shoulders,
        brush = Brush.verticalGradient(
            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.16f)),
            startY = 78f,
            endY = 100f
        )
    )

    if (stage == AgeStage.BABY) {
        // Soft baby onesie bib & pastel buttons
        val onesieBib = Path().apply {
            moveTo(38f, 78f)
            cubicTo(42f, 87f, 58f, 87f, 62f, 78f)
        }
        drawPath(
            path = onesieBib,
            color = outfitColor.lighten(0.35f),
            style = Stroke(width = 2.4f, cap = StrokeCap.Round)
        )
        drawCircle(
            color = Color.White.copy(alpha = 0.85f),
            radius = 1.3f,
            center = Offset(50f, 89f)
        )
        drawCircle(
            color = Color.White.copy(alpha = 0.85f),
            radius = 1.3f,
            center = Offset(50f, 94f)
        )
    } else {
        // Clean crew-neck collar band
        val collar = Path().apply {
            moveTo(36f, 78f)
            cubicTo(42f, 85f, 58f, 85f, 64f, 78f)
        }
        drawPath(
            path = collar,
            color = outfitColor.lighten(0.20f),
            style = Stroke(width = 2.2f, cap = StrokeCap.Round)
        )
    }
}

/** Layer 2: Neck & Chin Cast Shadow */
private fun DrawScope.drawNeckAndShadow(skinColor: Color, stage: AgeStage) {
    val neck = Path().apply {
        if (stage == AgeStage.BABY) {
            moveTo(41f, 56f)
            lineTo(39f, 79f)
            lineTo(61f, 79f)
            lineTo(59f, 56f)
        } else {
            moveTo(39f, 56f)
            lineTo(37f, 79f)
            lineTo(63f, 79f)
            lineTo(61f, 56f)
        }
        close()
    }
    drawPath(neck, skinColor)

    // Curved chin cast shadow on upper neck
    val shadow = Path().apply {
        moveTo(38f, 62f)
        cubicTo(44f, 69f, 56f, 69f, 62f, 62f)
        cubicTo(56f, 65.5f, 44f, 65.5f, 38f, 62f)
        close()
    }
    drawPath(shadow, skinColor.darken(0.20f))
}

/** Layer 3: Symmetrical Ears */
private fun DrawScope.drawEars(skinColor: Color) {
    // Left ear
    val leftEar = Path().apply {
        moveTo(27f, 35f)
        cubicTo(19f, 38f, 18f, 48f, 26f, 50f)
        close()
    }
    drawPath(leftEar, skinColor)
    val leftEarContour = Path().apply {
        moveTo(25f, 38f)
        cubicTo(21f, 41f, 21f, 46f, 25f, 47f)
    }
    drawPath(leftEarContour, skinColor.darken(0.16f), style = Stroke(width = 1.2f, cap = StrokeCap.Round))

    // Right ear
    val rightEar = Path().apply {
        moveTo(73f, 35f)
        cubicTo(81f, 38f, 82f, 48f, 74f, 50f)
        close()
    }
    drawPath(rightEar, skinColor)
    val rightEarContour = Path().apply {
        moveTo(75f, 38f)
        cubicTo(79f, 41f, 79f, 46f, 75f, 47f)
    }
    drawPath(rightEarContour, skinColor.darken(0.16f), style = Stroke(width = 1.2f, cap = StrokeCap.Round))
}

/** Layer 3: Age-Sculpted Head Silhouette */
private fun DrawScope.drawHeadSilhouette(skinColor: Color, stage: AgeStage) {
    val head = Path().apply {
        when (stage) {
            AgeStage.BABY, AgeStage.CHILD -> {
                // Plump cranium, round cheeks, soft rounded chin
                moveTo(50f, 15f)
                cubicTo(68f, 15f, 75f, 26f, 75f, 38f)
                cubicTo(75f, 50f, 73f, 59f, 64f, 64f)
                cubicTo(58f, 67.5f, 42f, 67.5f, 36f, 64f)
                cubicTo(27f, 59f, 25f, 50f, 25f, 38f)
                cubicTo(25f, 26f, 32f, 15f, 50f, 15f)
            }
            AgeStage.TEEN, AgeStage.ADULT -> {
                // Smooth temple-to-cheekbone taper, clean defined jawline
                moveTo(50f, 11f)
                cubicTo(68f, 11f, 74f, 22f, 74f, 36f)
                cubicTo(74f, 46f, 71f, 54f, 65f, 60f)
                cubicTo(59f, 66f, 55f, 70f, 50f, 70.5f)
                cubicTo(45f, 70f, 41f, 66f, 35f, 60f)
                cubicTo(29f, 54f, 26f, 46f, 26f, 36f)
                cubicTo(26f, 22f, 32f, 11f, 50f, 11f)
            }
            AgeStage.SENIOR -> {
                // Slightly softer jawline with mature contours
                moveTo(50f, 11f)
                cubicTo(68f, 11f, 74f, 23f, 74f, 37f)
                cubicTo(74f, 48f, 72f, 56f, 65f, 61f)
                cubicTo(59f, 67f, 55f, 71f, 50f, 71.5f)
                cubicTo(45f, 71f, 41f, 67f, 35f, 61f)
                cubicTo(28f, 56f, 26f, 48f, 26f, 37f)
                cubicTo(26f, 23f, 32f, 11f, 50f, 11f)
            }
        }
        close()
    }
    drawPath(head, skinColor)
}

/** Layer 3: 3D Skin Shading & Blush */
private fun DrawScope.drawSkinShadingAndBlush(skinColor: Color, facialFeature: Int?) {
    // 1. Forehead highlight glow
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(skinColor.lighten(0.08f), Color.Transparent),
            center = Offset(50f, 27f),
            radius = 22f
        ),
        radius = 22f,
        center = Offset(50f, 27f)
    )

    // 2. Jawline subtle rim shadow
    val jawShadow = Path().apply {
        moveTo(33f, 58f)
        cubicTo(42f, 68f, 58f, 68f, 67f, 58f)
    }
    drawPath(
        path = jawShadow,
        color = skinColor.darken(0.10f),
        style = Stroke(width = 1.8f, cap = StrokeCap.Round)
    )

    // 3. Apples of the cheeks blush
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFFF43F5E).copy(alpha = 0.16f), Color.Transparent),
            center = Offset(33f, 48f),
            radius = 8.5f
        ),
        radius = 8.5f,
        center = Offset(33f, 48f)
    )
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFFF43F5E).copy(alpha = 0.16f), Color.Transparent),
            center = Offset(67f, 48f),
            radius = 8.5f
        ),
        radius = 8.5f,
        center = Offset(67f, 48f)
    )

    // 4. Freckles / Beauty Marks / Dimples
    when (facialFeature) {
        1, 4 -> {
            // Delicate warm-brown freckles across bridge and cheeks
            val freckleColor = Color(0xFF78350F).copy(alpha = 0.32f)
            val dots = listOf(
                Offset(45f, 45f), Offset(47f, 47f), Offset(53f, 46f), Offset(55f, 48f),
                Offset(36f, 48f), Offset(38f, 50f), Offset(62f, 49f), Offset(64f, 47f)
            )
            dots.forEach { dot ->
                drawCircle(color = freckleColor, radius = 0.7f, center = dot)
            }
        }
        2 -> {
            // Refined beauty mark on lower cheek
            drawCircle(
                color = Color(0xFF29150B),
                radius = 0.9f,
                center = Offset(62f, 54f)
            )
        }
        3 -> {
            // Cute smile dimple accents
            val dimple = skinColor.darken(0.20f)
            val leftDimple = Path().apply {
                moveTo(34f, 54.5f)
                cubicTo(33f, 56f, 33f, 57.5f, 34f, 59f)
            }
            val rightDimple = Path().apply {
                moveTo(66f, 54.5f)
                cubicTo(67f, 56f, 67f, 57.5f, 66f, 59f)
            }
            drawPath(leftDimple, dimple, style = Stroke(width = 1.1f, cap = StrokeCap.Round))
            drawPath(rightDimple, dimple, style = Stroke(width = 1.1f, cap = StrokeCap.Round))
        }
    }
}

/** Layer 4: Minimal Illustrated Nose */
private fun DrawScope.drawNose(skinColor: Color) {
    // Nose bridge and nostril curve
    val nose = Path().apply {
        moveTo(49f, 42f)
        cubicTo(48.5f, 45f, 48.5f, 47f, 50f, 47.5f)
        cubicTo(51.8f, 47.5f, 52.5f, 46f, 53.5f, 44.5f)
    }
    drawPath(
        path = nose,
        color = skinColor.darken(0.20f),
        style = Stroke(width = 1.4f, cap = StrokeCap.Round)
    )

    // Tiny specular soft highlight on nose tip
    drawCircle(
        color = skinColor.lighten(0.14f),
        radius = 1.0f,
        center = Offset(50f, 45.8f)
    )
}

/** Layer 4: Layered Glossy Eyes with Catchlights */
private fun DrawScope.drawEyes(expression: Expression, skinColor: Color) {
    val darkSlate = Color(0xFF1E293B)
    val deepPupil = Color(0xFF0F172A)

    if (expression == Expression.HAPPY) {
        // Joyful upward-curved crescent eyes (^ ^)
        val leftCrescent = Path().apply {
            moveTo(31.5f, 38f)
            cubicTo(34.5f, 32.5f, 39.5f, 32.5f, 42.5f, 38f)
        }
        val rightCrescent = Path().apply {
            moveTo(57.5f, 38f)
            cubicTo(60.5f, 32.5f, 65.5f, 32.5f, 68.5f, 38f)
        }
        drawPath(leftCrescent, darkSlate, style = Stroke(width = 2.4f, cap = StrokeCap.Round))
        drawPath(rightCrescent, darkSlate, style = Stroke(width = 2.4f, cap = StrokeCap.Round))

        // Lower eye cheek creases
        val leftCrease = Path().apply {
            moveTo(33f, 40.5f)
            cubicTo(37f, 42f, 40f, 42f, 41.5f, 40.5f)
        }
        val rightCrease = Path().apply {
            moveTo(58.5f, 40.5f)
            cubicTo(60f, 42f, 63f, 42f, 67f, 40.5f)
        }
        drawPath(leftCrease, skinColor.darken(0.18f), style = Stroke(width = 1.0f, cap = StrokeCap.Round))
        drawPath(rightCrease, skinColor.darken(0.18f), style = Stroke(width = 1.0f, cap = StrokeCap.Round))
        return
    }

    // 1. Sclera (White eye almond base)
    val leftSclera = Path().apply {
        moveTo(32f, 36f)
        cubicTo(34f, 32.5f, 40f, 32.5f, 42f, 36f)
        cubicTo(40f, 39.5f, 34f, 39.5f, 32f, 36f)
        close()
    }
    val rightSclera = Path().apply {
        moveTo(58f, 36f)
        cubicTo(60f, 32.5f, 66f, 32.5f, 68f, 36f)
        cubicTo(66f, 39.5f, 60f, 39.5f, 58f, 36f)
        close()
    }
    drawPath(leftSclera, Color.White)
    drawPath(rightSclera, Color.White)

    // 2. Iris & Pupil
    val irisRadius = if (expression == Expression.SURPRISED) 2.4f else 3.0f
    val pupilRadius = if (expression == Expression.SURPRISED) 1.5f else 1.8f

    drawCircle(darkSlate, radius = irisRadius, center = Offset(37f, 36f))
    drawCircle(deepPupil, radius = pupilRadius, center = Offset(37f, 36f))

    drawCircle(darkSlate, radius = irisRadius, center = Offset(63f, 36f))
    drawCircle(deepPupil, radius = pupilRadius, center = Offset(63f, 36f))

    // 3. Specular Catchlights (The "Life" Spark!)
    // Primary catchlight in top-right quadrant
    drawCircle(Color.White, radius = 1.0f, center = Offset(38.2f, 34.8f))
    drawCircle(Color.White, radius = 1.0f, center = Offset(64.2f, 34.8f))

    // Secondary soft catchlight in bottom-left quadrant
    drawCircle(Color.White.copy(alpha = 0.75f), radius = 0.55f, center = Offset(35.8f, 37.0f))
    drawCircle(Color.White.copy(alpha = 0.75f), radius = 0.55f, center = Offset(61.8f, 37.0f))

    // 4. Lash Line / Eyelid Arc
    when (expression) {
        Expression.ANGRY -> {
            // Angled sharp upper eyelid clipping downward toward center
            val leftLash = Path().apply {
                moveTo(31.5f, 34.5f)
                lineTo(42.5f, 37.5f)
            }
            val rightLash = Path().apply {
                moveTo(57.5f, 37.5f)
                lineTo(68.5f, 34.5f)
            }
            drawPath(leftLash, darkSlate, style = Stroke(width = 2.2f, cap = StrokeCap.Round))
            drawPath(rightLash, darkSlate, style = Stroke(width = 2.2f, cap = StrokeCap.Round))
        }
        Expression.SAD -> {
            // Droopy outer eyelid + glistening tear
            val leftLash = Path().apply {
                moveTo(31.5f, 38f)
                cubicTo(34f, 33f, 40f, 34f, 42.5f, 36.5f)
            }
            val rightLash = Path().apply {
                moveTo(57.5f, 36.5f)
                cubicTo(60f, 34f, 66f, 33f, 68.5f, 38f)
            }
            drawPath(leftLash, darkSlate, style = Stroke(width = 2.0f, cap = StrokeCap.Round))
            drawPath(rightLash, darkSlate, style = Stroke(width = 2.0f, cap = StrokeCap.Round))
            // Tear glistens
            drawCircle(Color(0xFF38BDF8).copy(alpha = 0.85f), radius = 0.9f, center = Offset(33f, 38.5f))
            drawCircle(Color(0xFF38BDF8).copy(alpha = 0.85f), radius = 0.9f, center = Offset(67f, 38.5f))
        }
        else -> {
            // Standard rounded upper lash line
            val leftLash = Path().apply {
                moveTo(31.5f, 36f)
                cubicTo(34f, 32f, 40f, 32f, 42.5f, 36f)
            }
            val rightLash = Path().apply {
                moveTo(57.5f, 36f)
                cubicTo(60f, 32f, 66f, 32f, 68.5f, 36f)
            }
            drawPath(leftLash, darkSlate, style = Stroke(width = 1.8f, cap = StrokeCap.Round))
            drawPath(rightLash, darkSlate, style = Stroke(width = 1.8f, cap = StrokeCap.Round))
        }
    }
}

/** Layer 4: Dynamic Eyebrows */
private fun DrawScope.drawEyebrows(expression: Expression, hairColor: Color) {
    val browColor = if (hairColor.red > 0.65f && hairColor.green > 0.65f && hairColor.blue > 0.65f) {
        Color(0xFF475569)
    } else {
        hairColor.darken(0.12f)
    }

    val leftBrow = Path()
    val rightBrow = Path()

    when (expression) {
        Expression.HAPPY -> {
            leftBrow.moveTo(31f, 29f)
            leftBrow.cubicTo(34.5f, 27f, 38.5f, 27f, 42f, 29.5f)

            rightBrow.moveTo(58f, 29.5f)
            rightBrow.cubicTo(61.5f, 27f, 65.5f, 27f, 69f, 29f)
        }
        Expression.ANGRY -> {
            leftBrow.moveTo(31f, 27.5f)
            leftBrow.lineTo(42.5f, 32f)

            rightBrow.moveTo(57.5f, 32f)
            rightBrow.lineTo(69f, 27.5f)
        }
        Expression.SAD -> {
            leftBrow.moveTo(31f, 32f)
            leftBrow.lineTo(42f, 28.5f)

            rightBrow.moveTo(58f, 28.5f)
            rightBrow.lineTo(69f, 32f)
        }
        Expression.SURPRISED -> {
            leftBrow.moveTo(31f, 27.5f)
            leftBrow.cubicTo(34.5f, 25f, 38.5f, 25f, 42f, 27.5f)

            rightBrow.moveTo(58f, 27.5f)
            rightBrow.cubicTo(61.5f, 25f, 65.5f, 25f, 69f, 27.5f)
        }
        Expression.NEUTRAL -> {
            leftBrow.moveTo(31f, 30.5f)
            leftBrow.cubicTo(34.5f, 29.5f, 38.5f, 29.5f, 42f, 31f)

            rightBrow.moveTo(58f, 31f)
            rightBrow.cubicTo(61.5f, 29.5f, 65.5f, 29.5f, 69f, 30.5f)
        }
    }

    drawPath(leftBrow, browColor, style = Stroke(width = 2.0f, cap = StrokeCap.Round))
    drawPath(rightBrow, browColor, style = Stroke(width = 2.0f, cap = StrokeCap.Round))
}

/** Layer 5: Expressive Bezier Mouths */
private fun DrawScope.drawMouth(expression: Expression, skinColor: Color) {
    val lipDark = skinColor.darken(0.42f)

    when (expression) {
        Expression.HAPPY -> {
            // Open D-shaped happy grin with interior, teeth, and tongue
            val grin = Path().apply {
                moveTo(41f, 54f)
                lineTo(59f, 54f)
                cubicTo(59f, 63f, 41f, 63f, 41f, 54f)
                close()
            }
            drawPath(grin, Color(0xFF881337))

            // Upper teeth band
            val teeth = Path().apply {
                moveTo(41.5f, 54f)
                lineTo(58.5f, 54f)
                lineTo(58.5f, 56.5f)
                cubicTo(53f, 57.2f, 47f, 57.2f, 41.5f, 56.5f)
                close()
            }
            drawPath(teeth, Color.White)

            // Pink tongue arc
            val tongue = Path().apply {
                moveTo(45f, 63f)
                cubicTo(46f, 59.5f, 54f, 59.5f, 55f, 63f)
                close()
            }
            drawPath(tongue, Color(0xFFFB7185))

            // Corner smile dimple ticks
            val tickLeft = Path().apply { moveTo(39.5f, 53.5f); lineTo(41f, 54.5f) }
            val tickRight = Path().apply { moveTo(60.5f, 53.5f); lineTo(59f, 54.5f) }
            drawPath(tickLeft, skinColor.darken(0.35f), style = Stroke(width = 1.4f, cap = StrokeCap.Round))
            drawPath(tickRight, skinColor.darken(0.35f), style = Stroke(width = 1.4f, cap = StrokeCap.Round))
        }
        Expression.SAD -> {
            // Downward-curved Bezier frown arc
            val frown = Path().apply {
                moveTo(43f, 58.5f)
                cubicTo(47f, 55.5f, 53f, 55.5f, 57f, 58.5f)
            }
            drawPath(frown, lipDark, style = Stroke(width = 1.8f, cap = StrokeCap.Round))
            val cornerL = Path().apply { moveTo(43f, 58.5f); lineTo(42.2f, 59.5f) }
            val cornerR = Path().apply { moveTo(57f, 58.5f); lineTo(57.8f, 59.5f) }
            drawPath(cornerL, lipDark, style = Stroke(width = 1.4f, cap = StrokeCap.Round))
            drawPath(cornerR, lipDark, style = Stroke(width = 1.4f, cap = StrokeCap.Round))
        }
        Expression.ANGRY -> {
            // Tense downturned scowl
            val scowl = Path().apply {
                moveTo(43f, 58f)
                cubicTo(47f, 56f, 53f, 56f, 57f, 57.5f)
            }
            drawPath(scowl, lipDark.darken(0.10f), style = Stroke(width = 2.0f, cap = StrokeCap.Round))
        }
        Expression.SURPRISED -> {
            // Smooth rounded oval 'O' mouth
            val oMouth = Path().apply {
                moveTo(46.5f, 57f)
                cubicTo(46.5f, 53f, 53.5f, 53f, 53.5f, 57f)
                cubicTo(53.5f, 61f, 46.5f, 61f, 46.5f, 57f)
                close()
            }
            drawPath(oMouth, Color(0xFF4C0519))
            drawPath(oMouth, skinColor.darken(0.35f), style = Stroke(width = 1.4f))
        }
        Expression.NEUTRAL -> {
            // Gentle upturned Bezier smile line + soft lower lip shadow
            val smile = Path().apply {
                moveTo(43f, 56f)
                cubicTo(47f, 57.5f, 53f, 57.5f, 57f, 56f)
            }
            drawPath(smile, lipDark, style = Stroke(width = 1.8f, cap = StrokeCap.Round))

            val lowerLip = Path().apply {
                moveTo(47f, 59.5f)
                cubicTo(49f, 60.5f, 51f, 60.5f, 53f, 59.5f)
            }
            drawPath(lowerLip, skinColor.darken(0.18f), style = Stroke(width = 1.2f, cap = StrokeCap.Round))
        }
    }
}

/** Layer 6: Contoured Facial Hair (Gated to age >= 14) */
private fun DrawScope.drawFacialHair(facialHair: FacialHairStyle, hairColor: Color) {
    val beardColor = hairColor.darken(0.08f)

    when (facialHair) {
        FacialHairStyle.STUBBLE -> {
            // Soft jawline contour shadow with subtle stipple
            val stubble = Path().apply {
                moveTo(32f, 56f)
                cubicTo(38f, 66f, 46f, 70.5f, 50f, 70.5f)
                cubicTo(54f, 70.5f, 62f, 66f, 68f, 56f)
                cubicTo(60f, 64f, 40f, 64f, 32f, 56f)
                close()
            }
            drawPath(stubble, hairColor.copy(alpha = 0.22f))
        }
        FacialHairStyle.MUSTACHE -> {
            // Two-wing curved mustache cleanly between nose and upper lip
            val mustache = Path().apply {
                moveTo(50f, 50.5f)
                cubicTo(47f, 49.5f, 42f, 51.5f, 40.5f, 54f)
                cubicTo(43f, 56f, 47f, 54f, 50f, 52.5f)
                cubicTo(53f, 54f, 57f, 56f, 59.5f, 54f)
                cubicTo(58f, 51.5f, 53f, 49.5f, 50f, 50.5f)
                close()
            }
            drawPath(mustache, beardColor)
        }
        FacialHairStyle.GOATEE -> {
            // Mustache + sculpted chin beard patch with open mouth cutout
            val mustache = Path().apply {
                moveTo(50f, 50.5f)
                cubicTo(47f, 49.5f, 42f, 51.5f, 40.5f, 54f)
                cubicTo(43f, 56f, 47f, 54f, 50f, 52.5f)
                cubicTo(53f, 54f, 57f, 56f, 59.5f, 54f)
                cubicTo(58f, 51.5f, 53f, 49.5f, 50f, 50.5f)
                close()
            }
            val chinBeard = Path().apply {
                moveTo(43f, 59f)
                cubicTo(41f, 64f, 45f, 70.5f, 50f, 70.5f)
                cubicTo(55f, 70.5f, 59f, 64f, 57f, 59f)
                cubicTo(54f, 62f, 46f, 62f, 43f, 59f)
                close()
            }
            drawPath(mustache, beardColor)
            drawPath(chinBeard, beardColor)
        }
        FacialHairStyle.BEARD -> {
            // Full jaw and chin beard with clean cutout around mouth
            val fullBeard = Path().apply {
                moveTo(25.5f, 42f)
                cubicTo(26f, 56f, 35f, 68f, 50f, 72f)
                cubicTo(65f, 68f, 74f, 56f, 74.5f, 42f)
                cubicTo(71f, 45f, 68f, 54f, 61f, 58f)
                cubicTo(55f, 61f, 45f, 61f, 39f, 58f)
                cubicTo(32f, 54f, 29f, 45f, 25.5f, 42f)
                close()
            }
            // Add mustache as part of full beard
            val mustache = Path().apply {
                moveTo(50f, 50.5f)
                cubicTo(47f, 49.5f, 42f, 51.5f, 40.5f, 54f)
                cubicTo(43f, 56f, 47f, 54f, 50f, 52.5f)
                cubicTo(53f, 54f, 57f, 56f, 59.5f, 54f)
                cubicTo(58f, 51.5f, 53f, 49.5f, 50f, 50.5f)
                close()
            }
            drawPath(fullBeard, beardColor)
            drawPath(mustache, beardColor)
        }
    }
}

/** Senior Wrinkle Details */
private fun DrawScope.drawSeniorWrinkles(skinColor: Color) {
    val wrinkle = skinColor.darken(0.16f)

    // Forehead worry lines
    val line1 = Path().apply {
        moveTo(38f, 21f)
        cubicTo(44f, 19.5f, 56f, 19.5f, 62f, 21f)
    }
    val line2 = Path().apply {
        moveTo(40f, 24f)
        cubicTo(45f, 22.5f, 55f, 22.5f, 60f, 24f)
    }
    drawPath(line1, wrinkle, style = Stroke(width = 1.0f, cap = StrokeCap.Round))
    drawPath(line2, wrinkle, style = Stroke(width = 1.0f, cap = StrokeCap.Round))

    // Nasolabial laugh lines
    val laughL = Path().apply {
        moveTo(43.5f, 48f)
        cubicTo(40.5f, 53f, 40.5f, 57f, 42f, 60f)
    }
    val laughR = Path().apply {
        moveTo(56.5f, 48f)
        cubicTo(59.5f, 53f, 59.5f, 57f, 58f, 60f)
    }
    drawPath(laughL, wrinkle, style = Stroke(width = 1.1f, cap = StrokeCap.Round))
    drawPath(laughR, wrinkle, style = Stroke(width = 1.1f, cap = StrokeCap.Round))
}

/** Layer 7: Sculpted Front Hairstyles with Specular Sheen */
private fun DrawScope.drawFrontHair(hairStyle: Int, hairColor: Color, skinColor: Color, stage: AgeStage) {
    if (stage == AgeStage.BABY) {
        // Infant override: soft curly baby tuft swirl on crown
        val babyCurl = Path().apply {
            moveTo(50f, 15f)
            cubicTo(52f, 10f, 47f, 7f, 45f, 11f)
            cubicTo(43f, 15f, 48f, 17f, 52f, 16f)
        }
        drawPath(babyCurl, hairColor, style = Stroke(width = 2.2f, cap = StrokeCap.Round))
        drawCircle(hairColor, radius = 0.8f, center = Offset(46f, 16f))
        drawCircle(hairColor, radius = 0.8f, center = Offset(54f, 16f))
        return
    }

    when (hairStyle) {
        0 -> {
            // Clean Buzz / Taper Fade
            val buzz = Path().apply {
                moveTo(26f, 32f)
                cubicTo(26f, 18f, 34f, 10f, 50f, 10f)
                cubicTo(66f, 10f, 74f, 18f, 74f, 32f)
                cubicTo(70f, 24f, 62f, 18f, 50f, 17f)
                cubicTo(38f, 18f, 30f, 24f, 26f, 32f)
                close()
            }
            drawPath(buzz, hairColor)
        }
        1 -> {
            // Side-Part Quiff / Swept Bangs
            val quiff = Path().apply {
                moveTo(25f, 32f)
                cubicTo(25f, 16f, 34f, 7f, 50f, 7f)
                cubicTo(68f, 7f, 76f, 18f, 76f, 30f)
                cubicTo(72f, 25f, 65f, 22f, 56f, 22f)
                cubicTo(45f, 22f, 36f, 26f, 30f, 30f)
                close()
            }
            val swoop = Path().apply {
                moveTo(32f, 20f)
                cubicTo(45f, 13f, 62f, 13f, 68f, 22f)
                cubicTo(56f, 19f, 42f, 21f, 32f, 20f)
                close()
            }
            drawPath(quiff, hairColor)
            drawPath(swoop, hairColor.lighten(0.12f))
        }
        2 -> {
            // Volumetric Textured Afro / Rounded Coils
            val afro = Path().apply {
                moveTo(22f, 38f)
                cubicTo(16f, 25f, 22f, 5f, 40f, 3f)
                cubicTo(45f, 1f, 55f, 1f, 60f, 3f)
                cubicTo(78f, 5f, 84f, 25f, 78f, 38f)
                cubicTo(74f, 25f, 62f, 18f, 50f, 17f)
                cubicTo(38f, 18f, 26f, 25f, 22f, 38f)
                close()
            }
            drawPath(afro, hairColor)
            // Texture coils
            val coilColor = hairColor.darken(0.12f)
            drawCircle(coilColor, radius = 3.0f, center = Offset(36f, 12f), style = Stroke(width = 1.2f))
            drawCircle(coilColor, radius = 3.2f, center = Offset(50f, 8f), style = Stroke(width = 1.2f))
            drawCircle(coilColor, radius = 3.0f, center = Offset(64f, 12f), style = Stroke(width = 1.2f))
        }
        3 -> {
            // Crown Braids / Textured Locs
            val locsFront = Path().apply {
                moveTo(25f, 34f)
                cubicTo(25f, 18f, 35f, 8f, 50f, 8f)
                cubicTo(65f, 8f, 75f, 18f, 75f, 34f)
                cubicTo(70f, 26f, 62f, 20f, 50f, 20f)
                cubicTo(38f, 20f, 30f, 26f, 25f, 34f)
                close()
            }
            drawPath(locsFront, hairColor)
            // Braided parting lines
            val braidColor = hairColor.darken(0.20f)
            val p1 = Path().apply { moveTo(50f, 8f); lineTo(36f, 22f) }
            val p2 = Path().apply { moveTo(50f, 8f); lineTo(46f, 21f) }
            val p3 = Path().apply { moveTo(50f, 8f); lineTo(54f, 21f) }
            val p4 = Path().apply { moveTo(50f, 8f); lineTo(64f, 22f) }
            drawPath(p1, braidColor, style = Stroke(width = 1.4f, cap = StrokeCap.Round))
            drawPath(p2, braidColor, style = Stroke(width = 1.4f, cap = StrokeCap.Round))
            drawPath(p3, braidColor, style = Stroke(width = 1.4f, cap = StrokeCap.Round))
            drawPath(p4, braidColor, style = Stroke(width = 1.4f, cap = StrokeCap.Round))
        }
        4 -> {
            // Sleek Chin-Length Bob with side bangs
            val bobFront = Path().apply {
                moveTo(24f, 36f)
                cubicTo(24f, 16f, 34f, 7f, 50f, 7f)
                cubicTo(66f, 7f, 76f, 16f, 76f, 36f)
                cubicTo(73f, 28f, 64f, 22f, 50f, 22f)
                cubicTo(36f, 22f, 27f, 28f, 24f, 36f)
                close()
            }
            drawPath(bobFront, hairColor)
        }
        5 -> {
            // Long Wavy Curtain Bangs
            val crownCap = Path().apply {
                moveTo(25f, 30f)
                cubicTo(26f, 14f, 35f, 6f, 50f, 6f)
                cubicTo(65f, 6f, 74f, 14f, 75f, 30f)
                cubicTo(70f, 22f, 60f, 18f, 50f, 18f)
                cubicTo(40f, 18f, 30f, 22f, 25f, 30f)
                close()
            }
            val curtainLeft = Path().apply {
                moveTo(50f, 15f)
                cubicTo(44f, 18f, 32f, 25f, 26f, 38f)
                cubicTo(30f, 28f, 40f, 21f, 50f, 19f)
                close()
            }
            val curtainRight = Path().apply {
                moveTo(50f, 15f)
                cubicTo(56f, 18f, 68f, 25f, 74f, 38f)
                cubicTo(70f, 28f, 60f, 21f, 50f, 19f)
                close()
            }
            drawPath(crownCap, hairColor)
            drawPath(curtainLeft, hairColor)
            drawPath(curtainRight, hairColor)
        }
        6 -> {
            // Sleek Pulled-Back Hairline with Baby-Hair Edges
            val sleek = Path().apply {
                moveTo(25f, 32f)
                cubicTo(26f, 20f, 35f, 14f, 50f, 14f)
                cubicTo(65f, 14f, 74f, 20f, 75f, 32f)
                cubicTo(70f, 26f, 60f, 22f, 50f, 22f)
                cubicTo(40f, 22f, 30f, 26f, 25f, 32f)
                close()
            }
            drawPath(sleek, hairColor)
            // Baby-hair edge swirls along temples
            val swirlL = Path().apply { moveTo(29f, 26f); cubicTo(27f, 28f, 28f, 30f, 30f, 29f) }
            val swirlR = Path().apply { moveTo(71f, 26f); cubicTo(73f, 28f, 72f, 30f, 70f, 29f) }
            drawPath(swirlL, hairColor, style = Stroke(width = 1.2f, cap = StrokeCap.Round))
            drawPath(swirlR, hairColor, style = Stroke(width = 1.2f, cap = StrokeCap.Round))
        }
        7 -> {
            // Modern Curly Top / Textured Crop
            val crop = Path().apply {
                moveTo(26f, 33f)
                cubicTo(26f, 18f, 33f, 7f, 50f, 7f)
                cubicTo(67f, 7f, 74f, 18f, 74f, 33f)
                cubicTo(70f, 25f, 62f, 22f, 50f, 22f)
                cubicTo(38f, 22f, 30f, 25f, 26f, 33f)
                close()
            }
            drawPath(crop, hairColor)
            // Wavy curl lobes
            val curlColor = hairColor.lighten(0.12f)
            val curl1 = Path().apply { moveTo(36f, 18f); cubicTo(40f, 14f, 46f, 14f, 48f, 18f) }
            val curl2 = Path().apply { moveTo(48f, 18f); cubicTo(54f, 14f, 60f, 14f, 64f, 18f) }
            drawPath(curl1, curlColor, style = Stroke(width = 1.6f, cap = StrokeCap.Round))
            drawPath(curl2, curlColor, style = Stroke(width = 1.6f, cap = StrokeCap.Round))
        }
    }

    // Specular Hair Sheen Ribbon
    val sheen = Path().apply {
        moveTo(36f, 14f)
        cubicTo(44f, 11f, 56f, 11f, 64f, 14f)
    }
    drawPath(
        path = sheen,
        color = Color.White.copy(alpha = 0.16f),
        style = Stroke(width = 2.4f, cap = StrokeCap.Round)
    )

    // Subtle forehead hairline shadow
    val hairlineShadow = Path().apply {
        moveTo(31f, 24f)
        cubicTo(42f, 22f, 58f, 22f, 69f, 24f)
    }
    drawPath(
        path = hairlineShadow,
        color = skinColor.darken(0.12f),
        style = Stroke(width = 1.0f, cap = StrokeCap.Round)
    )
}

/** Layer 8: Eyewear */
private fun DrawScope.drawEyewear(eyewear: EyewearStyle) {
    val darkSlate = Color(0xFF1E293B)
    val tortoise = Color(0xFF451A03)

    val yPos = if (eyewear == EyewearStyle.READING_GLASSES) 37.5f else 36f
    val lensH = if (eyewear == EyewearStyle.READING_GLASSES) 8f else 10f

    val frameColor = when (eyewear) {
        EyewearStyle.GLASSES -> darkSlate
        EyewearStyle.SUNGLASSES -> Color(0xFF0F172A)
        EyewearStyle.READING_GLASSES -> tortoise
    }

    val leftRect = RoundRect(
        rect = Rect(Offset(30.5f, yPos - lensH / 2f), Size(13f, lensH)),
        cornerRadius = CornerRadius(3.5f, 3.5f)
    )
    val rightRect = RoundRect(
        rect = Rect(Offset(56.5f, yPos - lensH / 2f), Size(13f, lensH)),
        cornerRadius = CornerRadius(3.5f, 3.5f)
    )

    val leftLens = Path().apply { addRoundRect(leftRect) }
    val rightLens = Path().apply { addRoundRect(rightRect) }

    // If Sunglasses: fill dark tinted lenses
    if (eyewear == EyewearStyle.SUNGLASSES) {
        drawPath(leftLens, Color(0xFF0F172A).copy(alpha = 0.88f))
        drawPath(rightLens, Color(0xFF0F172A).copy(alpha = 0.88f))
    }

    // Outer frames
    drawPath(leftLens, frameColor, style = Stroke(width = 1.8f))
    drawPath(rightLens, frameColor, style = Stroke(width = 1.8f))

    // Bridge between frames
    val bridge = Path().apply {
        moveTo(43.5f, yPos)
        cubicTo(47f, yPos - 1.5f, 53f, yPos - 1.5f, 56.5f, yPos)
    }
    drawPath(bridge, frameColor, style = Stroke(width = 1.8f, cap = StrokeCap.Round))

    // Temples extending to ears
    val templeL = Path().apply { moveTo(30.5f, yPos); lineTo(26f, yPos + 2f) }
    val templeR = Path().apply { moveTo(69.5f, yPos); lineTo(74f, yPos + 2f) }
    drawPath(templeL, frameColor, style = Stroke(width = 1.5f, cap = StrokeCap.Round))
    drawPath(templeR, frameColor, style = Stroke(width = 1.5f, cap = StrokeCap.Round))

    // Diagonal glass reflection slash
    val slashL = Path().apply {
        moveTo(40.5f, yPos - lensH / 2f + 2f)
        lineTo(33.5f, yPos + lensH / 2f - 2f)
    }
    val slashR = Path().apply {
        moveTo(66.5f, yPos - lensH / 2f + 2f)
        lineTo(59.5f, yPos + lensH / 2f - 2f)
    }
    drawPath(slashL, Color.White.copy(alpha = 0.30f), style = Stroke(width = 1.3f, cap = StrokeCap.Round))
    drawPath(slashR, Color.White.copy(alpha = 0.30f), style = Stroke(width = 1.3f, cap = StrokeCap.Round))
}

/** Layer 8: Accessories (No Forehead Dots!) */
private fun DrawScope.drawAccessory(accessoryId: Int, stage: AgeStage) {
    when (accessoryId) {
        0 -> {
            // Stylish Beanie / Cap on crown
            val beanie = Path().apply {
                moveTo(25f, 22f)
                cubicTo(25f, 8f, 36f, 3f, 50f, 3f)
                cubicTo(64f, 3f, 75f, 8f, 75f, 22f)
                cubicTo(65f, 18f, 35f, 18f, 25f, 22f)
                close()
            }
            drawPath(beanie, Color(0xFF0F172A))
            // Beanie brim
            val brim = Path().apply {
                moveTo(23f, 22f)
                cubicTo(36f, 19f, 64f, 19f, 77f, 22f)
            }
            drawPath(brim, Color(0xFF1E293B), style = Stroke(width = 2.4f, cap = StrokeCap.Round))
        }
        1 -> {
            // Gold Earring / Stud on actual EAR LOBE (Never on forehead!)
            drawCircle(
                color = Color(0xFFF59E0B),
                radius = 1.6f,
                center = Offset(24f, 48f)
            )
            drawCircle(
                color = Color.White.copy(alpha = 0.9f),
                radius = 0.6f,
                center = Offset(23.6f, 47.6f)
            )
        }
        2 -> {
            // Cute flower / pin clip at temple
            drawCircle(
                color = Color(0xFFF43F5E),
                radius = 2.2f,
                center = Offset(68f, 23f)
            )
            drawCircle(
                color = Color(0xFFFDE047),
                radius = 0.9f,
                center = Offset(68f, 23f)
            )
        }
    }
}
