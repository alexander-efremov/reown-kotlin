package com.reown.appkit.ui.components.internal.root

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import com.reown.android.internal.common.wcKoinApp
import com.reown.android.pulse.domain.SendEventInterface
import com.reown.android.pulse.model.EventType
import com.reown.android.pulse.model.properties.Props
import com.reown.appkit.ui.navigation.Route
import com.reown.appkit.ui.navigation.getTitleArg
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Composable
internal fun rememberAppKitRootState(
    coroutineScope: CoroutineScope,
    navController: NavController
): AppKitRootState {
    return remember(coroutineScope, navController) {
        AppKitRootState(coroutineScope, navController)
    }
}

internal class AppKitRootState(
    private val coroutineScope: CoroutineScope,
    private val navController: NavController
) {
    private val sendEventUseCase: SendEventInterface = wcKoinApp.koin.get()
    val currentDestinationFlow: Flow<NavBackStackEntry>
        get() = navController.currentBackStackEntryFlow

    val canPopUp: Boolean
        get() = !topLevelDestinations.any { it.path == navController.currentDestination?.route } || navController.previousBackStackEntry != null

    val title: Flow<String?> = currentDestinationFlow
        .map { it.getTitleArg() ?: it.destination.toTitle() }

    val currentDestinationRoute: String?
        get() = navController.currentDestination?.route

    fun navigateToHelp() {
        sendEventUseCase.send(Props(EventType.TRACK, EventType.Track.CLICK_WALLET_HELP,))
        navController.navigate(Route.WHAT_IS_WALLET.path)
    }

    fun popUp() {
        navController.popBackStack()
    }
}

private fun NavDestination.toTitle(): String? = Route.values().find { route.orEmpty().startsWith(it.path) }?.title

private val topLevelDestinations = listOf(Route.CONNECT_YOUR_WALLET, Route.ACCOUNT, Route.CHOOSE_NETWORK, Route.CHANGE_NETWORK, Route.SIWE_FALLBACK)
