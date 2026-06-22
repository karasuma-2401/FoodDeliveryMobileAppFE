package com.example.fooddelivery.ui.screens.restaurant.profile

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.fooddelivery.R
import com.example.fooddelivery.ui.components.sectionheader.SectionHeader
import com.example.fooddelivery.ui.components.topbar.DFoodTopBar
import com.example.fooddelivery.ui.screens.restaurant.component.RestaurantInputField
import com.example.fooddelivery.ui.theme.DFoodTheme

@Composable
fun RestaurantPersonalInfoScreen(
    onNavigateBack: () -> Unit,
    viewModel: RestaurantPersonalInfoViewModel = hiltViewModel()
) {
    val state = viewModel.uiState
    val context = LocalContext.current

    LaunchedEffect(key1 = state.isSuccess) {
        if (state.isSuccess) {
            Toast.makeText(context, "Information updated successfully!", Toast.LENGTH_SHORT).show()
            viewModel.resetSuccessState()
            onNavigateBack()
        }
    }

    RestaurantPersonalInfoContent(
        state = state,
        onEvent = { event -> viewModel.onEvent(event) },
        onNavigateBack = onNavigateBack
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RestaurantPersonalInfoContent(
    state: RestaurantPersonalInfoState,
    onEvent: (RestaurantPersonalInfoEvent) -> Unit,
    onNavigateBack: () -> Unit
) {
    Scaffold(
        topBar = {
            DFoodTopBar(title = "Restaurant Information", onBackClick = onNavigateBack)
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
                // Upload Image Section
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
                        TextButton(onClick = { /* Open Gallery Picker */ }) {
                            Text("Change Restaurant Photo")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // GENERAL INFORMATION
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

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                Spacer(modifier = Modifier.height(16.dp))

                // BUSINESS ADDRESS
                SectionHeader("BUSINESS ADDRESS")
                RestaurantInputField(
                    label = "Street Address",
                    placeholder = "e.g. 123 Main Street",
                    value = state.street,
                    onValueChange = { onEvent(RestaurantPersonalInfoEvent.StreetChanged(it)) }
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    RestaurantInputField(
                        label = "District",
                        placeholder = "District 1",
                        value = state.district,
                        onValueChange = { onEvent(RestaurantPersonalInfoEvent.DistrictChanged(it)) },
                        modifier = Modifier.weight(1f)
                    )
                    RestaurantInputField(
                        label = "City",
                        placeholder = "HCMC",
                        value = state.city,
                        onValueChange = { onEvent(RestaurantPersonalInfoEvent.CityChanged(it)) },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // SAVE BUTTON
                Button(
                    onClick = { onEvent(RestaurantPersonalInfoEvent.Submit) },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !state.isLoading
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Text("Save Changes", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    }
                }

                if (state.error != null) {
                    Text(
                        text = state.error,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 12.dp).align(Alignment.CenterHorizontally)
                    )
                }

                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun RestaurantPersonalInfoPreview() {
    DFoodTheme {
        RestaurantPersonalInfoContent(
            state = RestaurantPersonalInfoState(
                isLoading = false,
                name = "King Burger - District 1",
                phone = "0901234567",
                street = "123 Le Loi Street",
                district = "District 1",
                city = "Ho Chi Minh City"
            ),
            onEvent = {},
            onNavigateBack = {}
        )
    }
}