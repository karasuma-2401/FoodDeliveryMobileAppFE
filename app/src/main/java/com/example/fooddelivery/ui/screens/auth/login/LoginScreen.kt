package com.example.fooddelivery.ui.screens.auth.login

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.fooddelivery.R
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.fooddelivery.ui.components.textfield.DFoodFTextField
import com.example.fooddelivery.ui.components.button.DFoodButton
import com.example.fooddelivery.ui.components.topbar.DFoodTopBar
import com.example.fooddelivery.ui.components.button.SocialButton
import com.example.fooddelivery.ui.theme.DFoodTheme
import com.example.fooddelivery.ui.utils.rememberFacebookLoginLauncher
import com.example.fooddelivery.ui.utils.rememberGoogleLoginLauncher


@Composable
fun LoginScreen(
    onNavigateBack: () -> Unit,
    onNavigateToSignUp: () -> Unit,
    onNavigateToForgotPassword: () -> Unit,
    onNavigateAfterLogin: (Any) -> Unit,
    showRegistrationSuccess: Boolean = false,
    onRegistrationSuccessShown: () -> Unit = {},
    viewModel: LoginViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackBarHostState = remember { SnackbarHostState() }

    val triggerFacebookLogin = rememberFacebookLoginLauncher(
        onSuccess = { token -> viewModel.onEvent(LoginEvent.FacebookLoginClicked(token)) },
        onCancel = { viewModel.onEvent(LoginEvent.ErrorMessageSet("Facebook Login Cancelled")) },
        onError = { viewModel.onEvent(LoginEvent.ErrorMessageSet("Facebook error: $it")) }
    )

    val triggerGoogleLogin = rememberGoogleLoginLauncher(
        onSuccess = { idToken -> viewModel.onEvent(LoginEvent.GoogleLoginClicked(idToken)) },
        onError = { error -> viewModel.onEvent(LoginEvent.ErrorMessageSet(error)) }
    )

    LaunchedEffect(showRegistrationSuccess) {
        if (showRegistrationSuccess) {
            snackBarHostState.showSnackbar(
                message = "Registration successful! Please log in.",
                duration = SnackbarDuration.Short
            )
            onRegistrationSuccessShown()
        }
    }

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let {
            if (it.isNotEmpty()) {
                snackBarHostState.showSnackbar(it)
                viewModel.onEvent(LoginEvent.ErrorMessageSet(""))
            }
        }
    }
    LaunchedEffect(state.isSuccess, state.postLoginDestination) {
        if (state.isSuccess) {
            state.postLoginDestination?.let(onNavigateAfterLogin)
        }
    }
    LoginScreenContent(
        state = state,
        onEvent = viewModel::onEvent,
        onNavigateBack = onNavigateBack,
        onNavigateToSignUp = onNavigateToSignUp,
        onNavigateToForgotPassword = onNavigateToForgotPassword,
        triggerFacebookLogin = triggerFacebookLogin,
        triggerGoogleLogin = triggerGoogleLogin,
        snackBarHostState = snackBarHostState,
    )
}

@OptIn (ExperimentalMaterial3Api::class)
@Composable
fun LoginScreenContent(
    state: LoginState,
    onEvent: (LoginEvent) -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToSignUp: () -> Unit,
    onNavigateToForgotPassword: () -> Unit,
    triggerFacebookLogin: () -> Unit,
    triggerGoogleLogin: () -> Unit,
    snackBarHostState: SnackbarHostState
) {
    Scaffold(
        snackbarHost = { SnackbarHost(snackBarHostState) },
        topBar = {
            DFoodTopBar(
                title = "",
                onBackClick = onNavigateBack,
                scrollBehavior = null
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(innerPadding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Log In",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.width(8.dp))

                Icon (
                    painter = painterResource(id = R.drawable.ic_fork_knife),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text (
                text = "Deliciousness is just a tap away",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            DFoodFTextField(
                value = state.phone,
                onValueChange = { onEvent(LoginEvent.PhoneChanged(it))},
                label = "Phone",
                leadingIcon = {
                    Icon (
                        imageVector = Icons.Outlined.Phone,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                isError = state.phoneError?.isNotEmpty() == true,
                errorMessage = state.phoneError
            )

            Spacer(modifier = Modifier.height(12.dp))

            DFoodFTextField(
                value = state.password,
                onValueChange = { onEvent(LoginEvent.PasswordChanged(it))},
                label = "Password",
                isPassword = true,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Lock,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                isError = state.passwordError?.isNotEmpty() == true,
                errorMessage = state.passwordError
            )

            Spacer (modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = state.rememberMe,
                        onCheckedChange = { onEvent(LoginEvent.RememberMeChanged(it))},
                        colors = CheckboxDefaults.colors(
                            checkedColor = MaterialTheme.colorScheme.primary,
                            uncheckedColor = MaterialTheme.colorScheme.outline
                        )
                    )
                    Text (
                        text = "Remember Me",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
                Text (
                    text = "Forgot Password?",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable { onNavigateToForgotPassword() }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            DFoodButton(
                text = if (state.isLoading) "LOGGING IN..." else "LOG IN",
                onClick = { onEvent(LoginEvent.LoginClicked)},
                enabled = !state.isLoading
            )

            Spacer(modifier = Modifier.height(32.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HorizontalDivider(
                    modifier = Modifier.weight(1f), 
                    color = MaterialTheme.colorScheme.outlineVariant
                )

                Text(
                    text = " SOCIAL CONNECT ",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
                HorizontalDivider(
                    modifier = Modifier.weight(1f), 
                    color = MaterialTheme.colorScheme.outlineVariant
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SocialButton(
                    iconRes = R.drawable.ic_facebook,
                    contentDescription = "Log in with facebook",
                    onClick = triggerFacebookLogin,
                    modifier = Modifier.size(56.dp)
                )
                Spacer(modifier = Modifier.width(24.dp))
                SocialButton(
                    iconRes = R.drawable.ic_google,
                    contentDescription = "Log in with google",
                    onClick = triggerGoogleLogin,
                    modifier = Modifier.size(56.dp)
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Don't have an account?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "SIGN UP",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { onNavigateToSignUp() }
                )
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun LoginScreenPreview() {
    DFoodTheme(darkTheme = false
    ) {
        LoginScreenContent(
            state = LoginState(),
            onEvent = {},
            onNavigateBack = {},
            onNavigateToSignUp = {},
            onNavigateToForgotPassword = {},
            triggerFacebookLogin = {},
            triggerGoogleLogin = {},
            snackBarHostState = remember { SnackbarHostState() }
        )
    }
}
