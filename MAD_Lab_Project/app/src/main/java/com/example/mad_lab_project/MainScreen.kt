package com.example.mad_lab_project

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

@Composable
fun MainScreen(navController: NavHostController) {
    MaterialTheme(colorScheme = darkColorScheme()) {
        val context = LocalContext.current
        var alarms by remember { mutableStateOf(loadAlarms(context)) }
        var alarmToDelete by remember { mutableStateOf<Alarm?>(null) }
        var showDeleteDialog by remember { mutableStateOf(false) }

        fun refreshAlarms() {
            alarms = loadAlarms(context)
        }

        if (showDeleteDialog && alarmToDelete != null) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Delete Alarm") },
                text = { Text("Are you sure you want to delete this alarm?") },
                confirmButton = {
                    TextButton(onClick = {
                        deleteAlarm(context, alarmToDelete!!)
                        refreshAlarms()
                        showDeleteDialog = false
                    }) {
                        Text("Yes")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        Scaffold(
            topBar = { AlarmAppTopBar(navController) },
            modifier = Modifier.fillMaxSize()
        ) { innerPadding ->
            if (alarms.isEmpty()) {
                Text(text = "No alarms set yet", modifier = Modifier.padding(innerPadding))
            } else {
                LazyColumn(modifier = Modifier.padding(innerPadding)) {
                    items(alarms) { alarm ->
                        AlarmCard(
                            alarm = alarm,
                            onEdit = { selectedAlarm ->
                                AlarmHolder.alarmToEdit = selectedAlarm
                                navController.navigate("create_alarm_screen")
                            },
                            onDelete = { selectedAlarm ->
                                alarmToDelete = selectedAlarm
                                showDeleteDialog = true
                            },
                            onToggle = { selectedAlarm, isEnabled ->
                                updateAlarmState(context, selectedAlarm, isEnabled)
                                refreshAlarms()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AlarmCard(
    alarm: Alarm,
    onEdit: (Alarm) -> Unit,
    onDelete: (Alarm) -> Unit,
    onToggle: (Alarm, Boolean) -> Unit
) {
    val context = LocalContext.current
    var isEnabled by remember { mutableStateOf(loadAlarmState(context, alarm)) }
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(alarm.time, style = MaterialTheme.typography.headlineSmall)
                    Text("Days: ${alarm.days.joinToString()}", style = MaterialTheme.typography.bodyMedium)
                }
                Row {
                    // Switch to toggle alarm ON/OFF
                    Switch(
                        checked = isEnabled,
                        onCheckedChange = { newState ->
                            isEnabled = newState
                            onToggle(alarm, newState)
                        }
                    )
                    // Options button with dropdown menu.
                    IconButton(onClick = { expanded = true }) {
                        Icon(imageVector = Icons.Filled.MoreVert, contentDescription = "Options")
                    }
                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        DropdownMenuItem(
                            text = { Text("Edit") },
                            onClick = {
                                expanded = false
                                onEdit(alarm)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete") },
                            onClick = {
                                expanded = false
                                onDelete(alarm)
                            }
                        )
                    }
                }
            }
        }
    }
}


fun loadAlarms(context: Context): List<Alarm> {
    val sharedPreferences: SharedPreferences = context.getSharedPreferences("alarms", Context.MODE_PRIVATE)
    val gson = Gson()
    val type = object : TypeToken<List<Alarm>>() {}.type
    return gson.fromJson(sharedPreferences.getString("alarms", "[]"), type) ?: emptyList()
}

fun deleteAlarm(context: Context, alarmToDelete: Alarm) {
    val sharedPreferences: SharedPreferences = context.getSharedPreferences("alarms", Context.MODE_PRIVATE)
    val gson = Gson()
    val type = object : TypeToken<MutableList<Alarm>>() {}.type
    val alarms: MutableList<Alarm> = gson.fromJson(sharedPreferences.getString("alarms", "[]"), type)
        ?: mutableListOf()
    alarms.removeAll { it == alarmToDelete }
    sharedPreferences.edit().apply {
        putString("alarms", gson.toJson(alarms))
        apply()
    }
}

fun updateAlarmState(context: Context, alarm: Alarm, isEnabled: Boolean) {
    val sharedPreferences: SharedPreferences = context.getSharedPreferences("alarm_states", Context.MODE_PRIVATE)
    sharedPreferences.edit().putBoolean(alarm.time, isEnabled).apply()
}

fun loadAlarmState(context: Context, alarm: Alarm): Boolean {
    val sharedPreferences: SharedPreferences = context.getSharedPreferences("alarm_states", Context.MODE_PRIVATE)
    return sharedPreferences.getBoolean(alarm.time, true) // default true
}

