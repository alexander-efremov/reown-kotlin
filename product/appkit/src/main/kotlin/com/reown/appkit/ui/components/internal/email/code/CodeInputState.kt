package com.reown.appkit.ui.components.internal.email.code

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.platform.LocalFocusManager
import com.reown.appkit.ui.components.internal.commons.inputs.InputState
import kotlinx.coroutines.CoroutineScope

@Composable
internal fun rememberCodeInputState(
    coroutineScope: CoroutineScope = rememberCoroutineScope(),
    focusManager: FocusManager = LocalFocusManager.current,
    onSubmit: (String) -> Unit
) = remember(coroutineScope, focusManager) {
    CodeInputState(coroutineScope, focusManager, onSubmit)
}

internal class CodeInputState(
    coroutineScope: CoroutineScope,
    focusManager: FocusManager,
    private val onSubmit: (String) -> Unit
) : InputState(coroutineScope, focusManager) {

    override fun onTextChange(value: String) {
        if (value.length <= 6) {
            super.onTextChange(value)
            if (value.length == 6) {
                clearFocus()
                submit(value)
            }
        }
    }

    override fun submit(text: String) {
        onSubmit(text)
    }
}