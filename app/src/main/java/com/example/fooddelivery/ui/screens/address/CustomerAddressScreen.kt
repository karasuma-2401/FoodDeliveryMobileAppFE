package com.example.fooddelivery.ui.screens.address

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddLocationAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.fooddelivery.ui.components.button.DFoodButton
import com.example.fooddelivery.ui.components.card.AddressCard
import com.example.fooddelivery.ui.components.layout.ScaffoldBottomBarSurface
import com.example.fooddelivery.ui.components.topbar.DFoodTopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerAddressScreen(
    onNavigateBack: () -> Unit,
    onAddNewAddress: () -> Unit,
    onEditAddress: (Int) -> Unit = {}, // Chuyển sang Int
    viewModel: CustomerAddressViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var addressIdToDelete by remember { mutableStateOf<Int?>(null) } // Chuyển sang Int?
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.onEvent(CustomerAddressEvent.LoadAddresses)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    if (addressIdToDelete != null) {
        AlertDialog(
            onDismissRequest = { addressIdToDelete = null },
            title = { Text("Delete Address", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to delete this address?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        addressIdToDelete?.let { viewModel.onEvent(CustomerAddressEvent.DeleteAddress(it)) }
                        addressIdToDelete = null
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { addressIdToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            DFoodTopBar(
                title = "My Address",
                onBackClick = onNavigateBack,
                actions = {},
                scrollBehavior = null
            )
        },
        bottomBar = {
            ScaffoldBottomBarSurface {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    DFoodButton(
                        text = "ADD NEW ADDRESS",
                        onClick = onAddNewAddress,
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.AddLocationAlt,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        },
                    )
                }
            }
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            if (state.isLoading && state.addresses.isEmpty()) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (state.errorMessage != null) {
                Text(
                    text = state.errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(state.addresses) { address ->
                        AddressCard(
                            address = address,
                            onEdit = { onEditAddress(address.id) },
                            onDelete = { addressIdToDelete = address.id }
                        )
                    }
                }

                if (state.isLoading && state.addresses.isNotEmpty()) {
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth().align(Alignment.TopCenter),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}
