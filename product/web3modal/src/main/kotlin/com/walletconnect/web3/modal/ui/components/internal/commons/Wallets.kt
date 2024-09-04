package com.walletconnect.web3.modal.ui.components.internal.commons

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.reown.android.internal.common.modal.data.model.Wallet
import com.walletconnect.web3.modal.R
import com.walletconnect.web3.modal.ui.previews.MultipleComponentsPreview
import com.walletconnect.web3.modal.ui.previews.UiModePreview
import com.walletconnect.web3.modal.ui.previews.testWallets
import com.walletconnect.web3.modal.ui.theme.AppKitTheme
import com.walletconnect.web3.modal.utils.grayColorFilter
import com.walletconnect.web3.modal.utils.imageHeaders

@Composable
internal fun MultipleWalletIcon(wallets: List<Wallet>) {
    Column(
        modifier = Modifier
            .size(40.dp)
            .background(AppKitTheme.colors.background.color200, shape = RoundedCornerShape(10.dp))
            .padding(1.dp)
            .border(1.dp, AppKitTheme.colors.grayGlass10, shape = RoundedCornerShape(10.dp)),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        wallets.chunked(2).forEach {
            Row(
                modifier = Modifier.width(40.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                it.forEach { item ->
                    WalletImage(
                        url = item.imageUrl,
                        modifier = Modifier
                            .size(16.dp)
                            .padding(1.dp)
                            .clip(RoundedCornerShape(4.dp))
                    )
                }
            }
        }

    }
}

internal fun LazyGridScope.walletsGridItems(
    wallets: List<Wallet>,
    onWalletItemClick: (Wallet) -> Unit
) {
    itemsIndexed(
        items = wallets,
        key = { _, wallet -> wallet.id }
    ) { _, wallet ->
        WalletGridItem(
            wallet = wallet,
            onWalletItemClick = onWalletItemClick
        )
    }
}

@Composable
internal fun WalletImageWithLoader(url: String?) {
    LoadingBorder(
        cornerRadius = 28.dp
    ) {
        RoundedWalletImage(url = url)
    }
}

@Composable
internal fun RoundedWalletImage(url: String?) {
    WalletImage(
        url = url, modifier = Modifier
            .size(80.dp)
            .border(width = 1.dp, color = AppKitTheme.colors.grayGlass10, shape = RoundedCornerShape(28.dp))
            .clip(RoundedCornerShape(28.dp))
    )
}

@Composable
internal fun WalletImage(url: String?, isEnabled: Boolean = true, modifier: Modifier) {
    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(url)
            .fallback(R.drawable.walletconnect_blue)
            .crossfade(true)
            .placeholder(R.drawable.wallet_placeholder)
            .imageHeaders()
            .build(),
        contentDescription = null,
        modifier = modifier,
        colorFilter = if (isEnabled) null else grayColorFilter
    )
}

@Composable
internal fun BoxScope.InstalledWalletIcon() {
    Icon(
        modifier = Modifier
            .offset(x = 2.dp, y = 2.dp)
            .background(AppKitTheme.colors.background.color125, shape = CircleShape)
            .align(Alignment.BottomEnd)
            .background(AppKitTheme.colors.grayGlass02, shape = CircleShape)
            .padding(2.dp)
            .size(12.dp)
            .background(AppKitTheme.colors.success.copy(0.15f), shape = CircleShape)
            .padding(2.dp),
        imageVector = ImageVector.vectorResource(id = R.drawable.ic_check),
        contentDescription = "WalletConnectLogo",
        tint = AppKitTheme.colors.success
    )
}

@Composable
internal fun WalletGridItem(
    wallet: Wallet,
    onWalletItemClick: (Wallet) -> Unit
) {
    TransparentSurface(
        modifier = Modifier.padding(2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .width(76.dp)
                .height(96.dp)
                .background(AppKitTheme.colors.grayGlass02)
                .clickable { onWalletItemClick(wallet) },
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box {
                WalletImage(
                    url = wallet.imageUrl,
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .border(width = 1.dp, color = AppKitTheme.colors.grayGlass10, shape = RoundedCornerShape(16.dp))
                )
                if (wallet.isWalletInstalled) {
                    InstalledWalletIcon()
                }
            }
            VerticalSpacer(height = 8.dp)
            Text(
                text = wallet.name,
                style = AppKitTheme.typo.tiny500,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 2.dp)
            )
        }
    }
}

@UiModePreview
@Composable
private fun PreviewWallets() {
    MultipleComponentsPreview(
        { WalletGridItem(wallet = testWallets.first(), onWalletItemClick = {}) },
        { WalletGridItem(wallet = testWallets[1], onWalletItemClick = {}) },
        { MultipleWalletIcon(wallets = testWallets.take(4)) },
    )
}
