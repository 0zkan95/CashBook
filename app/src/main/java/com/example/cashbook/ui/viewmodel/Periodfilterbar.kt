package com.example.cashbook.ui.viewmodel

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Calendar

/**
 * The time-range a balance/transaction list is being filtered to.
 */
enum class Period(val label: String) {
    ALL("All"),
    DAILY("Daily"),
    WEEKLY("Weekly"),
    MONTHLY("Monthly"),
    YEARLY("Yearly"),
    CASH_IN("Cash In"),
    CASH_OUT("Cash Out")
}

/**
 * Horizontal row of pill toggles. Only one Period is selected at a time.
 */
@Composable
fun PeriodFilterBar(
    selected: Period,
    onSelect: (Period) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(Period.values().toList()) { period ->
            FilterChip(
                selected = period == selected,
                onClick = { onSelect(period) },
                label = { Text(period.label) },
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    labelColor = MaterialTheme.colorScheme.onSurface,
                    selectedContainerColor = Color(0xFFD4D957),
                    selectedLabelColor = Color.Black
                )
            )
        }
    }
}

@Composable
fun SubFilterBar(
    mainPeriod: Period,
    selectedSub: String?,
    onSelect: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    val options = remember(mainPeriod) {
        when (mainPeriod) {
            Period.DAILY -> {
                (0..7).map { offset ->
                    val c = Calendar.getInstance()
                    c.add(Calendar.DAY_OF_YEAR, -offset)
                    c.get(Calendar.DAY_OF_MONTH).toString()
                }.reversed()
            }
            Period.WEEKLY -> {
                (0..3).map { offset ->
                    val start = Calendar.getInstance().apply {
                        set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
                        add(Calendar.WEEK_OF_YEAR, -offset)
                    }
                    val end = (start.clone() as Calendar).apply { add(Calendar.DAY_OF_WEEK, 6) }
                    "${start.get(Calendar.DAY_OF_MONTH)} - ${end.get(Calendar.DAY_OF_MONTH)}"
                }.reversed()
            }
            Period.MONTHLY -> {
                listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
            }
            Period.YEARLY -> {
                val currentYear = Calendar.getInstance().get(Calendar.YEAR)
                (0..4).map { (currentYear - it).toString() }.reversed()
            }
            Period.CASH_IN, Period.CASH_OUT -> {
                listOf("Daily", "Weekly", "Monthly", "Yearly")
            }
            else -> emptyList()
        }
    }

    if (options.isEmpty()) return

    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(options) { opt ->
            FilterChip(
                selected = opt == selectedSub,
                onClick = { onSelect(if (opt == selectedSub) null else opt) },
                label = { Text(opt, fontSize = 12.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                    labelColor = MaterialTheme.colorScheme.onSurface,
                    selectedContainerColor = Color(0xFFD4D957).copy(alpha = 0.7f),
                    selectedLabelColor = Color.Black
                )
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PeriodFilterBarPreview() {
    PeriodFilterBar(selected = Period.DAILY, onSelect = {})
}
