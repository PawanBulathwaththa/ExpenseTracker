package com.example.expensetracker2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.expensetracker2.ui.navigation.NavGraph
import com.example.expensetracker2.ui.theme.Expensetracker2Theme
import com.example.expensetracker2.ui.viewmodel.ViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val viewModelFactory = ViewModelFactory(applicationContext)

        setContent {
            Expensetracker2Theme {
                NavGraph(viewModelFactory = viewModelFactory)
            }
        }
    }
}