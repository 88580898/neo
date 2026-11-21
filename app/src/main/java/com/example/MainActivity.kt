package com.example

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.outlined.CloudDownload
import androidx.compose.material.icons.outlined.CurrencyExchange
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.AppTab
import com.example.ui.CurrencyViewModel
import com.example.ui.UiEvent
import com.example.ui.screens.ConverterScreen
import com.example.ui.screens.OfflineRatesScreen
import com.example.ui.theme.MinimalFxTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MinimalFxTheme {
                MinimalFxApp()
            }
        }
    }
}

@Composable
fun MinimalFxApp() {
    val context = LocalContext.current
    val viewModel: CurrencyViewModel = viewModel(
        factory = remember(context) { CurrencyViewModel.Factory(context) }
    )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var currentTab by rememberSaveable { mutableStateOf(AppTab.CONVERTER) }

    // Return to primary converter tab on system back press when viewing Offline Rates tab
    BackHandler(enabled = currentTab != AppTab.CONVERTER) {
        currentTab = AppTab.CONVERTER
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is UiEvent.ShowMessage -> {
                    snackbarHostState.showSnackbar(
                        message = event.message,
                        duration = SnackbarDuration.Short
                    )
                }
                is UiEvent.CopyToClipboard -> {
                    val clipboard =
                        context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(ClipData.newPlainText(event.label, event.text))
                    snackbarHostState.showSnackbar(
                        message = event.toastMessage,
                        duration = SnackbarDuration.Short
                    )
                }
            }
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val useNavigationRail = maxWidth >= 600.dp

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = MaterialTheme.colorScheme.background,
            contentWindowInsets = WindowInsets.safeDrawing,
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            bottomBar = {
                if (!useNavigationRail) {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        tonalElevation = 3.dp
                    ) {
                        NavigationBarItem(
                            selected = currentTab == AppTab.CONVERTER,
                            onClick = { currentTab = AppTab.CONVERTER },
                            icon = {
                                Icon(
                                    imageVector = if (currentTab == AppTab.CONVERTER) {
                                        Icons.Filled.CurrencyExchange
                                    } else {
                                        Icons.Outlined.CurrencyExchange
                                    },
                                    contentDescription = stringResource(R.string.nav_converter)
                                )
                            },
                            label = { Text(stringResource(R.string.nav_converter)) },
                            modifier = Modifier.testTag("nav_tab_converter")
                        )
                        NavigationBarItem(
                            selected = currentTab == AppTab.OFFLINE_RATES,
                            onClick = { currentTab = AppTab.OFFLINE_RATES },
                            icon = {
                                Icon(
                                    imageVector = if (currentTab == AppTab.OFFLINE_RATES) {
                                        Icons.Filled.CloudDownload
                                    } else {
                                        Icons.Outlined.CloudDownload
                                    },
                                    contentDescription = stringResource(R.string.nav_offline_rates)
                                )
                            },
                            label = { Text(stringResource(R.string.nav_offline_rates)) },
                            modifier = Modifier.testTag("nav_tab_offline")
                        )
                    }
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (useNavigationRail) {
                    NavigationRail(
                        containerColor = MaterialTheme.colorScheme.surface
                    ) {
                        NavigationRailItem(
                            selected = currentTab == AppTab.CONVERTER,
                            onClick = { currentTab = AppTab.CONVERTER },
                            icon = {
                                Icon(
                                    imageVector = if (currentTab == AppTab.CONVERTER) {
                                        Icons.Filled.CurrencyExchange
                                    } else {
                                        Icons.Outlined.CurrencyExchange
                                    },
                                    contentDescription = stringResource(R.string.nav_converter)
                                )
                            },
                            label = { Text(stringResource(R.string.nav_converter)) },
                            modifier = Modifier.testTag("rail_tab_converter")
                        )
                        NavigationRailItem(
                            selected = currentTab == AppTab.OFFLINE_RATES,
                            onClick = { currentTab = AppTab.OFFLINE_RATES },
                            icon = {
                                Icon(
                                    imageVector = if (currentTab == AppTab.OFFLINE_RATES) {
                                        Icons.Filled.CloudDownload
                                    } else {
                                        Icons.Outlined.CloudDownload
                                    },
                                    contentDescription = stringResource(R.string.nav_offline_rates)
                                )
                            },
                            label = { Text(stringResource(R.string.nav_offline_rates)) },
                            modifier = Modifier.testTag("rail_tab_offline")
                        )
                    }
                }

                Box(modifier = Modifier.weight(1f)) {
                    when (currentTab) {
                        AppTab.CONVERTER -> ConverterScreen(
                            uiState = uiState,
                            onSelectActive = viewModel::selectActiveCurrency,
                            onReplaceCurrency = viewModel::replaceBoardCurrency,
                            onAddCurrency = viewModel::addCurrencyToBoard,
                            onRemoveCurrency = viewModel::removeCurrencyFromBoard,
                            onCopyItem = viewModel::copyConvertedValue,
                            onSyncNow = { viewModel.syncRates(silentFailure = false) },
                            onOpenOfflineManager = { currentTab = AppTab.OFFLINE_RATES },
                            onToggleKeypad = viewModel::toggleKeypadVisible,
                            onDigit = viewModel::onDigitPressed,
                            onDecimal = viewModel::onDecimalPressed,
                            onOperator = viewModel::onOperatorPressed,
                            onBackspace = viewModel::onBackspacePressed,
                            onClear = viewModel::onClearPressed,
                            onEquals = viewModel::onEqualsPressed,
                            onQuickAmount = viewModel::onQuickAmountSelected
                        )

                        AppTab.OFFLINE_RATES -> OfflineRatesScreen(
                            uiState = uiState,
                            onSyncNow = { viewModel.syncRates(silentFailure = false) },
                            onTogglePureOffline = viewModel::setPureOfflineMode,
                            onToggleAutoUpdate = viewModel::setAutoUpdateOnLaunch,
                            onSelectProvider = viewModel::setPreferredProvider,
                            onChangeOfflineBase = viewModel::setOfflineBaseCurrency,
                            onSearchQueryChange = viewModel::setOfflineSearchQuery,
                            onSaveCustomRate = viewModel::saveCustomOfflineRate,
                            onResetCustomRate = viewModel::resetCustomOfflineRate,
                            onPinToBoard = viewModel::addCurrencyToBoard,
                            onExportOfflinePack = viewModel::exportOfflinePack,
                            onImportOfflinePack = viewModel::importOfflinePack
                        )
                    }
                }
            }
        }
    }
}
