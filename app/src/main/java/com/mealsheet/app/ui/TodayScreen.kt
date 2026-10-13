package com.mealsheet.app.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mealsheet.app.Fmt
import com.mealsheet.app.Meal
import com.mealsheet.app.MealStore
import com.mealsheet.app.PayPeriod
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun TodayScreen(store: MealStore, onOpenDay: (LocalDate) -> Unit) {
    val today = LocalDate.now()
    val period = store.periodContaining(today)
    val periodTitle =
        if (store.payPeriod == PayPeriod.MONTHLY) "${today.format(Fmt.monthName)} so far"
        else "This pay period · ${period.label}"

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp),
    ) {
        Text(
            "${greeting()}, ${store.firstName}",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            today.format(Fmt.dayLong),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(Modifier.height(20.dp))
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Meal.entries.forEach { meal ->
                MealCard(
                    meal = meal,
                    date = today,
                    entry = store.entry(today, meal),
                    initials = store.initials,
                    priceCents = store.price(meal),
                    onClick = { store.toggle(today, meal) },
                )
            }
        }

        Spacer(Modifier.height(24.dp))
        SummaryCard(periodTitle, store.summarize(period.start, today))

        Spacer(Modifier.height(24.dp))
        SectionTitle("Last 7 days", "Forgot to initial one? Tap a day to fix it.")
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            for (daysAgo in 6 downTo 0) {
                val date = today.minusDays(daysAgo.toLong())
                DayChip(
                    date = date,
                    meals = store.mealsOn(date),
                    isToday = daysAgo == 0,
                    onClick = { onOpenDay(date) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun DayChip(date: LocalDate, meals: Set<Meal>, isToday: Boolean, onClick: () -> Unit, modifier: Modifier) {
    val cs = MaterialTheme.colorScheme
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = if (isToday) cs.primaryContainer else cs.surfaceContainerLowest,
        border = if (isToday) null else BorderStroke(1.dp, cs.outlineVariant),
        modifier = modifier,
    ) {
        Column(Modifier.padding(vertical = 10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.US).take(2),
                style = MaterialTheme.typography.labelSmall,
                color = cs.onSurfaceVariant,
            )
            Text("${date.dayOfMonth}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(6.dp))
            MealDots(meals, dot = 5.dp)
        }
    }
}

private fun greeting(): String {
    val hour = LocalTime.now().hour
    return when {
        hour < 12 -> "Good morning"
        hour < 17 -> "Good afternoon"
        else -> "Good evening"
    }
}
