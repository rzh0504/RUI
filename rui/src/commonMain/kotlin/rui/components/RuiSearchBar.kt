package rui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import rui.foundation.RuiIcon
import rui.foundation.RuiIcons
import rui.foundation.RuiText
import rui.foundation.rememberRuiIndication
import rui.theme.RuiTheme

/**
 * The Rui search bar: a [RuiTextField] tuned for search — leading magnifier,
 * search IME action, a clear button once text is entered, and an optional
 * cancel action that empties the field and drops focus, iOS style. Filtering
 * the content stays with the caller ([TextFieldState] is hoisted).
 *
 * @param state Hoisted query text.
 * @param placeholder Shown while the field is empty.
 * @param cancelLabel When non-null, a plain cancel button sits after the
 *   field; tapping it clears the query and focus, then calls [onCancel].
 * @param onCancel Called after the cancel button has cleared the field.
 */
@Composable
public fun RuiSearchBar(
    state: TextFieldState,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    placeholder: String? = null,
    cancelLabel: String? = null,
    onCancel: (() -> Unit)? = null,
    interactionSource: MutableInteractionSource? = null,
) {
    val colors = RuiTheme.colors
    val spacing = RuiTheme.spacing
    val focusManager = LocalFocusManager.current
    val resolvedInteractionSource = interactionSource ?: remember { MutableInteractionSource() }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RuiTextField(
            state = state,
            modifier = Modifier.weight(1f),
            enabled = enabled,
            placeholder = placeholder,
            leading = {
                RuiIcon(
                    imageVector = RuiIcons.Search,
                    contentDescription = null,
                    tint = colors.contentMuted,
                )
            },
            trailing = if (state.text.isNotEmpty()) {
                {
                    ClearButton(
                        enabled = enabled,
                        onClick = { state.edit { replace(0, length, "") } },
                    )
                }
            } else {
                null
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            interactionSource = resolvedInteractionSource,
        )
        if (cancelLabel != null) {
            RuiButton(
                onClick = {
                    state.edit { replace(0, length, "") }
                    focusManager.clearFocus()
                    onCancel?.invoke()
                },
                variant = RuiButtonVariant.Plain,
                enabled = enabled,
            ) {
                RuiText(text = cancelLabel)
            }
        }
    }
}

/**
 * Icon-sized clear affordance: the same 20dp footprint as the magnifier, so
 * its appearance never changes the field's height (a full icon button would
 * push the row past the touch target).
 */
@Composable
private fun ClearButton(enabled: Boolean, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val clearLabel = RuiTheme.strings.clear
    Box(
        modifier = Modifier
            .size(RuiTheme.dimens.iconSize)
            .clip(CircleShape)
            .clickable(
                interactionSource = interactionSource,
                indication = rememberRuiIndication(),
                enabled = enabled,
                onClickLabel = clearLabel,
                role = Role.Button,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        RuiIcon(
            imageVector = RuiIcons.Close,
            contentDescription = clearLabel,
            tint = if (enabled) RuiTheme.colors.contentMuted else RuiTheme.colors.contentDisabled,
        )
    }
}
