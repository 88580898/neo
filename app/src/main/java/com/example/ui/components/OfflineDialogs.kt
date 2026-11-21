package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.OfflineRateRow
import com.example.ui.theme.JetBrainsMonoFamily

@Composable
fun EditOfflineRateDialog(
    baseCode: String,
    row: OfflineRateRow,
    onSave: (Double) -> Unit,
    onResetToPublic: () -> Unit,
    onDismiss: () -> Unit
) {
    var rateInput by remember(row.info.code) {
        mutableStateOf(row.formattedEffectiveRate.replace(",", ""))
    }
    var errorText by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        title = {
            Text(
                text = "${stringResource(R.string.dialog_edit_rate_title)} · ${row.info.flag} ${row.info.code}",
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = stringResource(
                        R.string.dialog_edit_rate_desc,
                        baseCode,
                        "${row.info.nameZh} (${row.info.code})"
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = stringResource(
                        R.string.label_public_rate_reference,
                        baseCode,
                        row.formattedPublicRate,
                        row.info.code
                    ),
                    style = MaterialTheme.typography.labelMedium,
                    fontFamily = JetBrainsMonoFamily,
                    color = MaterialTheme.colorScheme.primary
                )

                OutlinedTextField(
                    value = rateInput,
                    onValueChange = {
                        rateInput = it
                        errorText = null
                    },
                    label = { Text(stringResource(R.string.label_custom_rate_input)) },
                    prefix = { Text("1 $baseCode = ") },
                    suffix = { Text(row.info.code) },
                    isError = errorText != null,
                    supportingText = errorText?.let { msg -> { Text(msg) } },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("custom_rate_input")
                )

                if (row.isCustomCalibrated) {
                    OutlinedButton(
                        onClick = {
                            onResetToPublic()
                            onDismiss()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("reset_custom_rate_button")
                    ) {
                        Text(stringResource(R.string.btn_reset_rate))
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val parsed = rateInput.trim().toDoubleOrNull()
                    if (parsed == null || parsed <= 0.0 || !parsed.isFinite()) {
                        errorText = "请输入大于 0 的有效数字"
                    } else {
                        onSave(parsed)
                        onDismiss()
                    }
                },
                modifier = Modifier.testTag("save_custom_rate_button")
            ) {
                Text(stringResource(R.string.btn_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.btn_cancel))
            }
        }
    )
}

@Composable
fun ImportOfflinePackDialog(
    initialClipboardText: String,
    onConfirmImport: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var jsonText by remember {
        mutableStateOf(
            if (initialClipboardText.trim().startsWith("{")) initialClipboardText else ""
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        title = {
            Text(
                text = stringResource(R.string.dialog_import_title),
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = stringResource(R.string.desc_offline_pack),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = jsonText,
                    onValueChange = { jsonText = it },
                    placeholder = { Text(stringResource(R.string.dialog_import_hint)) },
                    minLines = 4,
                    maxLines = 7,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("import_pack_input")
                )
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        onConfirmImport(jsonText)
                        onDismiss()
                    },
                    enabled = jsonText.isNotBlank(),
                    modifier = Modifier.testTag("confirm_import_pack_button")
                ) {
                    Text(stringResource(R.string.btn_confirm_import))
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.btn_cancel))
            }
        }
    )
}
