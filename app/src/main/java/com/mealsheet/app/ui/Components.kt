package com.mealsheet.app.ui

import android.content.Context
import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mealsheet.app.Entry
import com.mealsheet.app.Fmt
import com.mealsheet.app.Meal
import com.mealsheet.app.MealStore
import com.mealsheet.app.Summary
import java.time.LocalDate

/** One meal row: tap it to "initial" the sheet, tap again to undo. */
@Composable
fun MealCard(
    meal: Meal,
    date: LocalDate,
    entry: Entry?,
    initials: String,
    priceCents: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
) {
    val c = meal.colors()
    val cs = MaterialTheme.colorScheme
    val logged = entry != null
    val background by animateColorAsState(
        if (logged) c.container else cs.surfaceContainerLowest,
        label = "mealCardBackground",
    )
    val haptics = LocalHapticFeedback.current

    val subtitle = when {
        entry == null -> "Tap to initial · ${Fmt.money(priceCents)}"
        Fmt.dateOf(entry.at) == date -> "Initialed ${Fmt.time(entry.at)} · ${Fmt.money(entry.cents)}"
        else -> "Added late (${Fmt.dateOf(entry.at).format(Fmt.monthDay)}) · ${Fmt.money(entry.cents)}"
    }

    Surface(
        onClick = {
            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
            onClick()
        },
        shape = RoundedCornerShape(if (compact) 18.dp else 24.dp),
        color = background,
        border = BorderStroke(1.dp, if (logged) c.accent.copy(alpha = 0.35f) else cs.outlineVariant),
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            Modifier.padding(horizontal = 16.dp, vertical = if (compact) 12.dp else 18.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                Modifier
                    .size(if (compact) 44.dp else 56.dp)
                    .clip(CircleShape)
                    .background(if (logged) c.accent.copy(alpha = 0.18f) else c.container),
                contentAlignment = Alignment.Center,
            ) {
                Text(meal.emoji, fontSize = if (compact) 22.sp else 28.sp)
            }
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    meal.label,
                    style = if (compact) MaterialTheme.typography.titleMedium else MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = if (logged) c.onContainer else cs.onSurface,
                )
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (logged) c.onContainer.copy(alpha = 0.8f) else cs.onSurfaceVariant,
                )
            }
            Spacer(Modifier.width(12.dp))
            InitialStamp(logged, initials, c, if (compact) 40.dp else 48.dp)
        }
    }
}

/** Empty circle until tapped, then the person's initials "stamped" in, like initialing a paper sheet. */
@Composable
private fun InitialStamp(logged: Boolean, initials: String, c: MealColors, size: Dp) {
    Box(Modifier.size(size), contentAlignment = Alignment.Center) {
        Box(
            Modifier
                .size(size)
                .border(2.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), CircleShape),
        )
        AnimatedVisibility(
            visible = logged,
            enter = scaleIn(spring(dampingRatio = Spring.DampingRatioMediumBouncy), initialScale = 1.6f) + fadeIn(),
            exit = scaleOut(targetScale = 0.6f) + fadeOut(),
        ) {
            Box(
                Modifier
                    .size(size)
                    .rotate(-10f)
                    .clip(CircleShape)
                    .background(c.accent),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    initials,
                    color = c.onAccent,
                    fontWeight = FontWeight.Bold,
                    fontSize = if (initials.length > 2) 13.sp else 16.sp,
                )
            }
        }
    }
}

@Composable
fun MealDots(meals: Set<Meal>, modifier: Modifier = Modifier, dot: Dp = 6.dp, showEmpty: Boolean = true) {
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
        Meal.entries.forEach { m ->
            val color = when {
                m in meals -> m.colors().accent
                showEmpty -> MaterialTheme.colorScheme.outlineVariant
                else -> Color.Transparent
            }
            Box(Modifier.size(dot).clip(CircleShape).background(color))
        }
    }
}

/** Big green card: what you owe for a stretch of days, with a per-meal breakdown. */
@Composable
fun SummaryCard(title: String, summary: Summary, modifier: Modifier = Modifier, footnote: String? = null) {
    val cs = MaterialTheme.colorScheme
    Surface(shape = RoundedCornerShape(24.dp), color = cs.primary, modifier = modifier.fillMaxWidth()) {
        Column(Modifier.padding(20.dp)) {
            Text(title, style = MaterialTheme.typography.labelLarge, color = cs.onPrimary.copy(alpha = 0.8f))
            Spacer(Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    Fmt.money(summary.cents),
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = cs.onPrimary,
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    "for ${summary.meals} meal${if (summary.meals == 1) "" else "s"}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = cs.onPrimary.copy(alpha = 0.8f),
                    modifier = Modifier.padding(bottom = 6.dp),
                )
            }
            Spacer(Modifier.height(14.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Meal.entries.forEach { m ->
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = cs.onPrimary.copy(alpha = 0.12f),
                        contentColor = cs.onPrimary,
                        modifier = Modifier.weight(1f),
                    ) {
                        Column(Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
                            Text(
                                "${m.emoji} ${summary.counts[m] ?: 0}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Text(m.label, style = MaterialTheme.typography.labelSmall, color = cs.onPrimary.copy(alpha = 0.75f))
                        }
                    }
                }
            }
            if (footnote != null) {
                Spacer(Modifier.height(12.dp))
                Text(footnote, style = MaterialTheme.typography.bodySmall, color = cs.onPrimary.copy(alpha = 0.8f))
            }
        }
    }
}

@Composable
fun LogoMark(size: Dp, modifier: Modifier = Modifier) {
    Box(
        modifier
            .size(size)
            .clip(RoundedCornerShape(size * 0.3f))
            .background(MaterialTheme.colorScheme.primary),
        contentAlignment = Alignment.Center,
    ) {
        Icon(Icons.Filled.Check, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(size * 0.62f))
    }
}

@Composable
fun SectionTitle(title: String, subtitle: String? = null, modifier: Modifier = Modifier) {
    Column(modifier) {
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        if (subtitle != null) {
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

fun shareText(context: Context, subject: String, text: String) {
    val send = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, subject)
        putExtra(Intent.EXTRA_TEXT, text)
    }
    context.startActivity(Intent.createChooser(send, subject))
}

/** Plain-text summary for a date range, suitable for texting or emailing. */
fun buildReport(store: MealStore, heading: String, start: LocalDate, end: LocalDate): String = buildString {
    val entries = store.entriesIn(start, end)
    val summary = store.summarize(start, end)
    appendLine("Meal Sheet — $heading")
    appendLine("${store.name} (${store.initials})")
    appendLine("${start.format(Fmt.fullDate)} – ${end.format(Fmt.fullDate)}")
    appendLine()
    if (entries.isEmpty()) appendLine("No meals logged.")
    entries.groupBy { it.first }.forEach { (date, rows) ->
        appendLine("${date.format(Fmt.dayShort)}: " + rows.joinToString(", ") { (_, meal, e) -> "${meal.label} ${Fmt.money(e.cents)}" })
    }
    appendLine()
    Meal.entries.forEach { appendLine("${it.label}: ${summary.counts[it] ?: 0}") }
    appendLine("Total: ${summary.meals} meals, ${Fmt.money(summary.cents)}")
}

/** Every logged meal as CSV, for spreadsheets. */
fun buildCsv(store: MealStore): String = buildString {
    appendLine("date,meal,price,logged_at,added_late")
    val first = store.firstLoggedDate ?: return@buildString
    for ((date, meal, e) in store.entriesIn(first, LocalDate.now().plusYears(1))) {
        val loggedAt = java.time.Instant.ofEpochMilli(e.at).atZone(java.time.ZoneId.systemDefault()).toLocalDateTime().withNano(0)
        val late = Fmt.dateOf(e.at) != date
        appendLine("$date,${meal.label},${com.mealsheet.app.centsToInput(e.cents)},$loggedAt,${if (late) "yes" else "no"}")
    }
}
