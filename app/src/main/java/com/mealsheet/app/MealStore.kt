package com.mealsheet.app

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.text.NumberFormat
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters
import java.util.Locale
import kotlin.random.Random

enum class Meal(val label: String, val emoji: String, val code: String) {
    BREAKFAST("Breakfast", "🍳", "B"),
    LUNCH("Lunch", "🥪", "L"),
    DINNER("Dinner", "🍝", "D"),
}

/** One initialed meal: when it was logged and what it cost at the time. */
data class Entry(val at: Long, val cents: Int)

data class Summary(val counts: Map<Meal, Int>, val cents: Int) {
    val meals: Int get() = counts.values.sum()
}

data class Period(val start: LocalDate, val end: LocalDate) {
    operator fun contains(date: LocalDate): Boolean = !date.isBefore(start) && !date.isAfter(end)

    val label: String
        get() = if (start.month == end.month && start.year == end.year) {
            "${start.format(Fmt.monthDay)} – ${end.dayOfMonth}"
        } else {
            "${start.format(Fmt.monthDay)} – ${end.format(Fmt.monthDay)}"
        }
}

enum class PayPeriod(val label: String, val detail: String) {
    MONTHLY("Monthly", "The 1st through the end of the month"),
    SEMI_MONTHLY("Twice a month", "The 1st–15th, then the 16th–end of month"),
    BIWEEKLY("Every two weeks", "14-day periods"),
    WEEKLY("Weekly", "7-day periods");

    val usesStartDate: Boolean get() = this == BIWEEKLY || this == WEEKLY

    fun periodContaining(date: LocalDate, anchor: LocalDate): Period = when (this) {
        MONTHLY -> Period(date.withDayOfMonth(1), date.withDayOfMonth(date.lengthOfMonth()))
        SEMI_MONTHLY ->
            if (date.dayOfMonth <= 15) Period(date.withDayOfMonth(1), date.withDayOfMonth(15))
            else Period(date.withDayOfMonth(16), date.withDayOfMonth(date.lengthOfMonth()))
        BIWEEKLY, WEEKLY -> {
            val length = if (this == WEEKLY) 7L else 14L
            val offset = Math.floorMod(ChronoUnit.DAYS.between(anchor, date), length)
            val start = date.minusDays(offset)
            Period(start, start.plusDays(length - 1))
        }
    }
}

/**
 * Everything the app knows, kept on the phone in SharedPreferences.
 * Nothing is ever sent anywhere; the app doesn't even have internet permission.
 */
class MealStore(context: Context) {
    private val prefs = context.getSharedPreferences("meal_sheet", Context.MODE_PRIVATE)

    var name by mutableStateOf(prefs.getString(KEY_NAME, "").orEmpty())
        private set
    var initials by mutableStateOf(prefs.getString(KEY_INITIALS, "").orEmpty())
        private set
    var prices by mutableStateOf(loadPrices())
        private set
    var payPeriod by mutableStateOf(
        prefs.getString(KEY_PAY_PERIOD, null)?.let { runCatching { PayPeriod.valueOf(it) }.getOrNull() } ?: PayPeriod.MONTHLY,
    )
        private set
    var payAnchor by mutableStateOf(
        prefs.getString(KEY_PAY_ANCHOR, null)?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
            ?: LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY)),
    )
        private set

    /** date -> (meal -> entry) */
    var log by mutableStateOf(loadLog())
        private set

    val isSetUp: Boolean get() = name.isNotBlank()
    val firstName: String get() = name.trim().substringBefore(' ')

    fun price(meal: Meal): Int = prices.getValue(meal)

    fun updateProfile(name: String, initials: String) {
        this.name = name
        this.initials = initials
        prefs.edit().putString(KEY_NAME, name).putString(KEY_INITIALS, initials).apply()
    }

    fun setPrice(meal: Meal, cents: Int) {
        prices = prices + (meal to cents)
        prefs.edit().putInt(priceKey(meal), cents).apply()
    }

    fun setAllPrices(cents: Int) = Meal.entries.forEach { setPrice(it, cents) }

    fun updatePaySchedule(period: PayPeriod, anchor: LocalDate = payAnchor) {
        payPeriod = period
        payAnchor = anchor
        prefs.edit().putString(KEY_PAY_PERIOD, period.name).putString(KEY_PAY_ANCHOR, anchor.toString()).apply()
    }

    fun mealsOn(date: LocalDate): Set<Meal> = log[date]?.keys ?: emptySet()

    fun entry(date: LocalDate, meal: Meal): Entry? = log[date]?.get(meal)

    fun toggle(date: LocalDate, meal: Meal) {
        val day = log[date].orEmpty().toMutableMap()
        if (day.remove(meal) == null) day[meal] = Entry(System.currentTimeMillis(), price(meal))
        log = if (day.isEmpty()) log - date else log + (date to day)
        persist()
    }

    fun periodContaining(date: LocalDate): Period = payPeriod.periodContaining(date, payAnchor)

    fun summarize(start: LocalDate, end: LocalDate): Summary {
        val counts = Meal.entries.associateWith { 0 }.toMutableMap()
        var cents = 0
        for ((date, meals) in log) {
            if (date.isBefore(start) || date.isAfter(end)) continue
            for ((meal, entry) in meals) {
                counts[meal] = counts.getValue(meal) + 1
                cents += entry.cents
            }
        }
        return Summary(counts, cents)
    }

    fun summarize(period: Period): Summary = summarize(period.start, period.end)

    val firstLoggedDate: LocalDate? get() = log.keys.minOrNull()

    /** Pay periods before the current one that have at least one meal, newest first. */
    fun earlierPeriods(today: LocalDate = LocalDate.now(), limit: Int = 52): List<Period> {
        val first = firstLoggedDate ?: return emptyList()
        val result = mutableListOf<Period>()
        var period = periodContaining(today)
        while (result.size < limit) {
            period = periodContaining(period.start.minusDays(1))
            if (period.end.isBefore(first)) break
            if (summarize(period).meals > 0) result += period
        }
        return result
    }

    /** All logged meals in a date range, oldest first. */
    fun entriesIn(start: LocalDate, end: LocalDate): List<Triple<LocalDate, Meal, Entry>> =
        log.filterKeys { !it.isBefore(start) && !it.isAfter(end) }
            .toSortedMap()
            .flatMap { (date, meals) -> Meal.entries.mapNotNull { m -> meals[m]?.let { Triple(date, m, it) } } }

    fun clearMeals() {
        log = emptyMap()
        persist()
    }

    /** Debug-build helper for screenshots: fills recent past days with plausible meals. */
    fun fillSampleMeals(today: LocalDate = LocalDate.now(), days: Int = 45) {
        val rnd = Random(today.toEpochDay().toInt())
        val updated = log.toMutableMap()
        for (daysAgo in 1..days) {
            val date = today.minusDays(daysAgo.toLong())
            if (date in updated || rnd.nextFloat() > 0.68f) continue
            val day = mutableMapOf<Meal, Entry>()
            if (rnd.nextFloat() < 0.45f) day[Meal.BREAKFAST] = Entry(at(date, 7, rnd.nextInt(10, 55)), price(Meal.BREAKFAST))
            if (rnd.nextFloat() < 0.85f) day[Meal.LUNCH] = Entry(at(date, 12, rnd.nextInt(0, 50)), price(Meal.LUNCH))
            if (rnd.nextFloat() < 0.25f) day[Meal.DINNER] = Entry(at(date, 17, rnd.nextInt(15, 55)), price(Meal.DINNER))
            if (day.isNotEmpty()) updated[date] = day
        }
        log = updated
        persist()
    }

    private fun at(date: LocalDate, hour: Int, minute: Int): Long =
        date.atTime(hour, minute).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

    private fun priceKey(meal: Meal) = "price_${meal.code}"

    private fun loadPrices(): Map<Meal, Int> {
        val legacy = prefs.getInt(KEY_LEGACY_PRICE, DEFAULT_PRICE_CENTS)
        return Meal.entries.associateWith { prefs.getInt(priceKey(it), legacy) }
    }

    private fun loadLog(): Map<LocalDate, Map<Meal, Entry>> {
        val fallbackPrices = loadPrices()
        val result = mutableMapOf<LocalDate, MutableMap<Meal, Entry>>()
        for (raw in prefs.getStringSet(KEY_LOG, emptySet()).orEmpty()) {
            val parts = raw.split('|')
            if (parts.size !in 3..4) continue
            val date = runCatching { LocalDate.parse(parts[0]) }.getOrNull() ?: continue
            val meal = Meal.entries.firstOrNull { it.code == parts[1] } ?: continue
            val time = parts[2].toLongOrNull() ?: continue
            val cents = parts.getOrNull(3)?.toIntOrNull() ?: fallbackPrices.getValue(meal)
            result.getOrPut(date) { mutableMapOf() }[meal] = Entry(time, cents)
        }
        return result
    }

    private fun persist() {
        val entries = log.flatMap { (date, meals) ->
            meals.map { (meal, e) -> "$date|${meal.code}|${e.at}|${e.cents}" }
        }.toSet()
        prefs.edit().putStringSet(KEY_LOG, entries).apply()
    }

    companion object {
        const val DEFAULT_PRICE_CENTS = 500
        private const val KEY_NAME = "name"
        private const val KEY_INITIALS = "initials"
        private const val KEY_LEGACY_PRICE = "price_cents"
        private const val KEY_PAY_PERIOD = "pay_period"
        private const val KEY_PAY_ANCHOR = "pay_anchor"
        private const val KEY_LOG = "log"
    }
}

fun initialsFrom(name: String): String =
    name.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }.take(3)
        .joinToString("") { it.first().uppercase() }

/** Parses "5", "5.5", "5.50" or "5,50" into cents. */
fun parseCents(text: String): Int? {
    val value = text.trim().replace(',', '.').toBigDecimalOrNull() ?: return null
    if (value.signum() < 0 || value > 10_000.toBigDecimal()) return null
    return value.movePointRight(2).setScale(0, java.math.RoundingMode.HALF_UP).toInt()
}

fun centsToInput(cents: Int): String = String.format(Locale.US, "%.2f", cents / 100.0)

object Fmt {
    private val currency = NumberFormat.getCurrencyInstance()
    val currencySymbol: String = runCatching { currency.currency?.getSymbol(Locale.getDefault()) }.getOrNull() ?: "$"
    val dayLong: DateTimeFormatter = DateTimeFormatter.ofPattern("EEEE, MMMM d", Locale.US)
    val dayShort: DateTimeFormatter = DateTimeFormatter.ofPattern("EEE, MMM d", Locale.US)
    val monthDay: DateTimeFormatter = DateTimeFormatter.ofPattern("MMM d", Locale.US)
    val fullDate: DateTimeFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.US)
    val monthYear: DateTimeFormatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.US)
    val monthName: DateTimeFormatter = DateTimeFormatter.ofPattern("MMMM", Locale.US)
    private val clock: DateTimeFormatter = DateTimeFormatter.ofPattern("h:mm a", Locale.US)

    fun money(cents: Int): String = currency.format(cents / 100.0)
    fun time(millis: Long): String = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).format(clock)
    fun dateOf(millis: Long): LocalDate = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
}
