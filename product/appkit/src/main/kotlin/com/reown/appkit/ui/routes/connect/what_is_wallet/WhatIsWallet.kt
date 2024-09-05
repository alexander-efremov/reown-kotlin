package com.reown.appkit.ui.routes.connect.what_is_wallet

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.reown.android.internal.common.wcKoinApp
import com.reown.android.pulse.domain.SendEventInterface
import com.reown.android.pulse.model.EventType
import com.reown.android.pulse.model.properties.Props
import com.reown.appkit.R
import com.reown.appkit.ui.components.internal.commons.HelpSection
import com.reown.appkit.ui.components.internal.commons.VerticalSpacer
import com.reown.appkit.ui.components.internal.commons.WalletIcon
import com.reown.appkit.ui.components.internal.commons.button.ButtonSize
import com.reown.appkit.ui.components.internal.commons.button.ButtonStyle
import com.reown.appkit.ui.components.internal.commons.button.ImageButton
import com.reown.appkit.ui.navigation.Route
import com.reown.appkit.ui.previews.UiModePreview
import com.reown.appkit.ui.previews.AppKitPreview
import com.reown.appkit.ui.theme.AppKitTheme

@Composable
internal fun WhatIsWallet(
    navController: NavController
) {
    val sendEventUseCase: SendEventInterface = wcKoinApp.koin.get()
    WhatIsWallet {
        sendEventUseCase.send(Props(EventType.TRACK, EventType.Track.CLICK_GET_WALLET))
        navController.navigate(Route.GET_A_WALLET.path)
    }
}

@Composable
private fun WhatIsWallet(
    onGetAWalletClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        VerticalSpacer(20.dp)
        HelpSection(
            title = "One login for all of web3",
            body = "Log in to any app by connecting your wallet. Say goodbye to countless passwords!",
            assets = listOf(R.drawable.login, R.drawable.profile, R.drawable.lock)
        )
        VerticalSpacer(24.dp)
        HelpSection(
            title = "A home for your digital assets",
            body = "A wallet lets you store, send and receive digital assets like cryptocurrencies and NFTs.",
            assets = listOf(R.drawable.defi, R.drawable.nft, R.drawable.eth)
        )
        VerticalSpacer(24.dp)
        HelpSection(
            title = "Your gateway to a new web",
            body = "With your wallet, you can explore and interact with DeFi, NFTs, DAOs, and much more.",
            assets = listOf(R.drawable.browser, R.drawable.noun, R.drawable.dao)
        )
        VerticalSpacer(20.dp)
        ImageButton(
            text = "Get a wallet",
            image = { WalletIcon(AppKitTheme.colors.inverse100) },
            style = ButtonStyle.MAIN,
            size = ButtonSize.S,
            paddingValues = PaddingValues(start = 8.dp, top = 6.dp, end = 12.dp, 6.dp),
            onClick = { onGetAWalletClick() }
        )
        VerticalSpacer(30.dp)
    }
}

@Composable
@UiModePreview
private fun HelpContentPreview() {
    AppKitPreview("What is a Wallet?") {
        WhatIsWallet {}
    }
}