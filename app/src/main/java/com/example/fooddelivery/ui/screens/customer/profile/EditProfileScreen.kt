package com.example.fooddelivery.ui.screens.customer.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.fooddelivery.R
import com.example.fooddelivery.ui.components.button.DFoodButton
import com.example.fooddelivery.ui.components.layout.ScaffoldBottomBarSurface
import com.example.fooddelivery.ui.components.textfield.DFoodFTextField
import com.example.fooddelivery.ui.components.topbar.DFoodTopBar

@Composable
fun EditProfileScreen(
    onNavigateBack: () -> Unit,
    viewModel: EditProfileViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackBarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess) {
            onNavigateBack()
            viewModel.onEvent(EditProfileEvent.ResetSuccessState)
        }
    }

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let {
            if (it.isNotEmpty()) {
                snackBarHostState.showSnackbar(it)
                viewModel.onEvent(EditProfileEvent.ErrorDismissed)
            }
        }
    }

    EditProfileContent(
        state = state,
        onEvent = viewModel::onEvent,
        onNavigateBack = onNavigateBack,
        snackBarHostState = snackBarHostState
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileContent(
    state: EditProfileState,
    onEvent: (EditProfileEvent) -> Unit,
    onNavigateBack: () -> Unit,
    snackBarHostState: SnackbarHostState
) {
    val focusManager = LocalFocusManager.current
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            onEvent(EditProfileEvent.ProfileImageChanged(it.toString()))
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackBarHostState) },
        topBar = {
            DFoodTopBar(
                title = "Edit Profile",
                onBackClick = onNavigateBack,
                scrollBehavior = null,
            )
        },
        bottomBar = {
            ScaffoldBottomBarSurface(shadowElevation = 16.dp, tonalElevation = 4.dp) {
                DFoodButton(
                    text = "SAVE",
                    onClick = {
                        focusManager.clearFocus()
                        onEvent(EditProfileEvent.SaveClicked)
                    },
                    isLoading = state.isLoading,
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
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            Box(contentAlignment = Alignment.BottomEnd) {
                Box(
                    modifier = Modifier
                        .size(130.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface)
                        .border(4.dp, MaterialTheme.colorScheme.primaryContainer, CircleShape)
                ) {
                    if (!state.user.profileImage.isNullOrEmpty() || state.selectedImageUri != null) {
                        AsyncImage(
                            model = state.selectedImageUri ?: state.user.profileImage,
                            contentDescription = "Profile Picture",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize(),
                            placeholder = painterResource(id = R.drawable.food_bowl),
                            error = painterResource(R.drawable.food_bowl)
                        )
                    } else {
                        Image(
                            painter = painterResource(id = R.drawable.food_bowl),
                            contentDescription = "Default Profile Picture",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                Surface(
                    modifier = Modifier
                        .size(36.dp)
                        .offset(x = (-4).dp, y = (-4).dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary,
                    shadowElevation = 6.dp,
                    tonalElevation = 2.dp
                ) {
                    IconButton(onClick = { imagePickerLauncher.launch("image/*") }) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Image",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = state.user.fullName.ifEmpty { "User Name" },
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Ho Chi Minh, VietNam",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(32.dp))

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 2.dp
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    ProfileInputField(
                        label = "Full Name",
                        value = state.user.fullName,
                        onValueChange = { onEvent(EditProfileEvent.FullNameChanged(it)) },
                        icon = Icons.Default.AccountCircle,
                        isError = state.fullNameError != null,
                        errorMessage = state.fullNameError,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    ProfileInputField(
                        label = "Email",
                        value = state.user.email,
                        onValueChange = { onEvent(EditProfileEvent.EmailChanged(it)) },
                        icon = Icons.Default.Email,
                        isError = state.emailError != null,
                        errorMessage = state.emailError,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        )
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    ProfileInputField(
                        label = "Phone Number",
                        value = state.user.phone,
                        onValueChange = { onEvent(EditProfileEvent.PhoneChanged(it)) },
                        icon = Icons.Default.Phone,
                        isError = state.phoneError != null,
                        errorMessage = state.phoneError,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Phone,
                            imeAction = ImeAction.Next
                        )
                    )
                }
            }
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

@Composable
fun ProfileInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    icon: ImageVector,
    isError: Boolean = false,
    errorMessage: String? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default
) {
    DFoodFTextField(
        value = value,
        onValueChange = onValueChange,
        label = label,
        leadingIcon = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        isError = isError,
        errorMessage = errorMessage,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        modifier = Modifier.fillMaxWidth()
    )
}
