package com.example.fooddelivery.ui.screens.address

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.fooddelivery.ui.components.button.DFoodButton
import com.example.fooddelivery.ui.components.header.LocationPickerHeader
import com.example.fooddelivery.ui.screens.address.components.AddressSearchDialog
import com.example.fooddelivery.ui.screens.address.components.CustomAddressTextField
import com.example.fooddelivery.ui.components.topbar.DFoodTopBar
import com.example.fooddelivery.ui.screens.address.components.AddressTypeItem
import com.example.fooddelivery.ui.theme.DFoodTheme
import org.osmdroid.util.GeoPoint

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
    val isEditMode = state.isEditMode
    val mapLocation = remember(state.selectedAddress) {
        val selected = state.selectedAddress
        if (selected != null && selected.latitude != 0.0 && selected.longitude != 0.0) {
            GeoPoint(selected.latitude, selected.longitude)
        } else {
            GeoPoint(10.762622, 106.660172)
        }
    }

    AddressSearchDialog(
        showDialog = showSearchDialog,
        onDismissRequest = { showSearchDialog = false },
        searchQuery = state.searchQuery,
        onSearchQueryChange = { onEvent(AddAddressEvent.SearchQueryChanged(it)) },
        isSearching = state.isSearching,
        searchResults = state.searchResults,
        noResultsFound = state.noResultsFound,
        onSearchResultSelected = {
            onEvent(AddAddressEvent.SearchResultSelected(it))
            showSearchDialog = false
        }
    )

    Scaffold(
        snackbarHost = { SnackbarHost(snackBarHostState) },
        topBar = {
            DFoodTopBar(
                title = if (isEditMode) "Edit Address" else "Add New Address",
                onBackClick = onNavigateBack
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            LocationPickerHeader(
                initialLocation = mapLocation,
                onSearchClick = { showSearchDialog = true }
            )

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = (-24).dp)
                    .padding(bottom = 20.dp),
                shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                color = MaterialTheme.colorScheme.background,
                tonalElevation = 2.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 24.dp)
                ) {
                    Text(
                        text = if (isEditMode) "Update Location" else "Location Details",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (isEditMode) {
                            "Change location via search, then update delivery notes below"
                        } else {
                            "Search to pin your location, then add delivery notes if needed"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "SAVE ADDRESS AS",
                                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
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
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.22f)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "DELIVERY LOCATION",
                                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                TextButton(onClick = { showSearchDialog = true }) {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Search")
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            CustomAddressTextField(
                                value = state.fullAddress,
                                onValueChange = {},
                                readOnly = true,
                                leadingIcon = Icons.Default.LocationOn,
                                placeholder = "Search to select delivery location",
                            )
                            if (!state.hasPinnedLocation) {
                                Text(
                                    text = "A confirmed map location is required before saving",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.padding(top = 6.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.22f)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "DELIVERY NOTE",
                                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Floor, apartment, gate, or call-before-arrival instructions",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            CustomAddressTextField(
                                value = state.deliveryNote,
                                onValueChange = { onEvent(AddAddressEvent.DeliveryNoteChanged(it)) },
                                placeholder = "e.g. Floor 5, Unit B, call before arrival",
                                singleLine = false,
                                minLines = 2,
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    DFoodButton(
                        text = if (state.isLoading) "SAVING..." else if (isEditMode) "UPDATE ADDRESS" else "SAVE LOCATION",
                        onClick = {
                            focusManager.clearFocus()
                            onEvent(AddAddressEvent.SaveAddressClicked)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !state.isLoading && state.hasPinnedLocation,
                        leadingIcon = {
                            if (!state.isLoading) {
                                Icon(
                                    if (isEditMode) Icons.Default.EditLocation else Icons.Default.CheckCircle,
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
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun AddAddressScreenPreview() {
    DFoodTheme(darkTheme = false) {
        AddAddressContent(
            state = AddAddressState(),
            onEvent = {},
            onNavigateBack = {},
            snackBarHostState = SnackbarHostState()
        )
    }
}
