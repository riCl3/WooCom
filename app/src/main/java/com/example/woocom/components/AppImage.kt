package com.example.woocom.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.woocom.R

/**
 * The app's single image entry point.
 *
 * Every one of the 11 `AsyncImage` call sites used to be configured independently, which
 * meant loading flashed no placeholder and failed URLs rendered as an empty box. Wrapping
 * the loader here keeps the crossfade, placeholder and error drawable consistent and
 * makes a future switch to Coil 3 a one-file change.
 */
@Composable
fun AppImage(
    model: Any?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit,
) {
    val placeholder = painterResource(R.drawable.ic_image_placeholder)

    AsyncImage(
        model =
            ImageRequest.Builder(LocalContext.current)
                .data(model)
                .crossfade(true)
                .build(),
        placeholder = placeholder,
        error = placeholder,
        fallback = placeholder,
        contentDescription = contentDescription,
        modifier = modifier,
        contentScale = contentScale,
    )
}
