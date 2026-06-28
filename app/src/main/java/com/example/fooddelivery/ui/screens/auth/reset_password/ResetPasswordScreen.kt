package com.example.fooddelivery.ui.screens.auth.reset_password

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.fooddelivery.R
import com.example.fooddelivery.ui.components.button.DFoodButton
import com.example.fooddelivery.ui.components.layout.NavigationBarBottomSpacer
import com.example.fooddelivery.ui.components.textfield.DFoodFTextField
import com.example.fooddelivery.ui.components.topbar.DFoodTopBar

@Composable
fun ResetPasswordScreen(
    resetToken: String,
    onNavigateBack: () -> Unit,
    onNavigateToLogin: () -> Unit,
    viewModel: ResetPasswordViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackBarHostState = remember { SnackbarHostState() }

    LaunchedEffect(resetToken) {
        viewModel.onEvent(ResetPasswordEvent.Init(resetToken))
    }

    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess) {
            onNavigateToLogin()
        }
    }

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let { message ->
            if (message.isNotEmpty()) {
                snackBarHostState.showSnackbar(message)
                viewModel.onEvent(ResetPasswordEvent.ErrorDismissed)
            }
        }
    }

    ResetPasswordContent(
        state = state,
        onEvent = viewModel::onEvent,
        onNavigateBack = onNavigateBack,
        snackBarHostState = snackBarHostState
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResetPasswordContent(
    state: ResetPasswordState,
    onEvent: (ResetPasswordEvent) -> Unit,
    onNavigateBack: () -> Unit,
    snackBarHostState: SnackbarHostState
) {
    Scaffold(
        snackbarHost = { SnackbarHost(snackBarHostState) },
        topBar = {
            DFoodTopBar(
                title = "Reset Password",
                onBackClick = onNavigateBack
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Create New Password",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Your new password must be different from previous used passwords.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Spacer(modifier = Modifier.height(48.dp))

            DFoodFTextField(
                value = state.newPassword,
                onValueChange = { onEvent(ResetPasswordEvent.NewPasswordChanged(it)) },
                label = "New Password",
                isPassword = true,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Lock,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                isError = state.passwordError != null,
                errorMessage = state.passwordError
            )

            Spacer(modifier = Modifier.height(20.dp))

            DFoodFTextField(
                value = state.confirmPassword,
                onValueChange = { onEvent(ResetPasswordEvent.ConfirmPasswordChanged(it)) },
                label = "Confirm Password",
                isPassword = true,
                leadingIcon = {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_lock_reset),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                isError = state.confirmPasswordError != null,
                errorMessage = state.confirmPasswordError
            )

            Spacer(modifier = Modifier.height(48.dp))

            DFoodButton(
                text = if (state.isLoading) "RESETTING..." else "RESET PASSWORD",
                onClick = { onEvent(ResetPasswordEvent.ResetPasswordClicked) },
                enabled = !state.isLoading
            )
            NavigationBarBottomSpacer()
        }
    }
}