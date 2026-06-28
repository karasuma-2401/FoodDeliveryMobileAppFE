package com.example.fooddelivery.ui.screens.address

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EditLocation
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.fooddelivery.ui.components.button.DFoodButton
import com.example.fooddelivery.ui.components.header.LocationPickerHeader
import com.example.fooddelivery.ui.components.layout.ScaffoldBottomBarSurface
import com.example.fooddelivery.ui.components.textfield.DFoodFTextField
import com.example.fooddelivery.ui.components.topbar.DFoodTopBar
import com.example.fooddelivery.ui.screens.address.components.AddressDeliveryNoteField
import com.example.fooddelivery.ui.screens.address.components.AddressFormDivider
import com.example.fooddelivery.ui.screens.address.components.AddressSearchDialog
import com.example.fooddelivery.ui.screens.address.components.AddressTypeSelector
import com.example.fooddelivery.ui.screens.address.components.DeliveryLocationRow
import com.example.fooddelivery.ui.theme.CustomerDimens
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
                onBackClick = onNavigateBack,
                scrollBehavior = null,
            )
        },
        bottomBar = {
            ScaffoldBottomBarSurface(shadowElevation = 16.dp, tonalElevation = 4.dp) {
                DFoodButton(
                    text = if (isEditMode) "UPDATE ADDRESS" else "SAVE LOCATION",
                    onClick = {
                        focusManager.clearFocus()
                        onEvent(AddAddressEvent.SaveAddressClicked)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    enabled = !state.isLoading && state.hasPinnedLocation,
                    isLoading = state.isLoading,
                    leadingIcon = {
                        Icon(
                            imageVector = if (isEditMode) Icons.Default.EditLocation else Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp),
                        )
                    },
                )
            }
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = innerPadding.calculateTopPadding())
                .verticalScroll(rememberScrollState()),
        ) {
            LocationPickerHeader(
                initialLocation = mapLocation,
                onSearchClick = { showSearchDialog = true },
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = (-20).dp)
                    .padding(horizontal = CustomerDimens.screenHorizontalPadding)
                    .padding(bottom = 24.dp),
                shape = RoundedCornerShape(CustomerDimens.cardCornerRadius),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(
                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
                    ),
                ),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(CustomerDimens.cardPaddingLg),
                ) {
                    AddressTypeSelector(
                        selectedType = state.type,
                        onTypeSelected = { onEvent(AddAddressEvent.TypeChanged(it)) },
                    )

                    AnimatedVisibility(visible = state.type == "Other") {
                        Column {
                            Spacer(modifier = Modifier.height(16.dp))
                            DFoodFTextField(
                                value = state.title,
                                onValueChange = { onEvent(AddAddressEvent.TitleChanged(it)) },
                                label = "Address title",
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                                keyboardActions = KeyboardActions(
                                    onNext = { focusManager.moveFocus(androidx.compose.ui.focus.FocusDirection.Down) },
                                ),
                            )
                        }
                    }

                    AddressFormDivider()

                    DeliveryLocationRow(
                        address = state.fullAddress,
                        hasPinnedLocation = state.hasPinnedLocation,
                        onChangeClick = { showSearchDialog = true },
                    )

                    AddressFormDivider()

                    AddressDeliveryNoteField(
                        value = state.deliveryNote,
                        onValueChange = { onEvent(AddAddressEvent.DeliveryNoteChanged(it)) },
                    )
                }
            }

            Spacer(modifier = Modifier.height(innerPadding.calculateBottomPadding() + 16.dp))
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
