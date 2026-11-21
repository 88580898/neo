package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Dialpad
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardHide
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.BoardCurrencyItem
import com.example.ui.CurrencyUiState
import com.example.ui.SyncStatus
import com.example.ui.TimeFormatters
import com.example.ui.components.CurrencyPickerSheet
import com.example.ui.components.FinancialKeypad
import com.example.ui.theme.FinancialAmountLarge
import com.example.ui.theme.FinancialAmountMedium
import com.example.ui.theme.FinancialRateCaption
import com.example.ui.theme.JetBrainsMonoFamily

private sealed interface PickerMode {
    data object None : PickerMode
    data object AddToBoard : PickerMode
    data class ReplaceSlot(val oldCode: String) : PickerMode
}

@Composable
fun ConverterScreen(
    uiState: CurrencyUiState,
    onSelectActive: (String) -> Unit,
    onReplaceCurrency: (oldCode: String, newCode: String) -> Unit,
    onAddCurrency: (String) -> Unit,
    onRemoveCurrency: (String) -> Unit,
    onCopyItem: (BoardCurrencyItem) -> Unit,
    onSyncNow: () -> Unit,
    onOpenOfflineManager: () -> Unit,
    onToggleKeypad: () -> Unit,
    onDigit: (String) -> Unit,
    onDecimal: () -> Unit,
    onOperator: (Char) -> Unit,
    onBackspace: () -> Unit,
    onClear: () -> Unit,
    onEquals: () -> Unit,
    onQuickAmount: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var pickerMode by remember { mutableStateOf<PickerMode>(PickerMode.None) }
    val primaryGlow = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.42f)
    val bgColor = MaterialTheme.colorScheme.background

    Box(
        modifier = modifier
            .fillMaxSize()
            .drawBehind {
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(primaryGlow, bgColor),
                        center = Offset(size.width * 0.85f, 0f),
                        radius = size.width * 0.95f
                    )
                )
            },
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 640.dp)
        ) {
            ConverterTopHeader(
                uiState = uiState,
                onSyncNow = onSyncNow,
                onOpenOfflineManager = onOpenOfflineManager,
                onToggleKeypad = onToggleKeypad
            )

            // Multi-currency conversion board
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(
                    items = uiState.boardItems,
                    key = { it.info.code }
                ) { item ->
                    CurrencyBoardCard(
                        item = item,
                        expressionPreview = if (item.isActive) uiState.formattedExpressionPreview else "",
                        canRemove = uiState.boardItems.size > 2,
                        onSelectRow = { onSelectActive(item.info.code) },
                        onBadgeClick = { pickerMode = PickerMode.ReplaceSlot(item.info.code) },
                        onCopyClick = { onCopyItem(item) },
                        onRemoveClick = { onRemoveCurrency(item.info.code) }
                    )
                }

                item(key = "add_currency_footer") {
                    OutlinedButton(
                        onClick = { pickerMode = PickerMode.AddToBoard },
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 2.dp, bottom = 10.dp)
                            .height(48.dp)
                            .testTag("add_currency_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = stringResource(R.string.action_add_currency),
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }

            // Collapsible Financial Keypad
            AnimatedVisibility(
                visible = uiState.isKeypadVisible,
                enter = expandVertically(animationSpec = spring()) + fadeIn(tween(180)),
                exit = shrinkVertically(animationSpec = spring()) + fadeOut(tween(150))
            ) {
                FinancialKeypad(
                    hasOperator = uiState.hasOperatorInExpression,
                    onDigit = onDigit,
                    onDecimal = onDecimal,
                    onOperator = onOperator,
                    onBackspace = onBackspace,
                    onClear = onClear,
                    onEquals = onEquals,
                    onQuickAmount = onQuickAmount
                )
            }
        }
    }

    // Currency Picker Modal Bottom Sheet
    when (val mode = pickerMode) {
        PickerMode.None -> Unit
        PickerMode.AddToBoard -> {
            CurrencyPickerSheet(
                title = stringResource(R.string.title_add_currency),
                highlightedCodes = uiState.boardItems.map { it.info.code }.toSet(),
                onSelect = { selected ->
                    onAddCurrency(selected.code)
                    pickerMode = PickerMode.None
                },
                onDismiss = { pickerMode = PickerMode.None }
            )
        }
        is PickerMode.ReplaceSlot -> {
            CurrencyPickerSheet(
                title = stringResource(R.string.title_select_currency),
                highlightedCodes = setOf(mode.oldCode),
                onSelect = { selected ->
                    onReplaceCurrency(mode.oldCode, selected.code)
                    pickerMode = PickerMode.None
                },
                onDismiss = { pickerMode = PickerMode.None }
            )
        }
    }
}

@Composable
private fun ConverterTopHeader(
    uiState: CurrencyUiState,
    onSyncNow: () -> Unit,
    onOpenOfflineManager: () -> Unit,
    onToggleKeypad: () -> Unit
) {
    val isSyncing = uiState.syncStatus is SyncStatus.Syncing
    val infiniteTransition = rememberInfiniteTransition(label = "sync_spin")
    val spinAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 850, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "refresh_rotation"
    )

    val isUsingOfflineState = uiState.pureOfflineMode || !uiState.isOnline || uiState.lastSyncMillis == 0L
    val pillContainer = when {
        uiState.pureOfflineMode -> MaterialTheme.colorScheme.tertiaryContainer
        !uiState.isOnline -> MaterialTheme.colorScheme.surfaceVariant
        uiState.lastSyncMillis > 0L -> MaterialTheme.colorScheme.primaryContainer
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    val pillContent = when {
        uiState.pureOfflineMode -> MaterialTheme.colorScheme.onTertiaryContainer
        !uiState.isOnline -> MaterialTheme.colorScheme.onSurfaceVariant
        uiState.lastSyncMillis > 0L -> MaterialTheme.colorScheme.onPrimaryContainer
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    val pillLabel = when {
        isSyncing -> stringResource(R.string.status_updating)
        uiState.pureOfflineMode -> "${stringResource(R.string.status_offline_mode)} · ${TimeFormatters.formatRelativeSyncTime(uiState.lastSyncMillis)}"
        !uiState.isOnline -> "离线缓存 · ${TimeFormatters.formatRelativeSyncTime(uiState.lastSyncMillis)}"
        uiState.lastSyncMillis > 0L -> "${uiState.lastSyncSource} · ${TimeFormatters.formatRelativeSyncTime(uiState.lastSyncMillis)}"
        else -> stringResource(R.string.status_bundled_offline)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 12.dp, top = 12.dp, bottom = 6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            IconButton(
                onClick = onToggleKeypad,
                modifier = Modifier.testTag("toggle_keypad_button")
            ) {
                Icon(
                    imageVector = if (uiState.isKeypadVisible) Icons.Default.KeyboardHide else Icons.Default.Dialpad,
                    contentDescription = stringResource(R.string.cd_keypad_toggle),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(
                onClick = onSyncNow,
                enabled = !isSyncing,
                modifier = Modifier.testTag("header_refresh_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = stringResource(R.string.cd_refresh),
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.rotate(if (isSyncing) spinAngle else 0f)
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Interactive status pill (tapping opens Offline Rate Center)
        Surface(
            shape = RoundedCornerShape(999.dp),
            color = pillContainer,
            modifier = Modifier
                .clip(RoundedCornerShape(999.dp))
                .clickable(onClick = onOpenOfflineManager)
                .testTag("rate_status_pill")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = if (isUsingOfflineState) Icons.Default.CloudOff else Icons.Default.CloudDone,
                    contentDescription = null,
                    tint = pillContent,
                    modifier = Modifier.size(15.dp)
                )
                Text(
                    text = pillLabel,
                    style = MaterialTheme.typography.labelMedium,
                    color = pillContent,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (uiState.customCalibratedCount > 0) {
                    Text(
                        text = "· ${uiState.customCalibratedCount}项校准",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.tertiary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun CurrencyBoardCard(
    item: BoardCurrencyItem,
    expressionPreview: String,
    canRemove: Boolean,
    onSelectRow: () -> Unit,
    onBadgeClick: () -> Unit,
    onCopyClick: () -> Unit,
    onRemoveClick: () -> Unit
) {
    val borderColor by animateColorAsState(
        targetValue = if (item.isActive) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.outlineVariant
        },
        label = "card_border"
    )
    val containerColor by animateColorAsState(
        targetValue = if (item.isActive) {
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.28f)
        } else {
            MaterialTheme.colorScheme.surface
        },
        label = "card_bg"
    )

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = containerColor,
        border = BorderStroke(if (item.isActive) 1.8.dp else 1.dp, borderColor),
        tonalElevation = if (item.isActive) 2.dp else 0.dp,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .combinedClickable(
                onClick = onSelectRow,
                onLongClick = onCopyClick
            )
            .testTag("currency_card_${item.info.code}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Currency selector badge (tap to replace currency in this slot)
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .clickable(onClick = onBadgeClick)
                    .testTag("currency_badge_${item.info.code}")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.info.flag,
                        fontSize = 22.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = item.info.code,
                                fontFamily = JetBrainsMonoFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = stringResource(R.string.cd_currency_badge, item.info.nameZh),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = item.info.nameZh,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (item.isCustomCalibrated) {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.tertiaryContainer
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Tune,
                                        contentDescription = stringResource(R.string.status_custom_rate),
                                        tint = MaterialTheme.colorScheme.onTertiaryContainer,
                                        modifier = Modifier
                                            .padding(2.dp)
                                            .size(10.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Right side: Amount + unit rate + quick actions
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.End
            ) {
                if (expressionPreview.isNotEmpty()) {
                    Text(
                        text = "$expressionPreview =",
                        style = FinancialRateCaption,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        text = item.info.symbol,
                        fontFamily = JetBrainsMonoFamily,
                        fontSize = if (item.isActive) 18.sp else 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (item.isActive) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        modifier = Modifier.padding(end = 3.dp)
                    )
                    Text(
                        text = item.formattedAmount,
                        style = if (item.isActive) FinancialAmountLarge else FinancialAmountMedium,
                        color = if (item.isActive) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        },
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.End
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = item.unitRateText,
                        style = FinancialRateCaption,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = stringResource(R.string.action_copy_result),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier
                            .size(14.dp)
                            .clickable(onClick = onCopyClick)
                    )
                    if (canRemove && !item.isActive) {
                        Icon(
                            imageVector = Icons.Outlined.Close,
                            contentDescription = stringResource(R.string.cd_delete_currency, item.info.nameZh),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f),
                            modifier = Modifier
                                .size(15.dp)
                                .clickable(onClick = onRemoveClick)
                        )
                    }
                }
            }
        }
    }
}
