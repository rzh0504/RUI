package rui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePainter
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.crossfade
import rui.foundation.RuiIcon
import rui.foundation.RuiIcons
import rui.theme.RuiTheme

/**
 * An image that loads its content on Coil — typically from a network URL,
 * though any model the loader understands works. While the request is in
 * flight a breathing [RuiSkeleton] stands in; a failure swaps in a muted
 * placeholder. Both slots are overridable for content-shaped loading states
 * or actionable errors (a retry button, say).
 *
 * Requests are cancelled with composition and results are cached by the
 * loader — memory and, with a network fetcher on the classpath, disk.
 *
 * @param model The image to load: a URL string, a file path, or an
 *   [ImageRequest] for full control (headers, transformations, cache keys).
 *   An [ImageRequest] keeps all of its configuration; only [crossfade] is
 *   applied on top of it.
 * @param contentScale How the loaded image fits the layout bounds — `Crop`
 *   by default, so photos fill cards and avatars without letterboxing.
 * @param crossfade Fades the image in over the placeholder on success.
 * @param imageLoader The loader to execute requests with; `null` uses Coil's
 *   process-wide singleton.
 * @param placeholder Shown while loading; defaults to a [RuiSkeleton] bone
 *   filling the bounds.
 * @param error Shown when the request fails; defaults to a muted fill with
 *   a close glyph.
 */
@Composable
public fun RuiAsyncImage(
    model: Any?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    crossfade: Boolean = true,
    imageLoader: ImageLoader? = null,
    placeholder: (@Composable () -> Unit)? = null,
    error: (@Composable () -> Unit)? = null,
) {
    val context = LocalPlatformContext.current
    val loader = imageLoader ?: SingletonImageLoader.get(context)
    val request = remember(model, crossfade, context) {
        when (model) {
            is ImageRequest -> model.newBuilder()
                .crossfade(enable = crossfade)
                .build()
            else -> ImageRequest.Builder(context)
                .data(model)
                .crossfade(enable = crossfade)
                .build()
        }
    }
    var state by remember { mutableStateOf<AsyncImagePainter.State>(AsyncImagePainter.State.Empty) }

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        AsyncImage(
            model = request,
            contentDescription = contentDescription,
            imageLoader = loader,
            modifier = Modifier.matchParentSize(),
            contentScale = contentScale,
            transform = { it },
            onState = { state = it },
        )
        when (state) {
            is AsyncImagePainter.State.Success -> Unit
            is AsyncImagePainter.State.Error ->
                error?.invoke() ?: DefaultErrorPlaceholder(Modifier.matchParentSize())
            else -> placeholder?.invoke() ?: RuiSkeleton(Modifier.matchParentSize())
        }
    }
}

@Composable
private fun DefaultErrorPlaceholder(modifier: Modifier) {
    val colors = RuiTheme.colors
    Box(
        modifier = modifier.background(colors.surfaceMuted),
        contentAlignment = Alignment.Center,
    ) {
        RuiIcon(
            imageVector = RuiIcons.Close,
            contentDescription = null,
            tint = colors.contentMuted,
        )
    }
}
