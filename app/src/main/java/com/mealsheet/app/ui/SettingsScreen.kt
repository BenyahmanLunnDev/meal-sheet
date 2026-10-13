package com.mealsheet.app.ui

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mealsheet.app.BuildConfig
import com.mealsheet.app.Fmt
import com.mealsheet.app.Meal
import com.mealsheet.app.MealStore
import com.mealsheet.app.PayPeriod
import com.mealsheet.app.centsToInput
import com.mealsheet.app.parseCents
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

/** Link shown under About. Leave blank to hide it. */
private const val SOURCE_CODE_URL = "https://github.com/BenyahmanLunnDev/meal-sheet"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(store: MealStore, onBack: () -> Unit) {
    BackHandler(onBack = onBack)
    val context = LocalContext.current
    var name by rememberSaveable { mutableStateOf(store.name) }
    var initials by rememberSaveable { mutableStateOf(store.initials) }
    var confirmClear by remember { mutableStateOf(false) }
    var pickStartDate by remember { mutableStateOf(false) }

    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(Modifier.fillMaxSize()) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(start = 4.dp, end = 16.dp, top = 6.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Text("Settings", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
            }

            Column(
                Modifier
                    .weight(1f)
                    .imePadding()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp),
            ) {
                Section("You") {
                    OutlinedTextField(
                        value = name,
                        onValueChange = {
                            name = it
                            if (it.isNotBlank()) store.updateProfile(it.trim(), store.initials)
                        },
                        label = { Text("Name") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                        modifier = Modifier.fillMaxWidth(),
                    )
                    OutlinedTextField(
                        value = initials,
                        onValueChange = {
                            val cleaned = it.filter(Char::isLetter).uppercase().take(3)
                            initials = cleaned
                            if (cleaned.isNotBlank()) store.updateProfile(store.name, cleaned)
                        },
                        label = { Text("Initials") },
                        supportingText = { Text("Stamped on each meal you log") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                HorizontalDivider()
                Section("Price per meal", "New prices apply to meals you log from now on.") {
                    Meal.entries.forEach { PriceRow(store, it) }
                }

                HorizontalDivider()
                Section("Pay schedule", "Groups your meals the same way your paychecks do.") {
                    PayPeriod.entries.forEach { period ->
                        RadioRow(
                            selected = store.payPeriod == period,
                            title = period.label,
                            detail = period.detail,
                            onClick = { store.updatePaySchedule(period) },
                        )
                    }
                    if (store.payPeriod.usesStartDate) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("A pay period started on", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                            TextButton(onClick = { pickStartDate = true }) { Text(store.payAnchor.format(Fmt.fullDate)) }
                        }
                        Text(
                            "Current period: ${store.periodContaining(LocalDate.now()).label}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                HorizontalDivider()
                Section("Your data") {
                    OutlinedButton(
                        onClick = { shareText(context, "Meal Sheet — all meals (CSV)", buildCsv(store)) },
                        enabled = store.firstLoggedDate != null,
                        modifier = Modifier.fillMaxWidth(),
                    ) { Text("Export all meals (CSV)") }
                    if (BuildConfig.DEBUG) {
                        OutlinedButton(
                            onClick = {
                                store.fillSampleMeals()
                                Toast.makeText(context, "Added sample meals", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.fillMaxWidth(),
                        ) { Text("Fill past days with sample meals (debug)") }
                    }
                    TextButton(
                        onClick = { confirmClear = true },
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                    ) { Text("Clear all meals") }
                }

                HorizontalDivider()
                Section("About") {
                    Text(
                        "No ads, no account, no tracking. Meal Sheet doesn't have internet access, " +
                            "so everything you log stays on this phone.",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Text(
                        "Made by @BenyahmanLunnDev. Free and open source under the GPL-3.0 license.",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Text(
                        "Version ${BuildConfig.VERSION_NAME}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    if (SOURCE_CODE_URL.isNotBlank()) {
                        TextButton(onClick = {
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(SOURCE_CODE_URL)))
                        }) { Text("Source code") }
                    }
                }
                Spacer(Modifier.navigationBarsPadding().height(16.dp))
            }
        }
    }

    if (pickStartDate) {
        val state = rememberDatePickerState(
            initialSelectedDateMillis = store.payAnchor.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli(),
        )
        DatePickerDialog(
            onDismissRequest = { pickStartDate = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let {
                        store.updatePaySchedule(store.payPeriod, Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate())
                    }
                    pickStartDate = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { pickStartDate = false }) { Text("Cancel") } },
        ) {
            DatePicker(state = state)
        }
    }

    if (confirmClear) {
        AlertDialog(
            onDismissRequest = { confirmClear = false },
            title = { Text("Clear all meals?") },
            text = { Text("This deletes every meal logged on this phone. Your name, prices, and pay schedule stay.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        store.clearMeals()
                        confirmClear = false
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                ) { Text("Clear") }
            },
            dismissButton = { TextButton(onClick = { confirmClear = false }) { Text("Cancel") } },
        )
    }
}

@Composable
private fun Section(title: String, subtitle: String? = null, content: @Composable ColumnScope.() -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Column {
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
            if (subtitle != null) {
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        content()
    }
}

@Composable
private fun PriceRow(store: MealStore, meal: Meal) {
    var text by rememberSaveable(meal) { mutableStateOf(centsToInput(store.price(meal))) }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(meal.emoji, fontSize = 22.sp)
        Spacer(Modifier.width(12.dp))
        Text(meal.label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        OutlinedTextField(
            value = text,
            onValueChange = { input ->
                val cleaned = input.filter { it.isDigit() || it == '.' || it == ',' }.take(7)
                text = cleaned
                parseCents(cleaned)?.let { store.setPrice(meal, it) }
            },
            prefix = { Text(Fmt.currencySymbol) },
            isError = parseCents(text) == null,
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.width(140.dp),
        )
    }
}

@Composable
private fun RadioRow(selected: Boolean, title: String, detail: String, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .selectable(selected = selected, onClick = onClick, role = Role.RadioButton)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(selected = selected, onClick = null)
        Spacer(Modifier.width(12.dp))
        Column {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(detail, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
