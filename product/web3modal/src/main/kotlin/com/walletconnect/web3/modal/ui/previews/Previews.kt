package com.walletconnect.web3.modal.ui.previews

import android.content.res.Configuration.UI_MODE_NIGHT_NO
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import com.reown.android.internal.common.model.ProjectId
import com.reown.android.internal.common.wcKoinApp
import com.walletconnect.web3.modal.ui.components.internal.root.AppKitRoot
import com.walletconnect.web3.modal.ui.components.internal.commons.VerticalSpacer
import com.walletconnect.web3.modal.ui.components.internal.root.rememberAppKitRootState
import com.walletconnect.web3.modal.ui.components.internal.snackbar.rememberSnackBarState
import com.walletconnect.web3.modal.ui.theme.ProvideAppKitThemeComposition
import com.walletconnect.web3.modal.ui.theme.AppKitTheme
import org.koin.dsl.module

@Composable
internal fun AppKitPreview(
    title: String? = null,
    content: @Composable () -> Unit,
) {
    previewKoinDefinitions()
    ProvideAppKitThemeComposition {
        val scope = rememberCoroutineScope()
        val rootState = rememberAppKitRootState(coroutineScope = scope, navController = rememberNavController())
        val snackBarState = rememberSnackBarState(coroutineScope = scope)

        AppKitRoot(rootState = rootState, snackBarState = snackBarState, closeModal = {}, title = title) {
            content()
        }
    }
}

@Composable
internal fun ComponentPreview(
    content: @Composable () -> Unit
) {
    previewKoinDefinitions()
    ProvideAppKitThemeComposition {
        Column(modifier = Modifier.background(AppKitTheme.colors.background.color100)) {
            content()
        }
    }
}

@Composable
internal fun MultipleComponentsPreview(
    vararg content: @Composable () -> Unit
) {
    previewKoinDefinitions()
    ProvideAppKitThemeComposition {
        Column {
            content.forEach {
                VerticalSpacer(height = 5.dp)
                Box(modifier = Modifier.background(AppKitTheme.colors.background.color100)) { it() }
                VerticalSpacer(height = 5.dp)
            }
        }
    }
}

private fun previewKoinDefinitions() {
    val modules = listOf(
        module { single { ProjectId("fakeId") } }
    )
    wcKoinApp.koin.loadModules(modules = modules)
}

@LightTheme
@DarkTheme
internal annotation class UiModePreview

@Preview(uiMode = UI_MODE_NIGHT_NO)
internal annotation class LightTheme

@Preview(uiMode = UI_MODE_NIGHT_YES)
internal annotation class DarkTheme

@Preview(device = Devices.AUTOMOTIVE_1024p, widthDp = 720, heightDp = 360, uiMode = UI_MODE_NIGHT_YES)
internal annotation class Landscape
