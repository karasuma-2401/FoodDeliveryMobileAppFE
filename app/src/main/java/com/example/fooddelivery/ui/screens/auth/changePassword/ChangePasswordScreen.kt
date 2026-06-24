package com.example.fooddelivery.ui.screens.auth.changePassword

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.fooddelivery.ui.components.button.DFoodButton
import com.example.fooddelivery.ui.components.textfield.DFoodFTextField
import com.example.fooddelivery.ui.components.topbar.DFoodTopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangePasswordScreen(
    onNavigateBack: () -> Unit,
    viewModel: ChangePasswordViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackBarHostState = remember { SnackbarHostState() }
    val focusManager = LocalFocusManager.current

    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess) {
            snackBarHostState.showSnackbar("Password updated successfully")
            onNavigateBack()
            viewModel.onEvent(ChangePasswordEvent.ResetSuccessState)
        }
    }

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let {
            if (it.isNotEmpty()) {
                snackBarHostState.showSnackbar(it)
                viewModel.onEvent(ChangePasswordEvent.ErrorDismissed)
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackBarHostState) },
        topBar = {
            DFoodTopBar(
                title = "Change Password",
                onBackClick = onNavigateBack,
                scrollBehavior = null
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 16.dp,
                tonalElevation = 4.dp
            ) {
                DFoodButton(
                    text = "UPDATE PASSWORD",
                    onClick = {
                        focusManager.clearFocus()
                        viewModel.onEvent(ChangePasswordEvent.SaveClicked)
                    },
                    isLoading = state.isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.Start
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Secure Your Account",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Your new password must be different from previous used passwords.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 24.sp
            )

            Spacer(modifier = Modifier.height(40.dp))

            PasswordInputField(
                label = "Current Password",
                value = state.currentPassword,
                onValueChange = { viewModel.onEvent(ChangePasswordEvent.CurrentPasswordChanged(it)) },
                errorMessage = state.currentPasswordError,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Next
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            PasswordInputField(
                label = "New Password",
                value = state.newPassword,
                onValueChange = { viewModel.onEvent(ChangePasswordEvent.NewPasswordChanged(it)) },
                errorMessage = state.newPasswordError,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Next
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            PasswordInputField(
                label = "Confirm New Password",
                value = state.confirmPassword,
                onValueChange = { viewModel.onEvent(ChangePasswordEvent.ConfirmPasswordChanged(it)) },
                errorMessage = state.confirmPasswordError,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(onDone = {
                    focusManager.clearFocus()
                    viewModel.onEvent(ChangePasswordEvent.SaveClicked)
                })
            )

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
private fun PasswordInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    errorMessage: String?,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default
) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        DFoodFTextField(
            value = value,
            onValueChange = onValueChange,
            label = "Enter $label",
            isPassword = true,
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.Lock,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            isError = errorMessage != null,
            errorMessage = errorMessage,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
