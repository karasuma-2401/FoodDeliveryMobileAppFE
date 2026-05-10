package com.example.fooddelivery.ui.screens.profile.address

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.WorkOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.fooddelivery.ui.components.button.DFoodButton
import com.example.fooddelivery.ui.components.header.LocationPickerHeader
import com.example.fooddelivery.ui.screens.profile.address.components.CustomAddressTextField
import com.example.fooddelivery.ui.components.topbar.DFoodTopBar
import com.example.fooddelivery.ui.screens.profile.address.components.AddressTypeItem

@Composable
fun AddAddressScreen(
    onNavigateBack: () -> Unit,
    onAddressSaved: () -> Unit,
    viewModel: AddAddressViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackBarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess) {
            onAddressSaved()
            viewModel.onEvent(AddAddressEvent.ResetState)
        }
    }

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let {
            snackBarHostState.showSnackbar(it)
            viewModel.onEvent(AddAddressEvent.ErrorDismissed)
        }
    }

    AddAddressContent(
        state = state,
        onEvent = viewModel::onEvent,
        onNavigateBack = onNavigateBack,
        snackBarHostState = snackBarHostState
    )
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAddressContent(
        state: AddAddressState,
        onEvent: (AddAddressEvent) -> Unit,
        onNavigateBack: () -> Unit,
        snackBarHostState: SnackbarHostState
) {
    var showSearchDialog by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    Scaffold(
        snackbarHost = { SnackbarHost(snackBarHostState) },
        topBar = {
            DFoodTopBar(
                title = "Location Settings",
                onBackClick = onNavigateBack
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            LocationPickerHeader(onSearchClick = { showSearchDialog = true })

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = (-24).dp),
                shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                color = MaterialTheme.colorScheme.background,
                tonalElevation = 2.dp
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .padding(bottom = 24.dp)
                ) {
                    Text(
                        text = "Location Details",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Confirm your delivery address to proceed",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "SAVE ADDRESS AS",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        AddressTypeItem(
                            label = "Home",
                            icon = Icons.Outlined.Home,
                            isSelected = state.type == "Home",
                            selectedColor = Color(0xFF4285F4),
                            onClick = { onEvent(AddAddressEvent.TypeChanged("Home")) },
                            modifier = Modifier.weight(1f)
                        )
                        AddressTypeItem(
                            label = "Work",
                            icon = Icons.Outlined.WorkOutline,
                            isSelected = state.type == "Work",
                            selectedColor = Color(0xFF9C27B0),
                            onClick = { onEvent(AddAddressEvent.TypeChanged("Work")) },
                            modifier = Modifier.weight(1f)
                        )
                        AddressTypeItem(
                            label = "Other",
                            icon = Icons.Outlined.MoreHoriz,
                            isSelected = state.type == "Other",
                            selectedColor = MaterialTheme.colorScheme.primary,
                            onClick = { onEvent(AddAddressEvent.TypeChanged("Other")) },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    AnimatedVisibility(visible = state.type == "Other") {
                        Column {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "ADDRESS TITLE",
                                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            CustomAddressTextField(
                                value = state.title,
                                onValueChange = { onEvent(AddAddressEvent.TitleChanged(it)) },
                                placeholder = "e.g. Gym, My Friend's House",
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "CITY",
                        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    CustomAddressTextField(
                        value = state.city,
                        onValueChange = { onEvent(AddAddressEvent.CityChanged(it))},
                        leadingIcon = Icons.Default.LocationOn,
                        placeholder = "City, State, Country",
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "STREET NAME",
                        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    CustomAddressTextField(
                        value = state.streetName,
                        onValueChange = { onEvent(AddAddressEvent.StreetNameChanged(it))},
                        leadingIcon = Icons.Default.Apartment,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { 
                            focusManager.clearFocus()
                            onEvent(AddAddressEvent.SaveAddressClicked)
                        })
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onEvent(AddAddressEvent.DefaultChanged(!state.isDefault)) }
                    ) {
                        Checkbox(
                            checked = state.isDefault,
                            onCheckedChange = { onEvent(AddAddressEvent.DefaultChanged(it)) }
                        )
                        Text(
                            text = "Set as default address",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    DFoodButton(
                        text = if (state.isLoading) "SAVING..." else "SAVE LOCATION",
                        onClick = {
                            focusManager.clearFocus()
                            onEvent(AddAddressEvent.SaveAddressClicked)
                        },
                        enabled = !state.isLoading,
                        leadingIcon = {
                            if (!state.isLoading) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            } else {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            }
                        }
                    )
                }
            }
        }
    }
}
