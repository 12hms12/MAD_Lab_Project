package com.example.mad_lab_project

import android.app.TimePickerDialog
import android.content.Context
import android.content.SharedPreferences
import android.widget.TimePicker
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.Calendar


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateAlarmScreen(navController: NavHostController) {
    var selectedTime by remember { mutableStateOf("Select Time") }
    val daysOfWeek = listOf("Su", "M", "Tu", "W", "Th", "F", "S")
    val selectedDays = remember { mutableStateMapOf<String, Boolean>().apply {
        daysOfWeek.forEach { this[it] = false }
    } }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Set Alarm") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Time Picker Button
            Button(onClick = { selectTime(navController.context) { time -> selectedTime = time } }) {
                Text(text = selectedTime)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Days Selection (Two Rows)
            Text("Repeat on:")

            // First Row (Mon-Fri)
            Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
                listOf("M", "Tu", "W", "Th", "F").forEach { day ->
                    DayToggle(day, selectedDays)
                }
            }

            // Second Row (Sat, Sun)
            Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
                listOf("S", "Su").forEach { day ->
                    DayToggle(day, selectedDays)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Save Alarm Button
            Button(onClick = {
                val selectedDaysList = selectedDays.filter { it.value }.keys.toList()
                saveAlarm(navController.context, selectedTime, selectedDaysList)
                navController.popBackStack() // Return to MainScreen after saving
            }) {
                Text("Save Alarm")
            }
        }
    }
}

@Composable
fun DayToggle(day: String, selectedDays: MutableMap<String, Boolean>) {
    TextButton(
        onClick = { selectedDays[day] = !(selectedDays[day] ?: false) }, // Toggle state
        modifier = Modifier.padding(4.dp)
    ) {
        Text(
            text = day,
            color = if (selectedDays[day] == true) androidx.compose.ui.graphics.Color.Green else androidx.compose.ui.graphics.Color.Red
        )
    }
}



fun selectTime(context: Context, onTimeSelected: (String) -> Unit) {
    val calendar = Calendar.getInstance()
    val hour = calendar.get(Calendar.HOUR_OF_DAY)
    val minute = calendar.get(Calendar.MINUTE)

    TimePickerDialog(context, { _: TimePicker, selectedHour: Int, selectedMinute: Int ->
        val formattedTime = String.format("%02d:%02d", selectedHour, selectedMinute)
        onTimeSelected(formattedTime)
    }, hour, minute, true).show()
}



fun saveAlarm(context: Context, time: String, days: List<String>) {
    val sharedPreferences: SharedPreferences = context.getSharedPreferences("alarms", Context.MODE_PRIVATE)
    val editor = sharedPreferences.edit()

    // Load existing alarms
    val gson = Gson()
    val type = object : TypeToken<MutableList<Alarm>>() {}.type
    val alarms: MutableList<Alarm> = gson.fromJson(sharedPreferences.getString("alarms", "[]"), type) ?: mutableListOf()

    // Add new alarm
    alarms.add(Alarm(time, days))

    // Save back to SharedPreferences
    editor.putString("alarms", gson.toJson(alarms))
    editor.apply()
}

// Alarm data class
data class Alarm(val time: String, val days: List<String>)

