package com.example.fooddelivery.ui.screens.auth.register

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.fooddelivery.R
import com.example.fooddelivery.ui.components.button.DFoodButton
import com.example.fooddelivery.ui.components.button.SocialButton
import com.example.fooddelivery.ui.components.textfield.DFoodFTextField
import com.example.fooddelivery.ui.components.topbar.DFoodTopBar
import com.example.fooddelivery.ui.theme.DFoodTheme
import com.example.fooddelivery.ui.utils.rememberFacebookLoginLauncher
import com.example.fooddelivery.ui.utils.rememberGoogleLoginLauncher


@Composable
fun RegisterScreen(
    onNavigateBack: () -> Unit,
    onNavigateToLogin:() -> Unit,
    onNavigateHome: () -> Unit,
    onNavigateToVerification: (String) -> Unit,
    onNavigateToPolicy: (String) -> Unit,
    viewModel: RegisterViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackBarHostState = remember { SnackbarHostState() }

    val triggerFacebookLogin = rememberFacebookLoginLauncher(
        onSuccess = { token -> viewModel.onEvent(RegisterEvent.FacebookLoginClicked(token))},
        onCancel = { viewModel.onEvent(RegisterEvent.ErrorMessageSet("Facebook Login Cancelled")) },
        onError = { viewModel.onEvent(RegisterEvent.ErrorMessageSet("Facebook error: $it")) }
    )

    val triggerGoogleLogin = rememberGoogleLoginLauncher(
        onSuccess = { idToken -> viewModel.onEvent(RegisterEvent.GoogleLoginClicked(idToken)) },
        onError = { error -> viewModel.onEvent(RegisterEvent.ErrorMessageSet(error)) }
    )

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let { message ->
            if (message.isNotEmpty()) {
                snackBarHostState.showSnackbar(message)
                viewModel.onEvent(RegisterEvent.ErrorMessageSet(""))
            }
        }
    }

    LaunchedEffect(state.isSuccess, state.isSocialAuthSuccess) {
        if (state.isSuccess) {
            onNavigateToVerification(state.email)
        } else if (state.isSocialAuthSuccess) {
            onNavigateHome()
        }
    }
    
    RegisterContent(
        state = state,
        onEvent =  viewModel::onEvent,
        triggerFacebookLogin = triggerFacebookLogin,
        triggerGoogleLogin = triggerGoogleLogin,
        onNavigateBack = onNavigateBack,
        onNavigateToLogin = onNavigateToLogin,
        onNavigateToPolicy = onNavigateToPolicy,
        snackBarHostState = snackBarHostState
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterContent(
    state: RegisterState,
    onEvent: (RegisterEvent) -> Unit,
    triggerFacebookLogin: () -> Unit,
    triggerGoogleLogin: () -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToPolicy: (String) -> Unit,
    snackBarHostState: SnackbarHostState
) {
    Scaffold(
        snackbarHost = { SnackbarHost(snackBarHostState) },
        topBar = {
            DFoodTopBar(
                title = "",
                onBackClick = onNavigateBack,
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
            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = buildAnnotatedString {
                    append("Join the ")
                    withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary)) {
                        append("D")
                    }
                    append("Food")
                },
                style = MaterialTheme.typography.headlineLarge.copy(fontSize = 32.sp),
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Delicious meals delivered at kinetic speed",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(32.dp))

            DFoodFTextField(
                value = state.fullName,
                onValueChange = { onEvent(RegisterEvent.FullNameChanged(it))},
                label = "Enter your full name",
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                isError = state.fullNameError?.isNotEmpty() == true,
                errorMessage = state.fullNameError
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            DFoodFTextField(
                value = state.email,
                onValueChange = { onEvent(RegisterEvent.EmailChanged(it))},
                label = "Enter your email",
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Email,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                isError = state.emailError?.isNotEmpty() == true,
                errorMessage = state.emailError
            )
            Spacer(modifier = Modifier.height(8.dp))

            DFoodFTextField(
                value = state.phone,
                onValueChange = { onEvent(RegisterEvent.PhoneChanged(it))},
                label = "Enter your phone",
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Phone,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                isError = state.phoneError?.isNotEmpty() == true,
                errorMessage = state.phoneError
            )
            Spacer(modifier = Modifier.height(8.dp))


            DFoodFTextField(
                value = state.password,
                onValueChange = { onEvent(RegisterEvent.PasswordChanged(it))},
                label = "Enter your password",
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
            Spacer(modifier = Modifier.height(8.dp))

            DFoodFTextField(
                value = state.confirmPassword,
                onValueChange = { onEvent(RegisterEvent.ConfirmPasswordChanged(it))},
                label = "Confirm your password",
                leadingIcon = {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_lock_reset),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                isPassword = true,
                isError = state.confirmPasswordError?.isNotEmpty() == true,
                errorMessage = state.confirmPasswordError
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                verticalAlignment = Alignment.Top,
                modifier = Modifier.fillMaxWidth()
            ) {
                Checkbox(
                    checked = state.agreeToTerms,
                    onCheckedChange = { onEvent(RegisterEvent.AgreeToTermsChanged(it))},
                    colors = CheckboxDefaults.colors(
                        checkedColor = MaterialTheme.colorScheme.primary,
                        uncheckedColor = MaterialTheme.colorScheme.outline
                    )
                )

                val annotatedString = buildAnnotatedString {
                    append("I agree to the ")

                    pushStringAnnotation(tag = "terms", annotation = "terms")
                    withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)) {
                        append("Terms of Service")
                    }
                    pop()

                    append(" and ")

                    pushStringAnnotation(tag = "privacy", annotation = "privacy")
                    withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)) {
                        append("Privacy Policy")
                    }
                    pop()
                }

                ClickableText(
                    text = annotatedString,
                    style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                    modifier = Modifier.padding(top = 12.dp),
                    onClick = { offset ->
                        annotatedString.getStringAnnotations(tag = "terms", start = offset, end = offset)
                            .firstOrNull()?.let {
                                onNavigateToPolicy("terms")
                            }
                        annotatedString.getStringAnnotations(tag = "privacy", start = offset, end = offset)
                            .firstOrNull()?.let {
                                onNavigateToPolicy("privacy")
                            }
                    }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            DFoodButton(
                text = if (state.isLoading) "CREATING ACCOUNT..." else "SIGN UP",
                onClick = { onEvent(RegisterEvent.RegisterClicked)},
                enabled = !state.isLoading && state.agreeToTerms
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
                    contentDescription = "Sign up with facebook",
                    onClick = triggerFacebookLogin,
                    modifier = Modifier.size(56.dp)
                )
                Spacer(modifier = Modifier.width(24.dp))
                SocialButton(
                    iconRes = R.drawable.ic_google,
                    contentDescription = "Sign up with google",
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
                    text = "Already a member? ",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Log In",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { onNavigateToLogin() }
                )
            }
            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}
@Preview (showBackground = true, showSystemUi = true)
@Composable
fun RegisterScreenPreview() {
    DFoodTheme(darkTheme = false) {
        RegisterContent(
            state = RegisterState(),
            onEvent = {},
            triggerFacebookLogin = {},
            triggerGoogleLogin = {},
            onNavigateBack = {},
            onNavigateToLogin = {},
            onNavigateToPolicy = {},
            snackBarHostState = remember { SnackbarHostState() }
        )
    }
}