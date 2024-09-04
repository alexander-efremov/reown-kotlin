package com.walletconnect.web3.modal.ui.components.internal.commons

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.walletconnect.web3.modal.ui.previews.MultipleComponentsPreview
import com.walletconnect.web3.modal.ui.previews.UiModePreview
import com.walletconnect.web3.modal.ui.theme.AppKitTheme

@Composable
internal fun AllLabel(isEnabled: Boolean = true) {
    ListLabel(text = "ALL", isEnabled = isEnabled)
}

@Composable
internal fun TextLabel(text: String, isEnabled: Boolean = true) {
    ListLabel(
        text = text,
        isEnabled = isEnabled,
        backgroundColor = AppKitTheme.colors.grayGlass10,
        labelTextColor = AppKitTheme.colors.foreground.color150
    )
}

@Composable
internal fun GetWalletLabel(isEnabled: Boolean = true) {
    ListLabel(text = "GET WALLET", isEnabled = isEnabled)
}

@Composable
internal fun RecentLabel(isEnabled: Boolean = true) {
    ListLabel(
        text = "RECENT",
        isEnabled = isEnabled,
        backgroundColor = AppKitTheme.colors.grayGlass10,
        labelTextColor = AppKitTheme.colors.foreground.color150
    )
}

@Composable
internal fun InstalledLabel(isEnabled: Boolean = true) {
    ListLabel(
        text = "INSTALLED",
        isEnabled = isEnabled,
        backgroundColor = AppKitTheme.colors.success15,
        labelTextColor = AppKitTheme.colors.success
    )
}

@Composable
private fun ListLabel(
    text: String,
    isEnabled: Boolean,
    backgroundColor: Color = AppKitTheme.colors.accent15,
    labelTextColor: Color = AppKitTheme.colors.accent100
) {
    val textColor: Color
    val background: Color
    if (isEnabled) {
        background = backgroundColor
        textColor = labelTextColor
    } else {
        background = AppKitTheme.colors.grayGlass10
        textColor = AppKitTheme.colors.foreground.color300
    }
    Box(
        modifier = Modifier
            .height(20.dp)
            .background(background, shape = RoundedCornerShape(4.dp))
            .padding( horizontal = 5.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, style = AppKitTheme.typo.micro700.copy(textColor))
    }
}

@Composable
@UiModePreview
private fun AllLabelPreview() {
    MultipleComponentsPreview(
        { AllLabel() },
        { AllLabel(false) },
    )
}

@Composable
@UiModePreview
private fun TextLabelPreview() {
    MultipleComponentsPreview(
        { TextLabel("240+") },
        { TextLabel("240+", false) },
    )
}

@Composable
@UiModePreview
private fun GetWalletLabelPreview() {
    MultipleComponentsPreview(
        { GetWalletLabel() },
        { GetWalletLabel(false) },
    )
}

@Composable
@UiModePreview
private fun RecentLabelPreview() {
    MultipleComponentsPreview(
        { RecentLabel() },
        { RecentLabel(false) },
    )
}

@Composable
@UiModePreview
private fun InstalledLabelPreview() {
    MultipleComponentsPreview(
        { InstalledLabel() },
        { InstalledLabel(false) },
    )
}

