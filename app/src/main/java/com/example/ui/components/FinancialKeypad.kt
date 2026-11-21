package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.JetBrainsMonoFamily

private val QuickAmounts = listOf("100", "500", "1000", "5000", "10000")

@Composable
fun FinancialKeypad(
    hasOperator: Boolean,
    onDigit: (String) -> Unit,
    onDecimal: () -> Unit,
    onOperator: (Char) -> Unit,
    onBackspace: () -> Unit,
    onClear: () -> Unit,
    onEquals: () -> Unit,
    onQuickAmount: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Quick amount chips + Clear action
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                LazyRow(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    contentPadding = PaddingValues(end = 4.dp)
                ) {
                    items(QuickAmounts) { amount ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { onQuickAmount(amount) }
                                .testTag("quick_amount_$amount")
                        ) {
                            Text(
                                text = amount,
                                fontFamily = JetBrainsMonoFamily,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                            )
                        }
                    }
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.75f),
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { onClear() }
                        .testTag("key_clear")
                ) {
                    Text(
                        text = "AC",
                        fontFamily = JetBrainsMonoFamily,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                    )
                }
            }

            // 4x4 numeric + operator grid
            KeypadRow(
                keys = listOf("7", "8", "9"),
                operatorLabel = "÷",
                operatorTag = "key_div",
                onDigit = onDigit,
                onOperatorClick = { onOperator('÷') }
            )
            KeypadRow(
                keys = listOf("4", "5", "6"),
                operatorLabel = "×",
                operatorTag = "key_mul",
                onDigit = onDigit,
                onOperatorClick = { onOperator('×') }
            )
            KeypadRow(
                keys = listOf("1", "2", "3"),
                operatorLabel = "−",
                operatorTag = "key_sub",
                onDigit = onDigit,
                onOperatorClick = { onOperator('-') }
            )

            // Bottom row: .  0  ⌫  (+ or =)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                KeypadButton(
                    label = ".",
                    containerColor = MaterialTheme.colorScheme.background,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    onClick = onDecimal,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("key_decimal")
                )
                KeypadButton(
                    label = "0",
                    containerColor = MaterialTheme.colorScheme.background,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    onClick = { onDigit("0") },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("key_0")
                )
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f),
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .minimumInteractiveComponentSize()
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { onBackspace() }
                        .testTag("key_backspace")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Backspace,
                            contentDescription = stringResource(R.string.cd_backspace),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                if (hasOperator) {
                    KeypadButton(
                        label = "=",
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        onClick = onEquals,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("key_equals")
                    )
                } else {
                    KeypadButton(
                        label = "+",
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        onClick = { onOperator('+') },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("key_add")
                    )
                }
            }
            Spacer(modifier = Modifier.height(2.dp))
        }
    }
}

@Composable
private fun KeypadRow(
    keys: List<String>,
    operatorLabel: String,
    operatorTag: String,
    onDigit: (String) -> Unit,
    onOperatorClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        keys.forEach { digit ->
            KeypadButton(
                label = digit,
                containerColor = MaterialTheme.colorScheme.background,
                contentColor = MaterialTheme.colorScheme.onSurface,
                onClick = { onDigit(digit) },
                modifier = Modifier
                    .weight(1f)
                    .testTag("key_$digit")
            )
        }
        KeypadButton(
            label = operatorLabel,
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            onClick = onOperatorClick,
            modifier = Modifier
                .weight(1f)
                .testTag(operatorTag)
        )
    }
}

@Composable
private fun KeypadButton(
    label: String,
    containerColor: Color,
    contentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = containerColor,
        modifier = modifier
            .height(50.dp)
            .minimumInteractiveComponentSize()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = label,
                fontFamily = JetBrainsMonoFamily,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = contentColor
            )
        }
    }
}
