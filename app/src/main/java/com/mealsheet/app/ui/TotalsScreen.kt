package com.mealsheet.app.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mealsheet.app.Fmt
import com.mealsheet.app.Meal
import com.mealsheet.app.MealStore
import com.mealsheet.app.Period
import com.mealsheet.app.Summary
import java.time.LocalDate

/** Totals per pay period, so you can check them against the deduction on your paystub. */
@Composable
fun TotalsScreen(store: MealStore, onOpenSettings: () -> Unit) {
    val today = LocalDate.now()
    val current = store.periodContaining(today)
    val earlier = store.earlierPeriods(today)
    val context = LocalContext.current
    val cs = MaterialTheme.colorScheme

    fun share(period: Period) = shareText(
        context,
        "Meal Sheet — ${period.label}",
        buildReport(store, "Pay period ${period.label}", period.start, period.end),
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                SectionTitle(
                    "Pay periods",
                    "Compare these with the meal deduction on your paystub.",
                    Modifier.weight(1f),
                )
                TextButton(onClick = onOpenSettings) { Text(store.payPeriod.label) }
            }
        }

        item {
            SummaryCard(
                title = "Current pay period · ${current.label}",
                summary = store.summarize(current),
                footnote = "Ends ${current.end.format(Fmt.dayLong)}",
            )
        }

        item {
            Text(
                "Earlier",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 10.dp),
            )
        }

        if (earlier.isEmpty()) {
            item {
                Text(
                    "Earlier pay periods will show up here once they have meals in them.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = cs.onSurfaceVariant,
                )
            }
        } else {
            items(earlier, key = { it.start.toString() }) { period ->
                PeriodRow(period, store.summarize(period), onShare = { share(period) })
            }
        }

        item {
            val first = store.firstLoggedDate
            if (first != null) {
                val all = store.summarize(first, today)
                Text(
                    "All time: ${all.meals} meals · ${Fmt.money(all.cents)} since ${first.format(Fmt.fullDate)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = cs.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
        }

        item {
            OutlinedButton(
                onClick = { shareText(context, "Meal Sheet — all meals (CSV)", buildCsv(store)) },
                enabled = store.firstLoggedDate != null,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, bottom = 16.dp)
                    .height(52.dp),
            ) {
                Icon(Icons.Filled.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(10.dp))
                Text("Export all meals (CSV)")
            }
        }
    }
}

@Composable
private fun PeriodRow(period: Period, summary: Summary, onShare: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = cs.surfaceContainerLowest,
        border = BorderStroke(1.dp, cs.outlineVariant),
    ) {
        Row(Modifier.padding(start = 16.dp, top = 12.dp, bottom = 12.dp, end = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(period.label, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Meal.entries.forEach { m ->
                        Text("${m.emoji} ${summary.counts[m] ?: 0}", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(Fmt.money(summary.cents), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text("${summary.meals} meals", style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant)
            }
            IconButton(onClick = onShare) {
                Icon(Icons.Filled.Share, contentDescription = "Share ${period.label}", tint = cs.onSurfaceVariant, modifier = Modifier.size(20.dp))
            }
        }
    }
}
