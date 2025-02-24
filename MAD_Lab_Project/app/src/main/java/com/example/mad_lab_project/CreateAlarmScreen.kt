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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.Calendar

// Global holder to pass an alarm to the CreateAlarmScreen for editing.
object AlarmHolder {
    var alarmToEdit: Alarm? = null
}

// Data class for Alarm (declared only once)
data class Alarm(val time: String, val days: List<String>)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateAlarmScreen(navController: NavHostController) {
    MaterialTheme(colorScheme = darkColorScheme()) {
        val context = LocalContext.current
        val editingAlarm = AlarmHolder.alarmToEdit

        // Get current time for new alarms; if editing, use the alarm's set time.
        val currentTime = Calendar.getInstance().let {
            String.format("%02d:%02d", it.get(Calendar.HOUR_OF_DAY), it.get(Calendar.MINUTE))
        }
        var selectedTime by remember { mutableStateOf(editingAlarm?.time ?: currentTime) }

        val daysOfWeek = listOf("Su", "M", "Tu", "W", "Th", "F", "S")
        val selectedDays = remember {
            mutableStateMapOf<String, Boolean>().apply {
                daysOfWeek.forEach { day ->
                    this[day] = editingAlarm?.days?.contains(day) ?: false
                }
            }
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(if (editingAlarm != null) "Edit Alarm" else "Set Alarm") },
                    navigationIcon = {
                        IconButton(onClick = {
                            // Clear edit state when canceling.
                            AlarmHolder.alarmToEdit = null
                            navController.popBackStack()
                        }) {
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
                // Time Picker Button – shows the alarm's set time if editing.
                Button(onClick = { selectTime(context) { time -> selectedTime = time } }) {
                    Text(text = selectedTime)
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text("Repeat on:")

                // First Row (Mon-Fri)
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    listOf("M", "Tu", "W", "Th", "F").forEach { day ->
                        DayToggle(day, selectedDays)
                    }
                }

                // Second Row (Sat, Su)
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    listOf("S", "Su").forEach { day ->
                        DayToggle(day, selectedDays)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(onClick = {
                    val selectedDaysList = selectedDays.filter { it.value }.keys.toList()
                    if (editingAlarm != null) {
                        updateAlarm(context, editingAlarm, selectedTime, selectedDaysList)
                        AlarmHolder.alarmToEdit = null
                    } else {
                        saveAlarm(context, selectedTime, selectedDaysList)
                    }
                    navController.popBackStack() // Return to MainScreen after saving.
                }) {
                    Text("Save Alarm")
                }
            }
        }
    }
}

@Composable
fun DayToggle(day: String, selectedDays: MutableMap<String, Boolean>) {
    TextButton(
        onClick = { selectedDays[day] = !(selectedDays[day] ?: false) },
        modifier = Modifier.padding(4.dp)
    ) {
        Text(
            text = day,
            color = if (selectedDays[day] == true) Color.Green else Color.Red
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
    val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("alarms", Context.MODE_PRIVATE)
    val editor = sharedPreferences.edit()
    val gson = Gson()
    val type = object : TypeToken<MutableList<Alarm>>() {}.type
    val alarms: MutableList<Alarm> =
        gson.fromJson(sharedPreferences.getString("alarms", "[]"), type) ?: mutableListOf()
    alarms.add(Alarm(time, days))
    editor.putString("alarms", gson.toJson(alarms))
    editor.apply()
}

fun updateAlarm(context: Context, oldAlarm: Alarm, newTime: String, newDays: List<String>) {
    val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("alarms", Context.MODE_PRIVATE)
    val gson = Gson()
    val type = object : TypeToken<MutableList<Alarm>>() {}.type
    val alarms: MutableList<Alarm> =
        gson.fromJson(sharedPreferences.getString("alarms", "[]"), type) ?: mutableListOf()
    alarms.removeAll { it == oldAlarm }
    alarms.add(Alarm(newTime, newDays))
    sharedPreferences.edit().apply {
        putString("alarms", gson.toJson(alarms))
        apply()
    }
}

