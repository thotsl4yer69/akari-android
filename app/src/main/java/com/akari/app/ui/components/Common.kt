package com.akari.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.akari.app.ui.theme.AkariColors
import com.akari.app.ui.theme.AkariText

/**
 * Renders a single-path line glyph from an SVG `d` string (24×24 viewport),
 * stroked at 1.6px with round caps/joins — exactly like the prototype icons.
 * Compose's PathParser handles arc (a/A) commands natively.
 */
@Composable
fun PathGlyph(
    pathData: String,
    size: Dp,
    color: Color,
    strokeWidth: Dp = 1.6.dp,
    modifier: Modifier = Modifier,
    filled: Boolean = false,
) {
    val path = remember(pathData) { PathParser().parsePathString(pathData).toPath() }
    Canvas(modifier.size(size)) {
        val s = this.size.minDimension / 24f
        scale(s, s, pivot = androidx.compose.ui.geometry.Offset.Zero) {
            if (filled) {
                drawPath(path, color)
            } else {
                drawPath(
                    path, color,
                    style = Stroke(
                        width = strokeWidth.toPx() / s,
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round,
                    ),
                )
            }
        }
    }
}

/** Tappable with a Button role for TalkBack; press-feedback via [pressScale] where used. */
fun Modifier.clickableRole(role: Role = Role.Button, onClick: () -> Unit): Modifier =
    this.clickable(role = role, onClick = onClick)

/** On-brand pill toggle — sage track when on, sumi/washi knob; instant, no bounce. */
@Composable
fun AkariSwitch(checked: Boolean, onToggle: () -> Unit, motion: Boolean = true, modifier: Modifier = Modifier) {
    val knobX by animateFloatAsState(if (checked) 23f else 3f, tween(if (motion) 180 else 0), label = "knob")
    Box(
        modifier
            .size(width = 48.dp, height = 28.dp)
            .clip(RoundedCornerShape(15.dp))
            .background(if (checked) AkariColors.Sage else AkariColors.Line2)
            .clickableRole(role = Role.Switch) { onToggle() },
    ) {
        Box(
            Modifier
                .offset(x = knobX.dp, y = 3.dp)
                .size(22.dp)
                .clip(CircleShape)
                .background(Color.White),
        )
    }
}

/** UPPERCASE overline eyebrow — tracking .18–.22em, sumi3. */
@Composable
fun Eyebrow(text: String, modifier: Modifier = Modifier, color: Color = AkariColors.Sumi3) {
    Text(
        text = text.uppercase(),
        style = AkariText.Overline,
        color = color,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier,
    )
}

/** On-brand text input — BasicTextField with a placeholder, no M3 underline/purple. */
@Composable
fun AkariTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    textStyle: androidx.compose.ui.text.TextStyle = AkariText.Body,
    textAlign: androidx.compose.ui.text.style.TextAlign = androidx.compose.ui.text.style.TextAlign.Start,
    keyboardType: androidx.compose.ui.text.input.KeyboardType = androidx.compose.ui.text.input.KeyboardType.Text,
    singleLine: Boolean = true,
    color: Color = AkariColors.Sumi,
) {
    androidx.compose.foundation.text.BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        textStyle = textStyle.merge(androidx.compose.ui.text.TextStyle(color = color, textAlign = textAlign)),
        singleLine = singleLine,
        cursorBrush = androidx.compose.ui.graphics.SolidColor(AkariColors.Sumi),
        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = keyboardType),
        decorationBox = { inner ->
            val align = when (textAlign) {
                androidx.compose.ui.text.style.TextAlign.Center -> androidx.compose.ui.Alignment.Center
                androidx.compose.ui.text.style.TextAlign.End -> androidx.compose.ui.Alignment.CenterEnd
                else -> androidx.compose.ui.Alignment.CenterStart
            }
            androidx.compose.foundation.layout.Box(contentAlignment = align) {
                if (value.isEmpty()) {
                    Text(placeholder, style = textStyle.merge(androidx.compose.ui.text.TextStyle(textAlign = textAlign)), color = AkariColors.Sumi3, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                inner()
            }
        },
    )
}

/**
 * Press-to-scale tap target: primary buttons scale to 0.98, log tiles 0.97,
 * hue swatches 0.92 — instant, no bounce. Observes presses without consuming
 * the gesture, so a sibling [clickableRole] still fires the tap. Falls back to
 * instant scale when motion is disabled.
 */
@Composable
fun Modifier.pressScale(pressedScale: Float, motion: Boolean = true): Modifier {
    var pressed by remember { androidx.compose.runtime.mutableStateOf(false) }
    val scaleV by animateFloatAsState(
        targetValue = if (pressed) pressedScale else 1f,
        animationSpec = tween(if (motion) 90 else 0),
        label = "pressScale",
    )
    return this
        .graphicsLayer { scaleX = scaleV; scaleY = scaleV }
        .pointerInput(Unit) {
            awaitEachGesture {
                awaitFirstDown(requireUnconsumed = false)
                pressed = true
                waitForUpOrCancellation()
                pressed = false
            }
        }
}
