package com.example.expensetracker2.ui.screens

import android.Manifest
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.example.expensetracker2.ui.components.CameraCapture
import com.example.expensetracker2.ui.components.CategorySelector
import com.example.expensetracker2.ui.viewmodel.AuthViewModel
import com.example.expensetracker2.ui.viewmodel.ExpenseState
import com.example.expensetracker2.ui.viewmodel.ExpenseViewModel
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
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
    var photoUri by remember { mutableStateOf<Uri?>(null) }
    var showCamera by remember { mutableStateOf(false) }

    val expenseState by expenseViewModel.expenseState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Camera permission
    val cameraPermissionState = rememberPermissionState(permission = Manifest.permission.CAMERA)

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

    if (showCamera) {
        CameraCapture(
            onImageCaptured = { uri ->
                photoUri = uri
                showCamera = false
            },
            onError = { error ->
                showCamera = false
                // Show error
            },
            onClose = {
                showCamera = false
            }
        )
    } else {
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

                // Photo Section
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Receipt Photo (Optional)",
                            style = MaterialTheme.typography.titleMedium
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        if (photoUri != null) {
                            Box {
                                Image(
                                    painter = rememberAsyncImagePainter(photoUri),
                                    contentDescription = "Receipt photo",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(200.dp),
                                    contentScale = ContentScale.Crop
                                )

                                IconButton(
                                    onClick = { photoUri = null },
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(8.dp),
                                    colors = IconButtonDefaults.iconButtonColors(
                                        containerColor = MaterialTheme.colorScheme.error
                                    )
                                ) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Remove photo",
                                        tint = MaterialTheme.colorScheme.onError
                                    )
                                }
                            }
                        } else {
                            OutlinedButton(
                                onClick = {
                                    if (cameraPermissionState.status.isGranted) {
                                        showCamera = true
                                    } else {
                                        cameraPermissionState.launchPermissionRequest()
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    Icons.Default.CameraAlt,
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Take Photo")
                            }
                        }
                    }
                }

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
                            photoUri = photoUri?.toString(),
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
}