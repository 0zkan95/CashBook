package com.example.cashbook.ui.viewmodel

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Note
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp

@Composable
fun AppDrawerContent(
    onHomeClick: () -> Unit,
    onSearchClick: () -> Unit,
    onSummaryClick: () -> Unit,
    onNotesClick: () -> Unit,
    onAccountClick: () -> Unit,
    onBackupClick: () -> Unit,
    onVersionClick: () -> Unit,
    onSettingsClick: () -> Unit,
    isDarkMode: Boolean,
    onToggleDarkMode: () -> Unit
) {
    val drawerBgColor = MaterialTheme.colorScheme.background
    val contentColor = MaterialTheme.colorScheme.onBackground
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp

    ModalDrawerSheet(
        modifier = Modifier.width(screenWidth * 0.75f),
        drawerContainerColor = drawerBgColor
    ) {
        Text(
            text = "Cash Book",
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.titleLarge,
            color = contentColor
        )

        val itemColors = NavigationDrawerItemDefaults.colors(
            unselectedContainerColor = Color.Transparent,
            selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
            unselectedIconColor = contentColor,
            selectedIconColor = MaterialTheme.colorScheme.primary,
            unselectedTextColor = contentColor,
            selectedTextColor = MaterialTheme.colorScheme.primary
        )

        NavigationDrawerItem(
            label = { Text("Home") },
            selected = false,
            icon = { Icon(Icons.Default.Home, contentDescription = null) },
            onClick = onHomeClick,
            modifier = Modifier.padding(horizontal = 12.dp),
            colors = itemColors
        )
        NavigationDrawerItem(
            label = { Text("Custom Search") },
            selected = false,
            icon = { Icon(Icons.Default.Search, contentDescription = null) },
            onClick = onSearchClick,
            modifier = Modifier.padding(horizontal = 12.dp),
            colors = itemColors
        )
        NavigationDrawerItem(
            label = { Text("Monthly Summary") },
            selected = false,
            icon = { Icon(Icons.Default.DateRange, contentDescription = null) },
            onClick = onSummaryClick,
            modifier = Modifier.padding(horizontal = 12.dp),
            colors = itemColors
        )
        NavigationDrawerItem(
            label = { Text("Notes") },
            selected = false,
            icon = { Icon(Icons.AutoMirrored.Filled.Note, contentDescription = null) },
            onClick = onNotesClick,
            modifier = Modifier.padding(horizontal = 12.dp),
            colors = itemColors
        )
        NavigationDrawerItem(
            label = { Text("Account") },
            selected = false,
            icon = { Icon(Icons.Default.Person, contentDescription = null) },
            onClick = onAccountClick,
            modifier = Modifier.padding(horizontal = 12.dp),
            colors = itemColors
        )
        NavigationDrawerItem(
            label = { Text("Backup and Restore") },
            selected = false,
            icon = { Icon(Icons.Default.CloudUpload, contentDescription = null) },
            onClick = onBackupClick,
            modifier = Modifier.padding(horizontal = 12.dp),
            colors = itemColors
        )
        NavigationDrawerItem(
            label = { Text("Version Control") },
            selected = false,
            icon = { Icon(Icons.Default.SystemUpdate, contentDescription = null) },
            onClick = onVersionClick,
            modifier = Modifier.padding(horizontal = 12.dp),
            colors = itemColors
        )
        NavigationDrawerItem(
            label = { Text("Settings") },
            selected = false,
            icon = { Icon(Icons.Default.Settings, contentDescription = null) },
            onClick = onSettingsClick,
            modifier = Modifier.padding(horizontal = 12.dp),
            colors = itemColors
        )

        Spacer(modifier = Modifier.weight(1f))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (isDarkMode) Icons.Default.DarkMode else Icons.Default.LightMode,
                    contentDescription = null,
                    tint = contentColor
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(text = "Dark Mode", color = contentColor)
            }
            Switch(
                checked = isDarkMode,
                onCheckedChange = { onToggleDarkMode() },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.primary,
                    checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                )
            )
        }
    }
}
