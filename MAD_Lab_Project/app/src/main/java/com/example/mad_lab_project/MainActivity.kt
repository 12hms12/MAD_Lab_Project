package com.example.mad_lab_project

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.mad_lab_project.ui.theme.MAD_Lab_ProjectTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MAD_Lab_ProjectTheme {
                val navController = rememberNavController()
                AppNavigation(navController)
            }
        }
    }
}




@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmAppTopBar(navController: NavHostController) {
    TopAppBar(
        title = { Text("Alarmio") },
        actions = {
            IconButton(onClick = { navController.navigate(Screen.CreateAlarmScreen.route) }) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Add Alarm"
                )
            }
        }
    )
}

//@Preview(showBackground = true)
//@Composable
//fun AlarmAppTopBarPreview(){
//    MAD_Lab_ProjectTheme {
//        AlarmAppTopBar(onAddAlarmClicked = {})
//    }
//}