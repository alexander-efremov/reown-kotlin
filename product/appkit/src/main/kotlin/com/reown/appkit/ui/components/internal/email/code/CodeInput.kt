package com.reown.appkit.ui.components.internal.email.code

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.reown.util.Empty
import com.reown.appkit.ui.theme.AppKitTheme

@Composable
internal fun CodeInput(codeInputState: CodeInputState) {
    val focusRequester = remember { FocusRequester() }
    val state by codeInputState.state.collectAsState()

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    BasicTextField(
        value = state.text,
        onValueChange = codeInputState::onTextChange,
        textStyle = AppKitTheme.typo.paragraph400.copy(color = AppKitTheme.colors.foreground.color100),
        cursorBrush = SolidColor(AppKitTheme.colors.accent100),
        singleLine = true,
        keyboardActions = KeyboardActions { codeInputState.submit(state.text) },
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Done,
            keyboardType = KeyboardType.NumberPassword
        ),
        modifier = Modifier
            .height(54.dp)
            .onFocusChanged { codeInputState.onFocusChange(it.hasFocus) }
            .focusRequester(focusRequester),
        decorationBox = { _ ->
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                for (i in 0..5) {
                    CodeDigitText(
                        value = state.text.getOrNull(i)?.toString() ?: String.Empty,
                        isFocused = i == state.text.length && state.isFocused
                    )
                }
            }
        }
    )
}

@Composable
private fun CodeDigitText(
    value: String = String.Empty,
    isFocused: Boolean,
) {
    val shadowModifier: Modifier
    val background: Color
    val borderColor: Color

    if (isFocused) {
        background = AppKitTheme.colors.grayGlass10
        borderColor = AppKitTheme.colors.accent100
        shadowModifier = Modifier
            .size(54.dp)
            .border(4.dp, AppKitTheme.colors.accent20, RoundedCornerShape(20.dp))
            .padding(4.dp)
    } else {
        background = AppKitTheme.colors.grayGlass05
        borderColor = AppKitTheme.colors.grayGlass05
        shadowModifier = Modifier
            .size(54.dp)
            .padding(4.dp)
    }


    Box(modifier = shadowModifier) {
        Box(
            modifier = Modifier
                .size(50.dp)
                .border(1.dp, borderColor, RoundedCornerShape(16.dp))
                .background(background, RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            if (isFocused) {
                val infiniteTransition = rememberInfiniteTransition(label = "Indicator animation")
                val blinkAnimation by infiniteTransition.animateFloat(
                    initialValue = 0f,
                    targetValue = 1f,
                    label = "Indicator",
                    animationSpec = infiniteRepeatable(animation = tween(500), repeatMode = RepeatMode.Reverse),
                )
                AnimatedVisibility(visible = blinkAnimation > .5f, enter = fadeIn(), exit = fadeOut()) {
                    Box(
                        modifier = Modifier
                            .size(2.dp, 20.dp)
                            .background(AppKitTheme.colors.accent100)
                    )
                }
            } else {
                Text(
                    text = value,
                    style = AppKitTheme.typo.large400.copy(color = AppKitTheme.colors.grayGlass)
                )
            }
        }
    }
}
