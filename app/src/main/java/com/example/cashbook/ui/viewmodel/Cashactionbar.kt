package com.example.cashbook.ui.viewmodel

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

/**
 * Two side-by-side buttons: green "Cash In" (income) and red "Cash Out"
 * (expense). Wire these to TransactionViewModel.addTransaction() — Cash In
 * with a positive amount, Cash Out with a negative one — probably by
 * opening the same AddTransactionDialog pre-set to the right type.
 */
@Composable
fun CashActionBar(
    onCashIn: () -> Unit,
    onCashOut: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Button(
            onClick = onCashIn,
            modifier = Modifier
                .weight(1f)
                .height(52.dp),
            shape = RoundedCornerShape(26.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
        ) {
            Text("Cash In", color = Color.White)
        }
        Button(
            onClick = onCashOut,
            modifier = Modifier
                .weight(1f)
                .height(52.dp),
            shape = RoundedCornerShape(26.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        ) {
            Text("Cash Out", color = Color.White)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CashActionBarPreview() {
    CashActionBar(onCashIn = {}, onCashOut = {})
}