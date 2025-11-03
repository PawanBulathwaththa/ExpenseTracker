package com.example.expensetracker2.ui.screens


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.expensetracker2.ui.components.ExpenseItem
import com.example.expensetracker2.ui.viewmodel.AuthViewModel
import com.example.expensetracker2.ui.viewmodel.ExpenseState
import com.example.expensetracker2.ui.viewmodel.ExpenseViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    authViewModel: AuthViewModel,
    expenseViewModel: ExpenseViewModel,
    onNavigateToAddExpense: () -> Unit,
    onLogout: () -> Unit
) {
    val userId = authViewModel.getCurrentUserId() ?: ""
    val expenses by expenseViewModel.expenses.collectAsState()
    val categoryTotals by expenseViewModel.categoryTotals.collectAsState()
    val expenseState by expenseViewModel.expenseState.collectAsState()
    val isSyncing by expenseViewModel.isSyncing.collectAsState()

    var showLogoutDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var expenseToDelete by remember { mutableStateOf<com.example.expensetracker2.data.model.Expense?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }

    // Load expenses on first launch
    LaunchedEffect(userId) {
        if (userId.isNotEmpty()) {
            expenseViewModel.loadExpenses(userId)
            expenseViewModel.loadCategoryTotals(userId)
        }
    }

    // Show snackbar for success/error messages
    LaunchedEffect(expenseState) {
        when (expenseState) {
            is ExpenseState.Success -> {
                snackbarHostState.showSnackbar((expenseState as ExpenseState.Success).message)
                expenseViewModel.resetExpenseState()
            }
            is ExpenseState.Error -> {
                snackbarHostState.showSnackbar((expenseState as ExpenseState.Error).message)
                expenseViewModel.resetExpenseState()
            }
            else -> {}
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("My Expenses") },
                actions = {
                    // Sync Button
                    IconButton(
                        onClick = {
                            if (userId.isNotEmpty()) {
                                expenseViewModel.syncExpenses(userId)
                            }
                        },
                        enabled = !isSyncing
                    ) {
                        if (isSyncing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.Default.Refresh, contentDescription = "Sync")
                        }
                    }

                    // Logout Button
                    IconButton(onClick = { showLogoutDialog = true }) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Logout")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToAddExpense) {
                Icon(Icons.Default.Add, contentDescription = "Add Expense")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Total Spending Card
            val totalSpending = expenses.sumOf { it.amount }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Total Spending",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Rs. ${String.format("%.2f", totalSpending)}",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${expenses.size} transactions",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            // Category Breakdown
            if (categoryTotals.isNotEmpty()) {
                Text(
                    text = "Spending by Category",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )

                categoryTotals.forEach { categoryTotal ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = categoryTotal.category,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "Rs. ${String.format("%.2f", categoryTotal.total)}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
            }

            // Expenses List
            if (expenses.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "No expenses yet",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Tap + to add your first expense",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn {
                    items(expenses) { expense ->
                        ExpenseItem(
                            expense = expense,
                            onDelete = {
                                expenseToDelete = it
                                showDeleteDialog = true
                            }
                        )
                    }
                }
            }
        }
    }

    // Logout Confirmation Dialog
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Logout") },
            text = { Text("Are you sure you want to logout?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        authViewModel.signOut()
                        showLogoutDialog = false
                        onLogout()
                    }
                ) {
                    Text("Logout")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Delete Confirmation Dialog
    if (showDeleteDialog && expenseToDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Expense") },
            text = { Text("Are you sure you want to delete this expense?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        expenseToDelete?.let { expenseViewModel.deleteExpense(it) }
                        showDeleteDialog = false
                        expenseToDelete = null
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        expenseToDelete = null
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}