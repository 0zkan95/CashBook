package com.example.cashbook.ui.viewmodel

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cashbook.util.GithubRelease

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VersionControlScreen(
    currentVersion: String,
    availableUpdate: GithubRelease?,
    updateProgress: Float,
    onCheckUpdate: () -> Unit,
    onStartUpdate: (GithubRelease) -> Unit,
    onDismiss: () -> Unit,
    onMenuClick: () -> Unit = {}
) {
    val backgroundColor = MaterialTheme.colorScheme.background
    val cardBackgroundColor = MaterialTheme.colorScheme.surfaceVariant
    val context = LocalContext.current

    Column(
        modifier = Modifier.fillMaxSize().background(backgroundColor).padding(16.dp).safeDrawingPadding()
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onDismiss) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = MaterialTheme.colorScheme.onBackground) }
            Text(
                text = "Version Control",
                color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Bold, fontSize = 20.sp, modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onMenuClick) { Icon(Icons.Default.Menu, "Menu", tint = MaterialTheme.colorScheme.onBackground) }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = cardBackgroundColor)
        ) {
            Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text("Current Version", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                Text(currentVersion, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)

                Spacer(modifier = Modifier.height(24.dp))

                if (availableUpdate != null) {
                    Text("New Version Available!", color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold)
                    Text(availableUpdate.tag_name, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    if (updateProgress > 0f && updateProgress < 1f) {
                        LinearProgressIndicator(
                            progress = { updateProgress },
                            modifier = Modifier.fillMaxWidth().height(8.dp),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                        Text("${(updateProgress * 100).toInt()}%", fontSize = 12.sp)
                    } else {
                        Button(
                            onClick = { onStartUpdate(availableUpdate) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                        ) {
                            Icon(Icons.Default.Download, null)
                            Spacer(Modifier.width(8.dp))
                            Text("Update Now")
                        }
                    }
                } else {
                    Text("You're up to date!", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    OutlinedButton(
                        onClick = onCheckUpdate,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Refresh, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Check for Updates")
                    }
                }
            }
        }
    }
}
