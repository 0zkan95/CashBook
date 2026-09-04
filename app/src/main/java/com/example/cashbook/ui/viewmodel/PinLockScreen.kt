package com.example.cashbook.ui.viewmodel

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PinLockScreen(
    correctPin: String,
    onUnlock: () -> Unit
) {
    var enteredPin by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }

    val backgroundColor = MaterialTheme.colorScheme.background
    val contentColor = MaterialTheme.colorScheme.onBackground

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Lock,
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier.size(64.dp)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "Enter PIN to Unlock",
            color = contentColor,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(32.dp))

        // PIN Indicators
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            repeat(4) { index ->
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .background(
                            if (index < enteredPin.length) MaterialTheme.colorScheme.primary else contentColor.copy(alpha = 0.3f),
                            CircleShape
                        )
                )
            }
        }

        if (showError) {
            Text(
                text = "Incorrect PIN, try again",
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 16.dp)
            )
        }

        Spacer(modifier = Modifier.height(48.dp))

        // Number Pad
        val numbers = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "", "0", "DEL")
        
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            for (i in 0 until 4) {
                Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                    for (j in 0 until 3) {
                        val key = numbers[i * 3 + j]
                        if (key.isNotEmpty()) {
                            KeyButton(
                                text = key,
                                onClick = {
                                    if (key == "DEL") {
                                        if (enteredPin.isNotEmpty()) enteredPin = enteredPin.dropLast(1)
                                        showError = false
                                    } else if (enteredPin.length < 4) {
                                        enteredPin += key
                                        if (enteredPin.length == 4) {
                                            if (enteredPin == correctPin) {
                                                onUnlock()
                                            } else {
                                                showError = true
                                                enteredPin = ""
                                            }
                                        }
                                    }
                                }
                            )
                        } else {
                            Spacer(modifier = Modifier.size(72.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun KeyButton(text: String, onClick: () -> Unit) {
    FilledTonalButton(
        onClick = onClick,
        modifier = Modifier.size(72.dp),
        shape = CircleShape,
        colors = ButtonDefaults.filledTonalButtonColors(
            containerColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f),
            contentColor = MaterialTheme.colorScheme.onBackground
        )
    ) {
        if (text == "DEL") {
            Icon(Icons.AutoMirrored.Filled.Backspace, null)
        } else {
            Text(text = text, fontSize = 24.sp, fontWeight = FontWeight.Medium)
        }
    }
}
