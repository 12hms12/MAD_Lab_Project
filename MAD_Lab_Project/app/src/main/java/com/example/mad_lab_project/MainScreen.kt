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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

// Data class for Alarm. Ensure this is declared only once.
//data class Alarm(val time: String, val days: List<String>)

@Composable
fun MainScreen(navController: NavHostController) {
    // Wrap UI in a dark theme for improved appearance
    MaterialTheme(colorScheme = darkColorScheme()) {
        val context = LocalContext.current
        // Load alarms from SharedPreferences into a state variable
        var alarms by remember { mutableStateOf(loadAlarms(context)) }

        // States for handling dialogs:
        var alarmToEdit by remember { mutableStateOf<Alarm?>(null) }
        var alarmToDelete by remember { mutableStateOf<Alarm?>(null) }
        var showEditDialog by remember { mutableStateOf(false) }
        var showDeleteDialog by remember { mutableStateOf(false) }

        // Refresh alarms list (for example after deletion)
        fun refreshAlarms() {
            alarms = loadAlarms(context)
        }

        // Delete confirmation dialog: appears when delete icon is tapped.
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

        // Edit dialog placeholder: appears when edit icon is tapped.
        if (showEditDialog && alarmToEdit != null) {
            AlertDialog(
                onDismissRequest = { showEditDialog = false },
                title = { Text("Edit Alarm") },
                text = { Text("Edit functionality is not implemented yet.") },
                confirmButton = {
                    TextButton(onClick = { showEditDialog = false }) {
                        Text("OK")
                    }
                }
            )
        }

        Scaffold(
            topBar = { AlarmAppTopBar(navController) },
            modifier = Modifier.fillMaxSize()
        ) { innerPadding ->
            if (alarms.isEmpty()) {
                // If no alarms, display a message.
                Text(
                    text = "No alarms set yet",
                    modifier = Modifier.padding(innerPadding)
                )
            } else {
                // Display alarms using LazyColumn
                LazyColumn(modifier = Modifier.padding(innerPadding)) {
                    items(alarms) { alarm ->
                        AlarmCard(
                            alarm = alarm,
                            onEdit = { selectedAlarm ->
                                // Set the alarm to edit and show the edit dialog.
                                alarmToEdit = selectedAlarm
                                showEditDialog = true
                            },
                            onDelete = { selectedAlarm ->
                                // Set the alarm to delete and show the delete confirmation dialog.
                                alarmToDelete = selectedAlarm
                                showDeleteDialog = true
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AlarmCard(alarm: Alarm, onEdit: (Alarm) -> Unit, onDelete: (Alarm) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Display alarm details and action icons side by side.
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Alarm: ${alarm.time}", style = MaterialTheme.typography.headlineSmall)
                    Text("Days: ${alarm.days.joinToString()}", style = MaterialTheme.typography.bodyMedium)
                }
                Row {
                    // Edit icon button: triggers the edit dialog.
                    IconButton(onClick = { onEdit(alarm) }) {
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = "Edit Alarm"
                        )
                    }
                    // Delete icon button: triggers the delete confirmation dialog.
                    IconButton(onClick = { onDelete(alarm) }) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = "Delete Alarm"
                        )
                    }
                }
            }
        }
    }
}

// Function to load alarms from SharedPreferences using Gson.
fun loadAlarms(context: Context): List<Alarm> {
    val sharedPreferences: SharedPreferences = context.getSharedPreferences("alarms", Context.MODE_PRIVATE)
    val gson = Gson()
    val type = object : TypeToken<List<Alarm>>() {}.type
    return gson.fromJson(sharedPreferences.getString("alarms", "[]"), type) ?: emptyList()
}

// Function to delete an alarm from SharedPreferences.
fun deleteAlarm(context: Context, alarmToDelete: Alarm) {
    val sharedPreferences: SharedPreferences = context.getSharedPreferences("alarms", Context.MODE_PRIVATE)
    val gson = Gson()
    val type = object : TypeToken<MutableList<Alarm>>() {}.type
    val alarms: MutableList<Alarm> = gson.fromJson(sharedPreferences.getString("alarms", "[]"), type)
        ?: mutableListOf()

    // Remove the selected alarm.
    alarms.removeAll { it == alarmToDelete }

    // Save the updated list back to SharedPreferences.
    sharedPreferences.edit().apply {
        putString("alarms", gson.toJson(alarms))
        apply()
    }
}

