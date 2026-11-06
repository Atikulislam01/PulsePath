package com.pulsepath.app.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp

/**
 * Shows the picture from res/drawable with the given name (for example "welcome_illustration").
 * If that file doesn't exist, the fallback (the old placeholder) is shown instead.
 * With floating = true the picture drifts gently up and down.
 */
@Composable
fun ArtImage(
    name: String,
    modifier: Modifier = Modifier,
    floating: Boolean = false,
    fallback: @Composable () -> Unit
) {
    val context = LocalContext.current
    val resId = remember(name) {
        context.resources.getIdentifier(name, "drawable", context.packageName)
    }

    val transition = rememberInfiniteTransition(label = "float")
    val drift by transition.animateFloat(
        initialValue = -7f,
        targetValue = 7f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "drift"
    )

    if (resId != 0) {
        Image(
            painter = painterResource(resId),
            contentDescription = null,
            modifier = if (floating) {
                modifier.offset { IntOffset(0, drift.dp.roundToPx()) }
            } else {
                modifier
            },
            contentScale = ContentScale.Fit
        )
    } else {
        fallback()
    }
}

/**
 * A big picture that fills the free space in a Column. It floats gently.
 * Use it inside a Column, where "weight" is allowed.
 */
@Composable
fun ColumnScope.ArtImageFill(
    name: String,
    fallback: @Composable (Modifier) -> Unit
) {
    val fillModifier = Modifier
        .fillMaxWidth()
        .weight(1f)

    ArtImage(name = name, modifier = fillModifier, floating = true) {
        fallback(fillModifier)
    }
}