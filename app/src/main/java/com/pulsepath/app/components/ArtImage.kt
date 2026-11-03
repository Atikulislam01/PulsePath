package com.pulsepath.app.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource

/**
 * Shows the picture from res/drawable with the given name (for example "welcome_illustration").
 * If that file doesn't exist, the fallback (the old placeholder) is shown instead.
 */
@Composable
fun ArtImage(
    name: String,
    modifier: Modifier = Modifier,
    fallback: @Composable () -> Unit
) {
    val context = LocalContext.current
    val resId = remember(name) {
        context.resources.getIdentifier(name, "drawable", context.packageName)
    }

    if (resId != 0) {
        Image(
            painter = painterResource(resId),
            contentDescription = null,
            modifier = modifier,
            contentScale = ContentScale.Fit
        )
    } else {
        fallback()
    }
}

/**
 * A big picture that fills the free space in a Column.
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

    ArtImage(name = name, modifier = fillModifier) {
        fallback(fillModifier)
    }
}