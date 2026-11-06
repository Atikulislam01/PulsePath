package com.pulsepath.app.screens

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.unit.Dp
import com.pulsepath.app.ui.theme.BackgroundWhite
import com.pulsepath.app.ui.theme.NavyHeading
import com.pulsepath.app.ui.theme.ThemeState

/** The main color of the screen that is showing. The navigation sets it for every screen. */
val LocalScreenAccent = compositionLocalOf { Color(0xFF7B61FF) }

/** The title color: the screen color mixed with the normal heading color. */
val screenTitleColor: Color
    @Composable get() = lerp(LocalScreenAccent.current, NavyHeading, 0.35f)

/** The screen background with a soft glow of the screen color at the top. */
fun Modifier.screenBackground(): Modifier = composed {
    val accent = LocalScreenAccent.current
    val base = BackgroundWhite
    val glow = accent
        .copy(alpha = if (ThemeState.isDark) 0.30f else 0.22f)
        .compositeOver(base)

    this.background(
        Brush.verticalGradient(
            colorStops = arrayOf(0f to glow, 0.35f to base, 1f to base)
        )
    )
}

/** A card border in the screen color, with a very light tint inside. */
@Suppress("UNUSED_PARAMETER")
fun Modifier.cardBorder(width: Dp, color: Color, shape: Shape): Modifier = composed {
    val accent = LocalScreenAccent.current
    val tint = accent.copy(alpha = if (ThemeState.isDark) 0.10f else 0.05f)

    this
        .background(tint, shape)
        .border(width, accent.copy(alpha = 0.40f), shape)
}

/** Like clickable, but shrinks a little while pressed and bounces back. */
fun Modifier.pressable(onClick: () -> Unit): Modifier = composed {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.97f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "pressScale"
    )

    // The clickable call below has no dot in front of it on purpose,
    // so the Find & Replace in step 4 does not change it.
    this.scale(scale).run {
        clickable(
            interactionSource = interaction,
            indication = null,
            onClick = onClick
        )
    }
}