package com.mealsheet.app.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mealsheet.app.Fmt
import com.mealsheet.app.Meal
import com.mealsheet.app.MealStore
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth

@Composable
fun MonthScreen(store: MealStore, onOpenDay: (LocalDate) -> Unit) {
    val today = LocalDate.now()
    val current = YearMonth.from(today)
    var month by rememberSaveable { mutableStateOf(current) }
    val context = LocalContext.current
    val monthName = month.format(Fmt.monthName)

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { month = month.minusMonths(1) }) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Previous month")
            }
            Text(
                month.format(Fmt.monthYear),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f),
            )
            IconButton(onClick = { month = month.plusMonths(1) }, enabled = month < current) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Next month")
            }
        }

        Spacer(Modifier.height(8.dp))
        CalendarGrid(store, month, today, onOpenDay)

        Spacer(Modifier.height(14.dp))
        Legend(Modifier.align(Alignment.CenterHorizontally))

        Spacer(Modifier.height(20.dp))
        SummaryCard(
            title = if (month == current) "$monthName so far" else "$monthName total",
            summary = store.summarize(month.atDay(1), month.atEndOfMonth()),
        )

        Spacer(Modifier.height(14.dp))
        OutlinedButton(
            onClick = {
                shareText(
                    context,
                    "Meal Sheet — ${month.format(Fmt.monthYear)}",
                    buildReport(store, month.format(Fmt.monthYear), month.atDay(1), month.atEndOfMonth()),
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
                .height(52.dp),
        ) {
            Icon(Icons.Filled.Share, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(10.dp))
            Text("Share $monthName summary")
        }
    }
}

@Composable
private fun CalendarGrid(store: MealStore, month: YearMonth, today: LocalDate, onOpenDay: (LocalDate) -> Unit) {
    val offset = month.atDay(1).dayOfWeek.value % 7 // Sunday-first
    val days = month.lengthOfMonth()
    val rows = (offset + days + 6) / 7
    val gap = 6.dp

    Column(verticalArrangement = Arrangement.spacedBy(gap)) {
        Row(horizontalArrangement = Arrangement.spacedBy(gap)) {
            listOf("S", "M", "T", "W", "T", "F", "S").forEach {
                Text(
                    it,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f),
                )
            }
        }
        for (row in 0 until rows) {
            Row(horizontalArrangement = Arrangement.spacedBy(gap)) {
                for (col in 0 until 7) {
                    val dayOfMonth = row * 7 + col - offset + 1
                    if (dayOfMonth in 1..days) {
                        val date = month.atDay(dayOfMonth)
                        CalendarDay(
                            date = date,
                            meals = store.mealsOn(date),
                            isToday = date == today,
                            isFuture = date.isAfter(today),
                            onClick = { onOpenDay(date) },
                            modifier = Modifier.weight(1f),
                        )
                    } else {
                        Spacer(Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun CalendarDay(
    date: LocalDate,
    meals: Set<Meal>,
    isToday: Boolean,
    isFuture: Boolean,
    onClick: () -> Unit,
    modifier: Modifier,
) {
    val cs = MaterialTheme.colorScheme
    Surface(
        onClick = onClick,
        enabled = !isFuture,
        shape = RoundedCornerShape(12.dp),
        color = when {
            isToday -> cs.primaryContainer
            meals.isNotEmpty() -> cs.surfaceContainerLowest
            else -> cs.surfaceContainerLow
        },
        border = when {
            isToday -> BorderStroke(1.5.dp, cs.primary)
            meals.isNotEmpty() -> BorderStroke(1.dp, cs.outlineVariant)
            else -> null
        },
        modifier = modifier
            .aspectRatio(0.8f)
            .alpha(if (isFuture) 0.4f else 1f),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                "${date.dayOfMonth}",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Medium,
            )
            Spacer(Modifier.height(5.dp))
            MealDots(meals, dot = 5.dp, showEmpty = false)
        }
    }
}

@Composable
private fun Legend(modifier: Modifier = Modifier) {
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        Meal.entries.forEach { m ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(m.colors().accent),
                )
                Spacer(Modifier.width(6.dp))
                Text(m.label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

/** Bottom sheet for adding or removing meals on any past day. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DayEditorSheet(store: MealStore, date: LocalDate, onDismiss: () -> Unit) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    val isToday = date == LocalDate.now()

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            Modifier
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Column {
                Text(
                    if (isToday) "Today" else date.format(Fmt.dayLong),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    if (isToday) date.format(Fmt.dayLong) else "Tap a meal to add or remove it. Meals added after the day are marked as late.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Meal.entries.forEach { meal ->
                MealCard(
                    meal = meal,
                    date = date,
                    entry = store.entry(date, meal),
                    initials = store.initials,
                    priceCents = store.price(meal),
                    onClick = { store.toggle(date, meal) },
                    compact = true,
                )
            }
            Spacer(Modifier.height(4.dp))
            Button(
                onClick = { scope.launch { sheetState.hide() }.invokeOnCompletion { onDismiss() } },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
            ) {
                Text("Done")
            }
        }
    }
}
