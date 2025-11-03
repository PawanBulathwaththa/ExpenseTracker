package com.example.expensetracker2.ui.navigation


import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.expensetracker2.ui.screens.AddExpenseScreen
import com.example.expensetracker2.ui.screens.DashboardScreen
import com.example.expensetracker2.ui.screens.LoginScreen
import com.example.expensetracker2.ui.screens.RegisterScreen
import com.example.expensetracker2.ui.viewmodel.AuthViewModel
import com.example.expensetracker2.ui.viewmodel.ExpenseViewModel
import com.example.expensetracker2.ui.viewmodel.ViewModelFactory

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Dashboard : Screen("dashboard")
    object AddExpense : Screen("add_expense")
}

@Composable
fun NavGraph(
    navController: NavHostController = rememberNavController(),
    viewModelFactory: ViewModelFactory
) {
    val authViewModel: AuthViewModel = viewModel(factory = viewModelFactory)
    val expenseViewModel: ExpenseViewModel = viewModel(factory = viewModelFactory)

    val isUserLoggedIn by authViewModel.isUserLoggedIn.collectAsState()

    val startDestination = if (isUserLoggedIn) {
        Screen.Dashboard.route
    } else {
        Screen.Login.route
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                authViewModel = authViewModel,
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                },
                onLoginSuccess = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                authViewModel = authViewModel,
                onNavigateToLogin = {
                    navController.popBackStack()
                },
                onRegisterSuccess = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Dashboard.route) {
            DashboardScreen(
                authViewModel = authViewModel,
                expenseViewModel = expenseViewModel,
                onNavigateToAddExpense = {
                    navController.navigate(Screen.AddExpense.route)
                },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.AddExpense.route) {
            AddExpenseScreen(
                expenseViewModel = expenseViewModel,
                authViewModel = authViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}