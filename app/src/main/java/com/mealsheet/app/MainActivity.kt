package com.mealsheet.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mealsheet.app.ui.DayEditorSheet
import com.mealsheet.app.ui.LogoMark
import com.mealsheet.app.ui.MealSheetTheme
import com.mealsheet.app.ui.MonthScreen
import com.mealsheet.app.ui.OnboardingScreen
import com.mealsheet.app.ui.SettingsScreen
import com.mealsheet.app.ui.TodayScreen
import com.mealsheet.app.ui.TotalsScreen
import java.time.LocalDate

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val store = MealStore(applicationContext)
        setContent {
            MealSheetTheme {
                MealSheetApp(store)
            }
        }
    }
}

private enum class Tab(val label: String, val icon: ImageVector) {
    Today("Today", Icons.Filled.Home),
    Calendar("Calendar", Icons.Filled.DateRange),
    Totals("Totals", Icons.AutoMirrored.Filled.List),
}

@Composable
fun MealSheetApp(store: MealStore) {
    if (!store.isSetUp) {
        OnboardingScreen(onDone = { name, initials, cents ->
            store.setAllPrices(cents)
            store.updateProfile(name, initials)
        })
    } else {
        MainScaffold(store)
    }
}

@Composable
private fun MainScaffold(store: MealStore) {
    var tab by rememberSaveable { mutableStateOf(Tab.Today) }
    var showSettings by rememberSaveable { mutableStateOf(false) }
    var editingDay by remember { mutableStateOf<LocalDate?>(null) }

    if (showSettings) {
        SettingsScreen(store, onBack = { showSettings = false })
        return
    }

    Scaffold(
        topBar = { AppBar(initials = store.initials, onProfile = { showSettings = true }) },
        bottomBar = {
            NavigationBar {
                Tab.entries.forEach { t ->
                    NavigationBarItem(
                        selected = tab == t,
                        onClick = { tab = t },
                        icon = { Icon(t.icon, contentDescription = null) },
                        label = { Text(t.label) },
                    )
                }
            }
        },
    ) { padding ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            when (tab) {
                Tab.Today -> TodayScreen(store, onOpenDay = { editingDay = it })
                Tab.Calendar -> MonthScreen(store, onOpenDay = { editingDay = it })
                Tab.Totals -> TotalsScreen(store, onOpenSettings = { showSettings = true })
            }
        }
    }

    editingDay?.let { day -> DayEditorSheet(store, day, onDismiss = { editingDay = null }) }
}

@Composable
private fun AppBar(initials: String, onProfile: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(start = 20.dp, end = 16.dp, top = 10.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        LogoMark(30.dp)
        Spacer(Modifier.width(10.dp))
        Text("Meal Sheet", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.weight(1f))
        Surface(
            onClick = onProfile,
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier
                .size(40.dp)
                .semantics { contentDescription = "Settings" },
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    initials,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
        }
    }
}
