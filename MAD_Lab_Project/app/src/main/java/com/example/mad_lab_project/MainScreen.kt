package com.example.mad_lab_project

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

@Composable
fun MainScreen(navController: NavHostController) {
    // Get the current context for accessing SharedPreferences
    val context = LocalContext.current

    // Load alarms from SharedPreferences into a state variable
    var alarms by remember { mutableStateOf(loadAlarms(context)) }

    // Scaffold with top bar and content
    Scaffold(
        topBar = { AlarmAppTopBar(navController) },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        if (alarms.isEmpty()) {
            // Display a message when no alarms are set
            Text(
                text = "No alarms set yet",
                modifier = Modifier.padding(innerPadding)
            )
        } else {
            // Display the alarms in a list using LazyColumn
            LazyColumn(modifier = Modifier.padding(innerPadding)) {
                items(alarms) { alarm ->
                    AlarmCard(alarm)
                }
            }
        }
    }
}

@Composable
fun AlarmCard(alarm: Alarm) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Alarm: ${alarm.time}", style = MaterialTheme.typography.headlineSmall)
            Text("Days: ${alarm.days.joinToString()}", style = MaterialTheme.typography.bodyMedium)
        }
    }
}

// Function to load alarms from SharedPreferences using Gson
fun loadAlarms(context: Context): List<Alarm> {
    val sharedPreferences: SharedPreferences = context.getSharedPreferences("alarms", Context.MODE_PRIVATE)
    val gson = Gson()
    val type = object : TypeToken<List<Alarm>>() {}.type
    return gson.fromJson(sharedPreferences.getString("alarms", "[]"), type) ?: emptyList()
}
