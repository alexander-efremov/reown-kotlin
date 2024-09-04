package com.walletconnect.web3.modal.ui.components.internal.commons

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.walletconnect.web3.modal.ui.components.internal.walletconnect.WalletConnectLogo
import com.walletconnect.web3.modal.ui.previews.MultipleComponentsPreview
import com.walletconnect.web3.modal.ui.previews.UiModePreview
import com.walletconnect.web3.modal.ui.theme.AppKitTheme

@Composable
internal fun ListSelectRow(
    startIcon: @Composable (Boolean) -> Unit,
    text: String,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    label: (@Composable (Boolean) -> Unit)? = null,
    isEnabled: Boolean = true,
    onClick: () -> Unit
) {
    val background: Color
    val textColor: Color
    if (isEnabled) {
        background = AppKitTheme.colors.grayGlass02
        textColor = AppKitTheme.colors.foreground.color100
    } else {
        background = AppKitTheme.colors.grayGlass15
        textColor = AppKitTheme.colors.foreground.color300
    }
    Surface(
        color = Color.Transparent,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.padding(contentPadding)
    ) {
        Row(
            modifier = modifier
                .clickable(onClick = onClick)
                .height(56.dp)
                .background(background)
                .padding(horizontal = 8.dp)
                .clipToBounds(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            startIcon(isEnabled)
            HorizontalSpacer(width = 10.dp)
            Text(
                text = text,
                style = AppKitTheme.typo.paragraph500.copy(textColor),
                modifier = Modifier.weight(1f)
            )
            label?.let {
                HorizontalSpacer(width = 10.dp)
                it.invoke(isEnabled)
                HorizontalSpacer(width = 8.dp)
            }
        }
    }
}

@UiModePreview
@Composable
private fun ListSelectRowPreview() {
    MultipleComponentsPreview(
        { ListSelectRow(startIcon = { WalletConnectLogo(it) }, text = "WalletConnect") {} },
        { ListSelectRow(startIcon = { WalletConnectLogo(it) }, text = "WalletConnect", isEnabled = false) {} },
        { ListSelectRow(startIcon = { WalletConnectLogo(it) }, text = "WalletConnect", label = { InstalledLabel() }) {} },
    )
}