package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.CurrencyCatalog
import com.example.data.local.RateProvider
import com.example.ui.CurrencyUiState
import com.example.ui.OfflineRateRow
import com.example.ui.SyncStatus
import com.example.ui.TimeFormatters
import com.example.ui.components.CurrencyPickerSheet
import com.example.ui.components.EditOfflineRateDialog
import com.example.ui.components.ImportOfflinePackDialog
import com.example.ui.theme.EmeraldDeep
import com.example.ui.theme.EmeraldHeroEnd
import com.example.ui.theme.JetBrainsMonoFamily

@Composable
fun OfflineRatesScreen(
    uiState: CurrencyUiState,
    onSyncNow: () -> Unit,
    onTogglePureOffline: (Boolean) -> Unit,
    onToggleAutoUpdate: (Boolean) -> Unit,
    onSelectProvider: (RateProvider) -> Unit,
    onChangeOfflineBase: (String) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onSaveCustomRate: (targetCode: String, customRate: Double) -> Unit,
    onResetCustomRate: (targetCode: String) -> Unit,
    onPinToBoard: (String) -> Unit,
    onExportOfflinePack: () -> Unit,
    onImportOfflinePack: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var editingRow by remember { mutableStateOf<OfflineRateRow?>(null) }
    var showBasePicker by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }
    val clipboardManager = LocalClipboardManager.current

    val baseInfo = remember(uiState.offlineBaseCurrencyCode) {
        CurrencyCatalog.getOrFallback(uiState.offlineBaseCurrencyCode)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 640.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item(key = "header") {
                Column(modifier = Modifier.padding(horizontal = 4.dp)) {
                    Text(
                        text = stringResource(R.string.title_offline_manager),
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = stringResource(R.string.subtitle_offline_manager),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Hero Sovereign Emerald Sync Card
            item(key = "hero_sync_card") {
                OfflineHeroSyncCard(
                    uiState = uiState,
                    onSyncNow = onSyncNow
                )
            }

            // Offline & Public Source Settings
            item(key = "settings_card") {
                OfflineSettingsCard(
                    uiState = uiState,
                    onTogglePureOffline = onTogglePureOffline,
                    onToggleAutoUpdate = onToggleAutoUpdate,
                    onSelectProvider = onSelectProvider
                )
            }

            // Offline Pack Export / Import
            item(key = "offline_pack_card") {
                OfflinePackCard(
                    onExport = onExportOfflinePack,
                    onImport = { showImportDialog = true }
                )
            }

            // Cached Rates Directory Header & Search
            item(key = "directory_header") {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.title_all_rates, uiState.offlineBaseCurrencyCode),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = stringResource(R.string.subtitle_tap_to_calibrate),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { showBasePicker = true }
                                .testTag("offline_base_selector")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = baseInfo.flag, fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = baseInfo.code,
                                    fontFamily = JetBrainsMonoFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowDown,
                                    contentDescription = stringResource(R.string.action_swap_base),
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = uiState.offlineSearchQuery,
                        onValueChange = onSearchQueryChange,
                        placeholder = { Text(stringResource(R.string.hint_search_currency)) },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Search, contentDescription = null)
                        },
                        trailingIcon = {
                            if (uiState.offlineSearchQuery.isNotEmpty()) {
                                IconButton(onClick = { onSearchQueryChange("") }) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = stringResource(R.string.cd_clear)
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("offline_rates_search")
                    )
                }
            }

            if (uiState.allRates.isEmpty()) {
                item(key = "empty_rates") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.empty_search_result),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                items(
                    items = uiState.allRates,
                    key = { it.info.code }
                ) { row ->
                    OfflineRateItemCard(
                        row = row,
                        baseCode = uiState.offlineBaseCurrencyCode,
                        onEditClick = {
                            if (row.info.code != uiState.offlineBaseCurrencyCode) {
                                editingRow = row
                            }
                        },
                        onPinClick = { onPinToBoard(row.info.code) }
                    )
                }
            }

            item(key = "bottom_spacer") {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    editingRow?.let { row ->
        EditOfflineRateDialog(
            baseCode = uiState.offlineBaseCurrencyCode,
            row = row,
            onSave = { customRate -> onSaveCustomRate(row.info.code, customRate) },
            onResetToPublic = { onResetCustomRate(row.info.code) },
            onDismiss = { editingRow = null }
        )
    }

    if (showBasePicker) {
        CurrencyPickerSheet(
            title = stringResource(R.string.action_swap_base),
            highlightedCodes = setOf(uiState.offlineBaseCurrencyCode),
            onSelect = { selected ->
                onChangeOfflineBase(selected.code)
                showBasePicker = false
            },
            onDismiss = { showBasePicker = false }
        )
    }

    if (showImportDialog) {
        val clipboardText = clipboardManager.getText()?.text.orEmpty()
        ImportOfflinePackDialog(
            initialClipboardText = clipboardText,
            onConfirmImport = onImportOfflinePack,
            onDismiss = { showImportDialog = false }
        )
    }
}

@Composable
private fun OfflineHeroSyncCard(
    uiState: CurrencyUiState,
    onSyncNow: () -> Unit
) {
    val isSyncing = uiState.syncStatus is SyncStatus.Syncing

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(EmeraldDeep, EmeraldHeroEnd)
                )
            )
            .padding(20.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = Color.White.copy(alpha = 0.16f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Icon(
                            imageVector = when {
                                uiState.pureOfflineMode -> Icons.Default.CloudOff
                                uiState.isOnline -> Icons.Default.Wifi
                                else -> Icons.Default.WifiOff
                            },
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = when {
                                uiState.pureOfflineMode -> stringResource(R.string.status_offline_mode)
                                uiState.isOnline -> stringResource(R.string.state_network_online)
                                else -> stringResource(R.string.state_network_offline)
                            },
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White
                        )
                    }
                }

                Text(
                    text = TimeFormatters.formatFullTimestamp(uiState.lastSyncMillis),
                    fontFamily = JetBrainsMonoFamily,
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.85f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                HeroStatBlock(
                    label = stringResource(R.string.label_cached_currencies),
                    value = "${uiState.cachedCurrencyCount} 种",
                    modifier = Modifier.weight(1f)
                )
                HeroStatBlock(
                    label = stringResource(R.string.label_rate_source),
                    value = uiState.lastSyncSource,
                    modifier = Modifier.weight(1.4f)
                )
            }

            Button(
                onClick = onSyncNow,
                enabled = !isSyncing && !uiState.pureOfflineMode,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = EmeraldDeep,
                    disabledContainerColor = Color.White.copy(alpha = 0.35f),
                    disabledContentColor = Color.White.copy(alpha = 0.85f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("update_offline_rates_button")
            ) {
                if (isSyncing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = EmeraldDeep
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.btn_updating_offline),
                        style = MaterialTheme.typography.labelLarge
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.CloudDownload,
                        contentDescription = null,
                        modifier = Modifier.size(19.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (uiState.pureOfflineMode) {
                            "纯离线模式已开启"
                        } else {
                            stringResource(R.string.btn_update_offline_now)
                        },
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}

@Composable
private fun HeroStatBlock(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.72f)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun OfflineSettingsCard(
    uiState: CurrencyUiState,
    onTogglePureOffline: (Boolean) -> Unit,
    onToggleAutoUpdate: (Boolean) -> Unit,
    onSelectProvider: (RateProvider) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(R.string.title_offline_settings),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.label_pure_offline_mode),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = stringResource(R.string.desc_pure_offline_mode),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Switch(
                    checked = uiState.pureOfflineMode,
                    onCheckedChange = onTogglePureOffline,
                    modifier = Modifier.testTag("switch_pure_offline")
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.label_auto_update_launch),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = stringResource(R.string.desc_auto_update_launch),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Switch(
                    checked = uiState.autoUpdateOnLaunch,
                    onCheckedChange = onToggleAutoUpdate,
                    modifier = Modifier.testTag("switch_auto_update")
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = stringResource(R.string.label_rate_source),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    RateProvider.entries.forEach { provider ->
                        FilterChip(
                            selected = uiState.preferredProvider == provider,
                            onClick = { onSelectProvider(provider) },
                            label = {
                                Text(
                                    text = when (provider) {
                                        RateProvider.AUTO -> "自动优选"
                                        RateProvider.ER_API -> "ER-API"
                                        RateProvider.FRANKFURTER -> "欧洲央行"
                                    },
                                    style = MaterialTheme.typography.labelMedium
                                )
                            },
                            modifier = Modifier.testTag("provider_chip_${provider.id}")
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OfflinePackCard(
    onExport: () -> Unit,
    onImport: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = stringResource(R.string.title_offline_pack),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = stringResource(R.string.desc_offline_pack),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onExport,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("export_offline_pack_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = stringResource(R.string.btn_export_pack), maxLines = 1)
                }

                Button(
                    onClick = onImport,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("import_offline_pack_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = stringResource(R.string.btn_import_pack), maxLines = 1)
                }
            }
        }
    }
}

@Composable
private fun OfflineRateItemCard(
    row: OfflineRateRow,
    baseCode: String,
    onEditClick: () -> Unit,
    onPinClick: () -> Unit
) {
    val isBase = row.info.code == baseCode

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            width = 1.dp,
            color = if (row.isCustomCalibrated) {
                MaterialTheme.colorScheme.tertiary.copy(alpha = 0.6f)
            } else {
                MaterialTheme.colorScheme.outlineVariant
            }
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(enabled = !isBase, onClick = onEditClick)
            .testTag("offline_rate_row_${row.info.code}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.size(38.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(text = row.info.flag, fontSize = 19.sp)
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = row.info.code,
                        fontFamily = JetBrainsMonoFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = row.info.nameZh,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (row.isCustomCalibrated) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.tertiaryContainer
                        ) {
                            Text(
                                text = stringResource(R.string.status_custom_rate),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onTertiaryContainer,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                Text(
                    text = if (isBase) {
                        "当前基准货币"
                    } else {
                        "1 $baseCode = ${row.formattedEffectiveRate} ${row.info.code}"
                    },
                    fontFamily = JetBrainsMonoFamily,
                    fontSize = 12.sp,
                    color = if (row.isCustomCalibrated) {
                        MaterialTheme.colorScheme.tertiary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }

            if (!isBase) {
                IconButton(
                    onClick = onEditClick,
                    modifier = Modifier.testTag("calibrate_${row.info.code}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = stringResource(R.string.dialog_edit_rate_title),
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            IconButton(
                onClick = { if (!row.isPinnedToBoard) onPinClick() },
                enabled = !row.isPinnedToBoard,
                modifier = Modifier.testTag("pin_${row.info.code}")
            ) {
                Icon(
                    imageVector = if (row.isPinnedToBoard) Icons.Default.CheckCircle else Icons.Default.AddCircleOutline,
                    contentDescription = if (row.isPinnedToBoard) {
                        stringResource(R.string.badge_in_board)
                    } else {
                        stringResource(R.string.action_pin_to_board)
                    },
                    tint = if (row.isPinnedToBoard) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
