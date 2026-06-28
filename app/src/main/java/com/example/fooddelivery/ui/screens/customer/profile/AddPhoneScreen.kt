package com.example.fooddelivery.ui.screens.customer.profile

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.fooddelivery.ui.components.button.DFoodButton
import com.example.fooddelivery.ui.components.layout.ScaffoldBottomBarSurface
import com.example.fooddelivery.ui.components.topbar.DFoodTopBar

@Composable
fun AddPhoneScreen(
    onNavigateBack: () -> Unit,
    onPhoneAdded: () -> Unit,
    viewModel: AddPhoneViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackBarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess) {
            onPhoneAdded()
            viewModel.onEvent(AddPhoneEvent.ResetSuccessState)
        }
    }

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let {
            if (it.isNotEmpty()) {
                snackBarHostState.showSnackbar(it)
                viewModel.onEvent(AddPhoneEvent.ErrorDismissed)
            }
        }
    }

    AddPhoneContent(
        state = state,
        onEvent = viewModel::onEvent,
        onNavigateBack = onNavigateBack,
        snackBarHostState = snackBarHostState
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPhoneContent(
    state: AddPhoneState,
    onEvent: (AddPhoneEvent) -> Unit,
    onNavigateBack: () -> Unit,
    snackBarHostState: SnackbarHostState
) {
    Scaffold(
        snackbarHost = { SnackbarHost(snackBarHostState) },
        topBar = {
            DFoodTopBar(
                title = "Add Phone Number",
                onBackClick = onNavigateBack,
                scrollBehavior = null
            )
        },
        bottomBar = {
            ScaffoldBottomBarSurface(shadowElevation = 16.dp, tonalElevation = 4.dp) {
                DFoodButton(
                    text = if (state.isLoading) "SAVING..." else "SAVE",
                    onClick = { onEvent(AddPhoneEvent.SaveClicked) },
                    isLoading = state.isLoading,
                    enabled = state.phone.isNotBlank() && !state.isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                )
            }
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Add a phone number so we can contact you about your deliveries.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            ProfileInputField(
                label = "Phone Number",
                value = state.phone,
                onValueChange = { onEvent(AddPhoneEvent.PhoneChanged(it)) },
                icon = Icons.Default.Phone,
                isError = state.phoneError != null,
                errorMessage = state.phoneError,
                enabled = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Phone,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = { onEvent(AddPhoneEvent.SaveClicked) }
                )
            )
        }
    }
}
