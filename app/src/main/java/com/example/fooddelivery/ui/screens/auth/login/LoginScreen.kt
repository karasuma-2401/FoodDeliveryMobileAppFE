package com.example.fooddelivery.ui.screens.auth.login

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.background
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.fooddelivery.R
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.fooddelivery.ui.components.textfield.DFoodFTextField
import com.example.fooddelivery.ui.components.button.DFoodButton
import com.example.fooddelivery.ui.components.topbar.DFoodTopBar
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.example.fooddelivery.ui.components.button.SocialButton
import com.example.fooddelivery.ui.utils.rememberFacebookLoginLauncher

@OptIn (ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onNavigateBack: () -> Unit,
    onNavigateToSignUp: () -> Unit,
    onNavigateToForgotPassword: () -> Unit,
    onNavigateHome: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val triggerFacebookLogin = rememberFacebookLoginLauncher(
        onSuccess = { token -> viewModel.loginWithFacebook(token) },
        onCancel = { viewModel.setErrorMessage("Cancelled login with facebook") },
        onError = { errorMsg -> viewModel.setErrorMessage("Facebook error: $errorMsg") }
    )

    val state by viewModel.state
    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess)
            onNavigateHome()
    }
    Scaffold(
        topBar = {
            DFoodTopBar(
                title = "",
                onBackClick = onNavigateBack,
                scrollBehavior = null
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
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

            Spacer(modifier = Modifier.height(40.dp))

            if (state.errorMessage != null) {
                Text(
                    text = state.errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier =  Modifier.padding(bottom = 16.dp)
                )
            }
            Text(
                text = "Phone number",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            DFoodFTextField(
                value = state.phone,
                onValueChange = viewModel::onPhoneChange,
                label = "",
                leadingIcon = {
                    Icon (
                        imageVector = Icons.Outlined.Phone,
                        contentDescription = null,
                    )
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                isError = state.phoneError?.isNotEmpty() == true,
                errorMessage = state.phoneError
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Password",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            DFoodFTextField(
                value = state.password,
                onValueChange = viewModel::onPasswordChange,
                label = "",
                isPassword = true,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Lock,
                        contentDescription = null,
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
                        onCheckedChange = viewModel::onRememberMeChange,
                        colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary)
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

            Spacer(modifier = Modifier.height(24.dp))

            DFoodButton(
                text = if (state.isSuccess) "LOGGING IN..." else "LOG IN",
                onClick = viewModel::login,
                enabled = !state.isLoading
            )

            Spacer(modifier = Modifier.height(32.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.surfaceVariant)

                Text(
                    text = "SOCIAL CONNECT",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.surfaceVariant)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                SocialButton(
                    iconRes = R.drawable.ic_facebook,
                    onClick = triggerFacebookLogin
                )
                Spacer(modifier = Modifier.width(16.dp))
                SocialButton(iconRes = R.drawable.ic_x_twitter)
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
                    modifier = Modifier.clickable { onNavigateToSignUp() }
                )
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}