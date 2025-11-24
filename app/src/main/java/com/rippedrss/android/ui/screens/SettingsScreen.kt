package com.rippedrss.android.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.rippedrss.android.ui.viewmodel.SettingsUiState
import com.rippedrss.android.util.RelativeTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    uiState: SettingsUiState,
    onBackgroundRefreshChanged: (Boolean) -> Unit,
    onWifiOnlyChanged: (Boolean) -> Unit,
    onDarkModeChanged: (Boolean) -> Unit
) {
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
                SettingsSwitchItem(
                    title = "Dark Mode",
                    subtitle = "Use dark theme",
                    checked = uiState.darkMode,
                    onCheckedChange = onDarkModeChanged
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
private fun SettingsFooterText(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )
}
