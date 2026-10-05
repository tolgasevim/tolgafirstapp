package com.tolgasevim.habitcounter

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate

data class Habit(val name: String, val count: Int, val date: String)

class HabitStore(context: Context) {
    private val prefs = context.getSharedPreferences("habits", Context.MODE_PRIVATE)
    private val today get() = LocalDate.now().toString()

    /** Loads habits; a habit's count resets to 0 when the day has changed. */
    fun load(): List<Habit> {
        val arr = JSONArray(prefs.getString("list", "[]"))
        return (0 until arr.length()).map {
            val o = arr.getJSONObject(it)
            val h = Habit(o.getString("name"), o.getInt("count"), o.getString("date"))
            if (h.date == today) h else h.copy(count = 0, date = today)
        }
    }

    fun save(habits: List<Habit>) {
        val arr = JSONArray()
        habits.forEach {
            arr.put(JSONObject().put("name", it.name).put("count", it.count).put("date", it.date))
        }
        prefs.edit().putString("list", arr.toString()).apply()
    }

    fun newHabit(name: String) = Habit(name, 0, today)
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val store = HabitStore(this)
        setContent { MaterialTheme { HabitApp(store) } }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitApp(store: HabitStore) {
    val habits = remember { mutableStateListOf<Habit>().apply { addAll(store.load()) } }
    var showAdd by remember { mutableStateOf(false) }

    fun update(index: Int, habit: Habit) {
        habits[index] = habit
        store.save(habits)
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Today's habits") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAdd = true }) { Text("+") }
        },
    ) { padding ->
        if (habits.isEmpty()) {
            Column(
                Modifier.fillMaxSize().padding(padding).padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text("No habits yet. Tap + to add one, like \"Drink water\".")
            }
        } else {
            LazyColumn(
                Modifier.fillMaxSize().padding(padding),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(habits.size) { i ->
                    val h = habits[i]
                    Card(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(16.dp)) {
                            Text(h.name, style = MaterialTheme.typography.titleMedium)
                            Row(
                                Modifier.fillMaxWidth().padding(top = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                OutlinedButton(
                                    onClick = { update(i, h.copy(count = maxOf(0, h.count - 1))) },
                                ) { Text("−") }
                                Text("${h.count}", style = MaterialTheme.typography.headlineMedium)
                                Button(onClick = { update(i, h.copy(count = h.count + 1)) }) { Text("+1") }
                                Row(Modifier.weight(1f), horizontalArrangement = Arrangement.End) {
                                    TextButton(onClick = {
                                        habits.removeAt(i)
                                        store.save(habits)
                                    }) { Text("Delete") }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAdd) {
        var name by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAdd = false },
            title = { Text("New habit") },
            text = {
                OutlinedTextField(name, { name = it }, singleLine = true, label = { Text("Name") })
            },
            confirmButton = {
                TextButton(
                    enabled = name.isNotBlank(),
                    onClick = {
                        habits.add(store.newHabit(name.trim()))
                        store.save(habits)
                        showAdd = false
                    },
                ) { Text("Add") }
            },
            dismissButton = { TextButton(onClick = { showAdd = false }) { Text("Cancel") } },
        )
    }
}
