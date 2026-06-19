package com.example.fooddelivery.ui.screens.auth.verification

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.fooddelivery.ui.components.button.DFoodButton
import com.example.fooddelivery.ui.components.topbar.DFoodTopBar

@Composable
fun VerificationScreen(
    email: String,
    isFromRegistration: Boolean,
    onNavigateBack: () -> Unit,
    onVerificationSuccess: (String, String) -> Unit, // email, otp
    viewModel: VerificationViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackBarHostState = remember { SnackbarHostState() }
    
    LaunchedEffect(Unit) {
        viewModel.onEvent(VerificationEvent.Init(email, isFromRegistration))
    }
    
    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess) {
            onVerificationSuccess(state.email, state.otpCode)
        }
    }
    
    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let { message ->
            if (message.isNotEmpty()) {
                snackBarHostState.showSnackbar(message)
                viewModel.onEvent(VerificationEvent.ErrorDismissed)
            }
        }
    }
    
    VerificationContent(
        state = state,
        onEvent = viewModel::onEvent,
        onNavigateBack = onNavigateBack,
        snackBarHostState = snackBarHostState
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerificationContent(
    state: VerificationState,
    onEvent: (VerificationEvent) -> Unit,
    onNavigateBack: () -> Unit,
    snackBarHostState: SnackbarHostState
) {
    Scaffold(
        snackbarHost = { SnackbarHost(snackBarHostState) },
        topBar = {
            DFoodTopBar(
                title = "Verification",
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
                text = "Enter Code",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = if (state.isFromRegistration) "Verify your account" else "Reset your password",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "We have sent a code to your email",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = state.email,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(40.dp))

            BasicTextField(
                value = state.otpCode,
                onValueChange = {
                    if (it.length <= 6) {
                        onEvent(VerificationEvent.OtpChanged(it))
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                decorationBox = {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        repeat(6) { index ->
                            val isFocused = state.otpCode.length == index
                            val char = when {
                                index >= state.otpCode.length -> ""
                                else -> state.otpCode[index].toString()
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(0.8f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                                    .border(
                                        width = if (isFocused) 2.dp else 1.dp,
                                        color = if (isFocused) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                                        shape = RoundedCornerShape(12.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = char,
                                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            )
            Spacer(modifier = Modifier.height(32.dp))

            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (state.timeLeft > 0) {
                    Text(
                        text = buildAnnotatedString {
                            append("Resend in  ")
                            withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)) {
                                val minutes = state.timeLeft / 60
                                val seconds = state.timeLeft % 60
                                val secondsFormatted = if (seconds < 10) "0$seconds" else "$seconds"
                                val timeString = "$minutes:$secondsFormatted"
                                append(timeString)
                            }
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Text(
                        text = "Resend Code",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable { onEvent(VerificationEvent.ResendCodeClicked) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            DFoodButton(
                text = if (state.isLoading) "VERIFYING..." else "VERIFY",
                onClick = { onEvent(VerificationEvent.VerifyClicked) },
                enabled = state.otpCode.length == 6 && !state.isLoading
            )
        }
    }
}