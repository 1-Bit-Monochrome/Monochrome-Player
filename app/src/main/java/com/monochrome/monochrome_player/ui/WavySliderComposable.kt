@file:JvmName("WavySliderKt")
package com.monochrome.monochrome_player.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.sin

/**
 * Lightweight WavySlider-like composable.
 * Not a perfect 1:1 replacement for 3rd-party library but visually similar and easy to theme.
 */
@Composable
fun WavySliderHost(
    value: Float,
    onValueChange: java.util.function.Consumer<Float>,
    enabled: Boolean = true,
    waveLength: Dp = 14.dp,
    waveHeight: Dp = 34.dp,
    waveVelocity: Pair<Float, WaveDirection> = 34f to WaveDirection.TAIL,
    waveThickness: Dp = 4.dp,
    trackThickness: Dp = 13.dp,
    incremental: Boolean = true,
    animationSpecs: Any? = null // kept for API compatibility with snippet
) {
    val primary = MaterialTheme.colorScheme.primary
    val surface = MaterialTheme.colorScheme.surfaceVariant

    val density = LocalDensity.current
    val waveLenPx = with(density) { waveLength.toPx() }
    val waveHtPx = with(density) { waveHeight.toPx() }

    val infiniteTransition = rememberInfiniteTransition()
    val phase = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = (1000L).toInt(), easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    val progress = remember { mutableStateOf(value.coerceIn(0f, 1f)) }
    LaunchedEffect(value) { progress.value = value.coerceIn(0f, 1f) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height((waveHeight + 24.dp))
            .padding(horizontal = 12.dp)
            .pointerInput(enabled) {
                detectTapGestures { tap ->
                    // compute new value based on tap x
                    val w = size.width
                    val new = (tap.x / w).coerceIn(0f, 1f)
                    onValueChange.accept(new)
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxWidth()) {
            val w = size.width
            val h = size.height
            val cy = h / 2f

            // background track
            drawRoundRect(
                color = surface,
                topLeft = Offset(0f, cy - trackThickness.toPx() / 2f),
                size = androidx.compose.ui.geometry.Size(w, trackThickness.toPx()),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(trackThickness.toPx() / 2f, trackThickness.toPx() / 2f)
            )

            // wave path
            val path = Path()
            val phaseShift = phase.value * waveLenPx * 2f
            val steps = (w / 4).toInt().coerceAtLeast(50)
            for (i in 0..steps) {
                val x = i * (w / steps.toFloat())
                val theta = (x + phaseShift) / waveLenPx * 2f * PI.toFloat()
                val y = cy + sin(theta) * waveHtPx / 2f
                if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }

            drawPath(path = path, color = primary, style = androidx.compose.ui.graphics.drawscope.Stroke(width = waveThickness.toPx()))

            // thumb
            val thumbX = progress.value * w
            val thumbY = cy
            drawCircle(color = primary, radius = (trackThickness.toPx() * 0.9f), center = Offset(thumbX, thumbY))
        }
    }
}

enum class WaveDirection { HEAD, TAIL }

@Composable
fun WavySliderSimple(value: Float, onValueChange: java.util.function.Consumer<Float>) {
    WavySliderHost(value = value, onValueChange = onValueChange)
}
