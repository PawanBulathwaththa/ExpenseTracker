package com.example.expensetracker2.ui.screens


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.expensetracker2.ui.components.CategorySelector
import com.example.expensetracker2.ui.viewmodel.AuthViewModel
import com.example.expensetracker2.ui.viewmodel.ExpenseState
import com.example.expensetracker2.ui.viewmodel.ExpenseViewModel
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseScreen(
    expenseViewModel: ExpenseViewModel,
    authViewModel: AuthViewModel,
    onNavigateBack: () -> Unit
) {
    val userId = authViewModel.getCurrentUserId() ?: ""

    var amount by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }

    val expenseState by expenseViewModel.expenseState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(expenseState) {
        when (expenseState) {
            is ExpenseState.Success -> {
                snackbarHostState.showSnackbar((expenseState as ExpenseState.Success).message)
                expenseViewModel.resetExpenseState()
                onNavigateBack()
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
                title = { Text("Add Expense") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Amount Field
            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text("Amount (Rs.)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // Category Selector
            CategorySelector(
                selectedCategory = category,
                onCategorySelected = { category = it },
                modifier = Modifier.fillMaxWidth()
            )

            // Note Field
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Note (Optional)") },
                minLines = 3,
                maxLines = 5,
                modifier = Modifier.fillMaxWidth()
            )

            // TODO: Camera button will be added in next step

            Spacer(modifier = Modifier.weight(1f))

            // Save Button
            Button(
                onClick = {
                    val amountValue = amount.toDoubleOrNull() ?: 0.0
                    expenseViewModel.addExpense(
                        userId = userId,
                        amount = amountValue,
                        category = category,
                        note = note,
                        date = Date()
                    )
                },
                enabled = expenseState !is ExpenseState.Loading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                if (expenseState is ExpenseState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Save Expense", style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }
}
