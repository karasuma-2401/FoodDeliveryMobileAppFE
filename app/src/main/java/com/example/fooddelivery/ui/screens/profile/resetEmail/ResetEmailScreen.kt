package com.example.fooddelivery.ui.screens.profile.resetEmail

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Phone
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
fun ResetEmailScreen(
    onNavigateBack: () -> Unit,
    viewModel: ResetEmailViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackBarHostState = remember { SnackbarHostState() }
    val focusManager = LocalFocusManager.current

    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess) {
            snackBarHostState.showSnackbar("Email updated successfully")
            onNavigateBack()
        }
    }

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let {
            if (it.isNotEmpty()) {
                snackBarHostState.showSnackbar(it)
                viewModel.onEvent(ResetEmailEvent.ErrorDismissed)
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackBarHostState) },
        topBar = {
            DFoodTopBar(
                title = "Change Email",
                onBackClick = onNavigateBack,
                scrollBehavior = null
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
            horizontalAlignment = Alignment.Start
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Update Your Email",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = if (!state.isOtpSent) 
                    "To change your email, please verify your identity first." 
                else 
                    "We've sent an OTP to your phone. Please enter it along with your new email.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 24.sp
            )

            Spacer(modifier = Modifier.height(40.dp))

            AnimatedVisibility(
                visible = !state.isOtpSent,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Column {
                    Text(
                        text = "Phone Number",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    DFoodFTextField(
                        value = state.phone,
                        onValueChange = { viewModel.onEvent(ResetEmailEvent.PhoneChanged(it)) },
                        label = "Enter registered phone",
                        leadingIcon = { Icon(Icons.Outlined.Phone, null, tint = MaterialTheme.colorScheme.primary) },
                        isError = state.phoneError != null,
                        errorMessage = state.phoneError,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Next)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "Current Password",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    DFoodFTextField(
                        value = state.password,
                        onValueChange = { viewModel.onEvent(ResetEmailEvent.PasswordChanged(it)) },
                        label = "Enter your password",
                        isPassword = true,
                        leadingIcon = { Icon(Icons.Outlined.Lock, null, tint = MaterialTheme.colorScheme.primary) },
                        isError = state.passwordError != null,
                        errorMessage = state.passwordError,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { viewModel.onEvent(ResetEmailEvent.RequestOtpClicked) })
                    )

                    Spacer(modifier = Modifier.height(48.dp))

                    DFoodButton(
                        text = "GET OTP",
                        onClick = { viewModel.onEvent(ResetEmailEvent.RequestOtpClicked) },
                        isLoading = state.isLoading,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            AnimatedVisibility(
                visible = state.isOtpSent,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Column {
                    Text(
                        text = "New Email",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    DFoodFTextField(
                        value = state.newEmail,
                        onValueChange = { viewModel.onEvent(ResetEmailEvent.NewEmailChanged(it)) },
                        label = "Enter new email address",
                        leadingIcon = { Icon(Icons.Outlined.Email, null, tint = MaterialTheme.colorScheme.primary) },
                        isError = state.newEmailError != null,
                        errorMessage = state.newEmailError,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "OTP Code",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    DFoodFTextField(
                        value = state.otpCode,
                        onValueChange = { if (it.length <= 6) viewModel.onEvent(ResetEmailEvent.OtpChanged(it)) },
                        label = "Enter 6-digit OTP",
                        isError = state.otpError != null,
                        errorMessage = state.otpError,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { viewModel.onEvent(ResetEmailEvent.VerifyOtpClicked) })
                    )

                    Spacer(modifier = Modifier.height(48.dp))

                    DFoodButton(
                        text = "VERIFY & UPDATE",
                        onClick = { viewModel.onEvent(ResetEmailEvent.VerifyOtpClicked) },
                        isLoading = state.isLoading,
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    TextButton(
                        onClick = { viewModel.onEvent(ResetEmailEvent.RequestOtpClicked) },
                        modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 8.dp)
                    ) {
                        Text("Didn't receive code? Resend", color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}
