package com.rippedrss.android.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.rippedrss.android.ui.theme.TextScale
import com.rippedrss.android.ui.theme.ThemeMode
import com.rippedrss.android.ui.viewmodel.SettingsUiState
import com.rippedrss.android.util.RelativeTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    uiState: SettingsUiState,
    onBackgroundRefreshChanged: (Boolean) -> Unit,
    onWifiOnlyChanged: (Boolean) -> Unit,
    onThemeModeChanged: (ThemeMode) -> Unit,
    onTextScaleChanged: (TextScale) -> Unit,
    onResetData: () -> Unit,
    // Legacy callback for backward compatibility
    onDarkModeChanged: (Boolean) -> Unit = { onThemeModeChanged(if (it) ThemeMode.DARK else ThemeMode.LIGHT) }
) {
    var showResetDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // Appearance Section
            SettingsSection(title = "Appearance") {
                // Theme Selection
                SettingsDropdownItem(
                    title = "Theme",
                    subtitle = "Choose your preferred color scheme",
                    selectedValue = uiState.themeMode.displayName,
                    options = ThemeMode.entries.map { it.displayName },
                    onOptionSelected = { displayName ->
                        ThemeMode.entries.find { it.displayName == displayName }
                            ?.let { onThemeModeChanged(it) }
                    }
                )

                // Text Scale Selection
                SettingsDropdownItem(
                    title = "Text Size",
                    subtitle = "Adjust article text size",
                    selectedValue = uiState.textScale.displayName,
                    options = TextScale.entries.map { it.displayName },
                    onOptionSelected = { displayName ->
                        TextScale.entries.find { it.displayName == displayName }
                            ?.let { onTextScaleChanged(it) }
                    }
                )

                SettingsFooterText(
                    text = "Text size is applied to article content. " +
                            "Combine with your system text size setting for optimal readability."
                )
            }

            HorizontalDivider()

            // Feed Refresh Section
            SettingsSection(title = "Feed Refresh") {
                SettingsSwitchItem(
                    title = "Background Refresh",
                    subtitle = "Automatically refresh feeds in the background",
                    checked = uiState.backgroundRefreshEnabled,
                    onCheckedChange = onBackgroundRefreshChanged
                )

                if (uiState.backgroundRefreshEnabled) {
                    SettingsSwitchItem(
                        title = "Wi-Fi Only",
                        subtitle = "Only refresh when connected to Wi-Fi",
                        checked = uiState.wifiOnly,
                        onCheckedChange = onWifiOnlyChanged
                    )
                }

                if (uiState.lastRefreshTime != null) {
                    SettingsInfoItem(
                        title = "Last Refresh",
                        value = RelativeTimeFormatter.format(uiState.lastRefreshTime)
                    )
                }

                if (uiState.backgroundRefreshEnabled) {
                    SettingsFooterText(
                        text = "Background refresh frequency is determined by the system. " +
                                "Refresh may not occur when battery is low or Low Power Mode is enabled."
                    )
                }
            }

            HorizontalDivider()

            // Data Management Section
            SettingsSection(title = "Data Management") {
                SettingsActionItem(
                    title = "Reset All Data",
                    subtitle = "Delete all feeds, articles, and reset settings",
                    isDestructive = true,
                    isLoading = uiState.isResetting,
                    onClick = { showResetDialog = true }
                )
            }

            HorizontalDivider()

            // About Section
            SettingsSection(title = "About") {
                SettingsInfoItem(
                    title = "Version",
                    value = "1.0.0"
                )
                SettingsFooterText(
                    text = "RippedRichRSS is an Android port of RichRSS, " +
                            "created by Claude to feature-match the iOS version."
                )
            }
        }
    }

    // Reset Confirmation Dialog
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            icon = {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = { Text("Reset All Data?") },
            text = {
                Text(
                    "This will permanently delete all your feeds, articles, and settings. " +
                            "This action cannot be undone."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showResetDialog = false
                        onResetData()
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Reset")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        content()
    }
}

@Composable
private fun SettingsSwitchItem(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsDropdownItem(
    title: String,
    subtitle: String,
    selectedValue: String,
    options: List<String>,
    onOptionSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = true }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it }
        ) {
            OutlinedTextField(
                value = selectedValue,
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    .width(140.dp),
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                singleLine = true
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            onOptionSelected(option)
                            expanded = false
                        },
                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsInfoItem(
    title: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}

@Composable
private fun SettingsActionItem(
    title: String,
    subtitle: String,
    isDestructive: Boolean = false,
    isLoading: Boolean = false,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !isLoading) { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = if (isDestructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }

        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                strokeWidth = 2.dp
            )
        }
    }
}

@Composable
private fun SettingsFooterText(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )
}
