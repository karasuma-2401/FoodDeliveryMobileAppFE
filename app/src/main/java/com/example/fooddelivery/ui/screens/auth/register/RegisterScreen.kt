package com.example.fooddelivery.ui.screens.auth.register

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.fooddelivery.R
import com.example.fooddelivery.ui.components.button.DFoodButton
import com.example.fooddelivery.ui.components.textfield.DFoodFTextField
import com.example.fooddelivery.ui.components.topbar.DFoodTopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onNavigateBack: () -> Unit,
    onNavigateToLogin:() -> Unit,
    viewModel: RegisterViewModel = hiltViewModel()
) {
    val state by viewModel.state
    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess)
            onNavigateToLogin()
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
                .padding(innerPadding)
                .padding(horizontal = 24.dp)
                .background(MaterialTheme.colorScheme.background)
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
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Delicious meals delivered at kinetic speed",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text("Full Name", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 4.dp))
            DFoodFTextField(
                value = state.fullName,
                onValueChange = viewModel::onFullNameChange,
                label = "",
                leadingIcon = { Icon(Icons.Outlined.Person, null)},
                isError = state.fullNameError?.isNotEmpty() == true,
                errorMessage = state.fullNameError
            )
            Spacer(modifier = Modifier.height(8.dp))

            Text("Email", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 4.dp))
            DFoodFTextField(
                value = state.email,
                onValueChange = viewModel::onEmailChange,
                label = "",
                leadingIcon = { Icon(Icons.Outlined.Email, null)},
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                isError = state.email?.isNotEmpty() == true,
                errorMessage = state.emailError
            )
            Spacer(modifier = Modifier.height(8.dp))

            Text("Phone Number", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 4.dp))
            DFoodFTextField(
                value = state.phone,
                onValueChange = viewModel::onPhoneChange,
                label = "",
                leadingIcon = { Icon(Icons.Outlined.Phone, null)},
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                isError = state.phoneError?.isNotEmpty() == true,
                errorMessage = state.phoneError
            )
            Spacer(modifier = Modifier.height(8.dp))

            Text("Password", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 4.dp))
            DFoodFTextField(
                value = state.password,
                onValueChange = viewModel::onPasswordChange,
                label = "",
                isPassword = true,
                leadingIcon = { Icon(Icons.Outlined.Lock, null)},
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),

                isError = state.passwordError?.isNotEmpty() == true,
                errorMessage = state.passwordError
            )
            Spacer(modifier = Modifier.height(8.dp))

            Text("Confirm Password", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 4.dp))
            DFoodFTextField(
                value = state.confirmPassword,
                onValueChange = viewModel::onConfirmPasswordChange,
                label = "",
                leadingIcon = { Icon(painterResource(id = R.drawable.ic_lock_reset), null)},
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                isPassword = true,
                isError = state.confirmPasswordError?.isNotEmpty() == true,
                errorMessage = state.passwordError
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                verticalAlignment = Alignment.Top,
                modifier = Modifier.fillMaxWidth()
            ) {
                Checkbox(
                    checked = state.agreeToTerms,
                    onCheckedChange = viewModel::onAgreeToTermsChange,
                    colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary)
                )
                Text(
                    text = buildAnnotatedString {
                        append("I agree to the ")
                        withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary)) {
                            append("Term of Service")
                        }
                        append(" and ")
                        withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary)) {
                            append("Privacy Policy")
                        }
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }
            Spacer(modifier = Modifier.height(32.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outlineVariant)
                Text(
                    text = " OR REGISTER WITH ",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outlineVariant)
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                RegisterSocialButton(iconRes = R.drawable.ic_facebook)
                Spacer(modifier = Modifier.width(16.dp))
                RegisterSocialButton(iconRes = R.drawable.ic_x_twitter)
            }
            Spacer(modifier = Modifier.height(48.dp))

            DFoodButton(
                text = if (state.isSuccess) "CREATING ACCOUNT..." else "SIGN UP",
                onClick = viewModel::register,
                enabled = !state.isLoading && state.agreeToTerms
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Text("Already a member? ", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    text = "Log In",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable { onNavigateToLogin() }
                )
            }
            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}

@Composable
fun RegisterSocialButton(iconRes: Int) {
    Box(
        modifier = Modifier
            .size(56.dp)
            .clip(RoundedCornerShape(16.dp))
            .border(color = MaterialTheme.colorScheme.outlineVariant, width = 1.dp, shape = RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .clickable { },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.onBackground
        )
    }
}

//@Preview(showBackground = true, showSystemUi = true)
//@Composable
//fun RegisterScreenPreview(modifier: Modifier = Modifier) {
//    DFoodTheme(darkTheme = false) {
//        RegisterScreen(
//            onNavigateBack = {},
//            onNavigateToLogin = {}
//        )
//    }
//}