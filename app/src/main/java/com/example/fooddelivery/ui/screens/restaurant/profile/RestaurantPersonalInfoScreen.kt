package com.example.fooddelivery.ui.screens.restaurant.profile

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.fooddelivery.R
import com.example.fooddelivery.ui.components.sectionheader.SectionHeader
import com.example.fooddelivery.ui.components.topbar.DFoodTopBar
import com.example.fooddelivery.ui.screens.restaurant.component.RestaurantInputField

@Composable
fun RestaurantPersonalInfoScreen(
    navController: NavController, // 🌟 Nhận NavController để lắng nghe tín hiệu
    onNavigateBack: () -> Unit,
    onNavigateToSelectAddress: () -> Unit,
    onRegistrationComplete: () -> Unit,
    viewModel: RestaurantPersonalInfoViewModel = hiltViewModel()
) {
    val state = viewModel.uiState
    val context = LocalContext.current

    val backStackEntry = navController.currentBackStackEntry
    val hasSavedAddress by backStackEntry?.savedStateHandle
        ?.getStateFlow("address_saved_signal", false)
        ?.collectAsState() ?: remember { mutableStateOf(false) }
    LaunchedEffect(hasSavedAddress) {
        if (hasSavedAddress == true) {
            viewModel.fetchLatestAddress()
            navController.currentBackStackEntry?.savedStateHandle?.remove<Boolean>("address_saved_signal")
        }
    }

    LaunchedEffect(key1 = state.isSuccess) {
        if (state.isSuccess) {
            if (state.isFromSignUp) {
                Toast.makeText(context, "Restaurant created successfully!", Toast.LENGTH_SHORT).show()
                viewModel.resetSuccessState()
                onRegistrationComplete()
            } else {
                Toast.makeText(context, "Information updated successfully!", Toast.LENGTH_SHORT).show()
                viewModel.resetSuccessState()
                onNavigateBack()
            }
        }
    }

    RestaurantPersonalInfoContent(
        state = state,
        onEvent = viewModel::onEvent,
        onNavigateBack = onNavigateBack,
        onSelectAddressClick = onNavigateToSelectAddress
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RestaurantPersonalInfoContent(
    state: RestaurantPersonalInfoState,
    onEvent: (RestaurantPersonalInfoEvent) -> Unit,
    onNavigateBack: () -> Unit,
    onSelectAddressClick: () -> Unit
) {
    Scaffold(
        topBar = {
            val screenTitle = if (state.isFromSignUp) "Setup Restaurant" else "Restaurant Information"
            DFoodTopBar(title = screenTitle, onBackClick = onNavigateBack)
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        if (state.isLoading && state.name.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.shapes.medium),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_add),
                            contentDescription = "Upload Photo",
                            tint = MaterialTheme.colorScheme.primary
                        )
                        TextButton(onClick = { /* Open Gallery */ }) {
                            Text("Change Restaurant Photo")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                SectionHeader("GENERAL INFORMATION")

                RestaurantInputField(
                    label = "Restaurant Name",
                    placeholder = "e.g. King Burger",
                    value = state.name,
                    onValueChange = { onEvent(RestaurantPersonalInfoEvent.NameChanged(it)) }
                )

                RestaurantInputField(
                    label = "Hotline Phone Number",
                    placeholder = "090xxxxxxx",
                    value = state.phone,
                    onValueChange = { onEvent(RestaurantPersonalInfoEvent.PhoneChanged(it)) }
                )

                RestaurantInputField(
                    label = "Restaurant Description",
                    placeholder = "Describe your restaurant specialties...",
                    value = state.description,
                    onValueChange = { onEvent(RestaurantPersonalInfoEvent.DescriptionChanged(it)) }
                )

                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Business Location",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelectAddressClick() },
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = if (state.addressId != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = state.selectedAddressText.ifEmpty { "Tap to select restaurant address on map" },
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (state.addressId != null) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.outline
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = { onEvent(RestaurantPersonalInfoEvent.Submit) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !state.isLoading
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        val buttonText = if (state.isFromSignUp) "Complete Registration" else "Save Changes"
                        Text(text = buttonText, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    }
                }

                if (state.error != null) {
                    Text(
                        text = state.error,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier
                            .padding(top = 12.dp)
                            .align(Alignment.CenterHorizontally)
                    )
                }
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}