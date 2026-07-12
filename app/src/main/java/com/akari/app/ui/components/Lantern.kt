package com.akari.app.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.akari.app.ui.theme.AkariColors
import com.akari.app.ui.theme.LocalLanternHue
import kotlin.math.roundToInt

enum class LanternSize { Hero, Onboard, Mini }

/** Pure ellipse — the chōchin's vertically-oval body (CSS 46%/44% radii). */
private val OvalShape = object : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density) =
        Outline.Generic(Path().apply { addOval(androidx.compose.ui.geometry.Rect(Offset.Zero, size)) })
}
private val CapTopShape = RoundedCornerShape(topStart = 5.dp, topEnd = 5.dp, bottomStart = 3.dp, bottomEnd = 3.dp)
private val CapBottomShape = RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp, bottomStart = 5.dp, bottomEnd = 5.dp)

private data class LanternDims(
    val bodyW: Dp, val bodyH: Dp,
    val topCapW: Dp, val topCapH: Dp,
    val bottomCapW: Dp, val bottomCapH: Dp,
    val cordH: Dp, val haloSize: Dp,
    val caps: Boolean,
)

private fun dimsFor(size: LanternSize) = when (size) {
    LanternSize.Hero -> LanternDims(172.dp, 216.dp, 50.dp, 12.dp, 42.dp, 11.dp, 16.dp, 330.dp, true)
    LanternSize.Onboard -> LanternDims(150.dp, 190.dp, 46.dp, 11.dp, 38.dp, 10.dp, 16.dp, 280.dp, true)
    LanternSize.Mini -> LanternDims(30.dp, 38.dp, 0.dp, 0.dp, 0.dp, 0.dp, 0.dp, 0.dp, false)
}

private val PaperCenter = Color(0xFFFDF4E2)
private val PaperMid = Color(0xFFF0DCAE)
private val PaperEdge = Color(0xFFE6CE99)
private val WoodTop = Color(0xFF3A342B)
private val WoodBottom = Color(0xFF2C2820)
private val BambooRing = Color(0x1C462D0F) // rgba(70,45,15,.11)
private val BarrelShade = Color(0x335A370F) // rgba(90,55,15,.20)

/**
 * The paper lantern (chōchin) — the emotional core. Brightness tracks
 * `energy` (0..100): glow overlay opacity = 0.28 + e·0.68, halo scale =
 * 0.55 + e·0.68, halo opacity = 0.10 + e·0.5. Hue flows from
 * [LocalLanternHue] so every lantern in the app recolors together.
 */
@Composable
fun Lantern(
    energy: Float,
    startEnergy: Float,
    modifier: Modifier = Modifier,
    size: LanternSize = LanternSize.Hero,
    motion: Boolean = LocalMotionEnabled.current,
) {
    val d = dimsFor(size)
    val hue = LocalLanternHue.current

    val e by animateFloatAsState(
        targetValue = (energy.coerceIn(0f, 100f)) / 100f,
        animationSpec = tween(durationMillis = if (motion) 300 else 0),
        label = "energy",
    )

    val infinite = rememberInfiniteTransition(label = "lantern")
    val breatheAnim = if (motion) infinite.animateFloat(
        1f, 1.014f, infiniteRepeatable(tween(6500), RepeatMode.Reverse), label = "breathe",
    ) else null
    val flickAnim = if (motion) infinite.animateFloat(
        0.85f, 1.08f, infiniteRepeatable(tween(3400), RepeatMode.Reverse), label = "flicker",
    ) else null
    val breathe = breatheAnim?.value ?: 1f
    val flick = flickAnim?.value ?: 1f

    val glowAlpha = (0.28f + e * 0.68f).coerceIn(0f, 1f)
    val haloScale = 0.55f + e * 0.68f
    val haloAlpha = (0.10f + e * 0.5f).coerceIn(0f, 1f)

    val label = "Lantern — ${energy.roundToInt()} of ${startEnergy.roundToInt()} light left"

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.semantics { contentDescription = label },
    ) {
        if (d.caps) {
            Box(Modifier.width(1.dp).height(d.cordH).background(AkariColors.Line2))
            Box(
                Modifier.size(d.topCapW, d.topCapH).clip(CapTopShape)
                    .background(Brush.verticalGradient(listOf(WoodTop, WoodBottom))),
            )
        }

        Box(contentAlignment = Alignment.Center) {
            if (d.haloSize > 0.dp) {
                Canvas(
                    Modifier.size(d.haloSize).graphicsLayer {
                        scaleX = haloScale; scaleY = haloScale; alpha = haloAlpha
                    },
                ) {
                    drawCircle(
                        brush = Brush.radialGradient(
                            0f to hue, 0.66f to hue.copy(alpha = 0f),
                            center = center, radius = this.size.minDimension / 2f,
                        ),
                    )
                }
            }
            LanternBody(d.bodyW, d.bodyH, hue, glowAlpha, flick, breathe, mini = size == LanternSize.Mini)
        }

        if (d.caps) {
            Box(
                Modifier.size(d.bottomCapW, d.bottomCapH).clip(CapBottomShape)
                    .background(Brush.verticalGradient(listOf(WoodBottom, WoodTop))),
            )
            Box(Modifier.width(2.dp).height(13.dp).background(AkariColors.Ember.copy(alpha = 0.7f)))
            Box(
                Modifier.offset(y = (-2).dp).size(9.dp).clip(CircleShape)
                    .background(AkariColors.Ember.copy(alpha = 0.7f)),
            )
        }
    }
}

@Composable
private fun LanternBody(
    width: Dp, height: Dp,
    hue: Color, glowAlpha: Float, flameScale: Float, breatheScale: Float, mini: Boolean,
) {
    Canvas(
        Modifier
            .size(width, height)
            .graphicsLayer { scaleX = breatheScale; scaleY = breatheScale }
            .clip(OvalShape),
    ) {
        val w = size.width
        val h = size.height

        // 1) paper base — radial from 50%,44%
        drawRect(
            brush = Brush.radialGradient(
                0f to PaperCenter, 0.60f to PaperMid, 1f to PaperEdge,
                center = Offset(w * 0.5f, h * 0.44f), radius = maxOf(w, h) * 0.62f,
            ),
        )

        // 2) glow overlay — the energy signal, hue → transparent at 72%
        drawRect(
            brush = Brush.radialGradient(
                0f to hue.copy(alpha = glowAlpha), 0.72f to hue.copy(alpha = 0f),
                center = Offset(w * 0.5f, h * 0.46f), radius = maxOf(w, h) * 0.6f,
            ),
        )

        if (!mini) {
            // 3) flame hotspot — small white-amber radial at ~47%, flickers
            val flameW = w * 0.37f * flameScale
            val flameH = h * 0.41f * flameScale
            drawOval(
                brush = Brush.radialGradient(
                    0f to Color(0xFFFFF8E9), 0.7f to Color(0x00FFF0C8),
                    center = Offset(w * 0.5f, h * 0.47f), radius = maxOf(flameW, flameH) / 1.4f,
                ),
                topLeft = Offset(w * 0.5f - flameW / 2f, h * 0.47f - flameH / 2f),
                size = Size(flameW, flameH),
            )

            // 4) bamboo rings — horizontal lines every ~14px
            val ring = 14.dp.toPx()
            var y = ring
            while (y < h) {
                drawRect(color = BambooRing, topLeft = Offset(0f, y), size = Size(w, 1.dp.toPx()))
                y += ring
            }

            // 5) barrel shading — darken left/right edges (~22% inset)
            drawRect(
                brush = Brush.horizontalGradient(
                    0f to BarrelShade, 0.22f to Color.Transparent,
                    0.78f to Color.Transparent, 1f to BarrelShade,
                ),
            )
        }

        // 6) inset rim
        drawOval(
            color = Color(0x29785022),
            topLeft = Offset.Zero, size = Size(w, h),
            style = Stroke(width = 1.dp.toPx()),
        )
    }
}
