package com.walletconnect.web3.modal.ui.components.internal.commons

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import com.walletconnect.modal.ui.components.common.roundedClickable
import com.walletconnect.web3.modal.R
import com.walletconnect.web3.modal.ui.previews.MultipleComponentsPreview
import com.walletconnect.web3.modal.ui.previews.UiModePreview
import com.walletconnect.web3.modal.ui.theme.Web3ModalTheme

@Composable
internal fun BackArrowIcon(
    tint: Color = Web3ModalTheme.colors.foreground.color100,
    onClick: () -> Unit
) {
    Icon(
        imageVector = ImageVector.vectorResource(id = R.drawable.ic_chevron_left),
        contentDescription = ContentDescription.BACK_ARROW.description,
        tint = tint,
        modifier = Modifier
            .size(36.dp)
            .roundedClickable(onClick = onClick)
            .padding(10.dp),
    )
}

@Composable
internal fun QuestionMarkIcon(
    tint: Color = Web3ModalTheme.colors.foreground.color100,
    onClick: () -> Unit
) {
    Icon(
        imageVector = ImageVector.vectorResource(id = R.drawable.ic_question_mark),
        contentDescription = ContentDescription.QUESTION_MARK.description,
        tint = tint,
        modifier = Modifier
            .size(36.dp)
            .roundedClickable(onClick = onClick)
            .padding(10.dp),
    )
}

@Composable
internal fun CloseIcon(
    tint: Color = Web3ModalTheme.colors.foreground.color100,
    onClick: () -> Unit
) {
    Icon(
        imageVector = ImageVector.vectorResource(id = R.drawable.ic_close),
        contentDescription = ContentDescription.CLOSE.description,
        tint = tint,
        modifier = Modifier
            .size(36.dp)
            .roundedClickable(onClick = onClick)
            .padding(10.dp),
    )
}

@Composable
internal fun RetryIcon(
    tint: Color = Web3ModalTheme.colors.inverse100
) {
    Icon(
        imageVector = ImageVector.vectorResource(id = R.drawable.ic_retry),
        contentDescription = ContentDescription.RETRY.description,
        tint = tint,
        modifier = Modifier.size(12.dp),
    )
}

@Composable
internal fun DeclinedIcon() {
    Icon(
        imageVector = ImageVector.vectorResource(R.drawable.ic_close),
        tint = Web3ModalTheme.colors.error,
        contentDescription = ContentDescription.DECLINED.description,
        modifier = Modifier
            .size(20.dp)
            .background(Web3ModalTheme.colors.error.copy(alpha = .2f), shape = CircleShape)
            .padding(4.dp)
    )
}

@Composable
internal fun WalletIcon(
    tint: Color = Web3ModalTheme.colors.inverse100
) {
    Icon(
        imageVector = ImageVector.vectorResource(id = R.drawable.ic_wallet),
        contentDescription = ContentDescription.WALLET.description,
        modifier = Modifier.size(14.dp),
        tint = tint
    )
}

@Composable
internal fun ExternalIcon(
    tint: Color = Web3ModalTheme.colors.foreground.color200
) {
    Icon(
        imageVector = ImageVector.vectorResource(id = R.drawable.ic_external_link),
        contentDescription = ContentDescription.EXTERNAL_LINK.description,
        modifier = Modifier.size(10.dp),
        tint = tint
    )
}

@Composable
@UiModePreview
private fun IconsPreview() {
    MultipleComponentsPreview(
        { BackArrowIcon {} },
        { QuestionMarkIcon {} },
        { CloseIcon {} },
        { RetryIcon() },
        { DeclinedIcon() },
        { WalletIcon() },
        { ExternalIcon() }
    )
}