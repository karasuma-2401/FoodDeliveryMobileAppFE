package com.example.fooddelivery.ui.screens.restaurant.food_management

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.fooddelivery.ui.components.textfield.DFoodFTextField
import com.example.fooddelivery.ui.screens.restaurant.component.DFoodActionTopBar
import com.example.fooddelivery.ui.screens.restaurant.component.DFoodImagePicker
import com.example.fooddelivery.ui.screens.restaurant.component.DFoodSectionLabel
import com.example.fooddelivery.ui.screens.restaurant.component.DFoodTextArea
import com.example.fooddelivery.ui.theme.DFoodTheme

@Composable
fun AddFoodScreen(
    onNavigateBack: () -> Unit,
    viewModel: AddFoodViewModel = hiltViewModel()
) {
    val state by viewModel.state

    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess) {
            onNavigateBack()
        }
    }

    AddFoodScreenContent(
        state = state,
        onNameChange = viewModel::onNameChange,
        onPriceChange = viewModel::onPriceChange,
        onDetailsChange = viewModel::onDetailsChange,
        onSaveClick = viewModel::saveFoodItem,
        onResetClick = viewModel::resetState,
        onNavigateBack = onNavigateBack
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFoodScreenContent(
    state: AddFoodState,
    onNameChange: (String) -> Unit,
    onPriceChange: (String) -> Unit,
    onDetailsChange: (String) -> Unit,
    onSaveClick: () -> Unit,
    onResetClick: () -> Unit,
    onNavigateBack: () -> Unit
) {
    Scaffold(
        topBar = {
            DFoodActionTopBar(
                title = "Add New Items",
                actionText = "RESET",
                onActionClick = onResetClick,
                onBackClick = onNavigateBack
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(modifier = Modifier.height(24.dp))

                DFoodSectionLabel(text = "ITEM NAME")
                DFoodFTextField(value = state.itemName, onValueChange = onNameChange, label = "Enter Food Name")

                Spacer(modifier = Modifier.height(24.dp))

                DFoodSectionLabel(text = "UPLOAD PHOTO/VIDEO")
                DFoodImagePicker()

                Spacer(modifier = Modifier.height(24.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.weight(1f)) {
                        DFoodSectionLabel(text = "PRICE")
                        DFoodFTextField(value = state.price, onValueChange = onPriceChange, label = "$50")
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                DFoodSectionLabel(text = "DETAILS")
                DFoodTextArea(value = state.details, onValueChange = onDetailsChange, placeholder = "Enter food details...")

                Spacer(modifier = Modifier.height(48.dp))

                Button(
                    onClick = onSaveClick,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = MaterialTheme.shapes.medium,
                    enabled = !state.isLoading,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                    } else {
                        Text(text = "SAVE CHANGES")
                    }
                }

                if (state.error != null) {
                    Text(
                        text = state.error!!,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 8.dp).align(Alignment.CenterHorizontally)
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun AddFoodScreenPreview() {
    DFoodTheme {
        AddFoodScreenContent(
            state = AddFoodState(),
            onNameChange = {},
            onPriceChange = {},
            onDetailsChange = {},
            onSaveClick = {},
            onResetClick = {},
            onNavigateBack = {}
        )
    }
}
