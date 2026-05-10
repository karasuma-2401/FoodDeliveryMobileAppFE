package com.example.fooddelivery.ui.screens.admin
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.* // Quan trọng: chứa getValue/setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.fooddelivery.ui.components.sectionheader.SectionHeader
import com.example.fooddelivery.R
import com.example.fooddelivery.ui.theme.DFoodTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RestaurantFormScreen(
    restaurantId: Int? = null,
    viewModel: RestaurantFormViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val state = viewModel.uiState

    LaunchedEffect(key1 = restaurantId) {
        if (restaurantId != null) {
            viewModel.loadRestaurantData(restaurantId)
        }
    }

    // Xử lý khi lưu thành công
    LaunchedEffect(key1 = state.isSuccess) {
        if (state.isSuccess) onNavigateBack()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(if (restaurantId == null) "Add Restaurant" else "Edit Restaurant") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
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
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    TextButton(onClick = { /* Picker */ }) { Text("Upload Photo") }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            SectionHeader("GENERAL INFORMATION")
            CouponInputField(
                label = "Restaurant Name",
                placeholder = "e.g. King Burger",
                value = state.name,
                onValueChange = { viewModel.onEvent(RestaurantFormEvent.NameChanged(it)) }
            )
            CouponInputField(
                label = "Phone Number",
                placeholder = "090xxxxxxx",
                value = state.phone,
                onValueChange = { viewModel.onEvent(RestaurantFormEvent.PhoneChanged(it)) }
            )

            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Approved for operation", fontWeight = FontWeight.Medium)
                Switch(
                    checked = state.isApproved,
                    onCheckedChange = { viewModel.onEvent(RestaurantFormEvent.ApprovedChanged(it)) }
                )
            }

            Divider(modifier = Modifier.padding(vertical = 16.dp))

            SectionHeader("ADDRESS")
            CouponInputField(
                label = "Street",
                placeholder = "123 Street",
                value = state.street,
                onValueChange = { viewModel.onEvent(RestaurantFormEvent.StreetChanged(it)) }
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(modifier = Modifier.weight(1f)) {
                    CouponInputField("District", "Dist 1", state.district) {
                        viewModel.onEvent(RestaurantFormEvent.DistrictChanged(it))
                    }
                }
                Box(modifier = Modifier.weight(1f)) {
                    CouponInputField("City", "TP HCM", state.city) {
                        viewModel.onEvent(RestaurantFormEvent.CityChanged(it))
                    }
                }
            }

            Divider(modifier = Modifier.padding(vertical = 16.dp))

            SectionHeader("OWNER")
            OutlinedTextField(
                value = state.ownerId,
                onValueChange = { viewModel.onEvent(RestaurantFormEvent.OwnerIdChanged(it)) },
                label = { Text("Owner ID") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { viewModel.onEvent(RestaurantFormEvent.Submit) },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(12.dp),
                enabled = !state.isLoading // Disable khi đang lưu
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("Save Restaurant", fontWeight = FontWeight.Bold)
                }
            }

            if (state.error != null) {
                Text(state.error, color = Color.Red, modifier = Modifier.padding(top = 8.dp))
            }

            Spacer(modifier = Modifier.height(50.dp))
        }
    }
}
@Composable
fun CouponInputField(
    label: String,
    placeholder: String,
    value: String,
    onValueChange: (String) -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, color = MaterialTheme.colorScheme.onSurfaceVariant) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = Color.Transparent
            ),
            singleLine = true
        )
    }
}
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun RestaurantFormScreenReview() {
    DFoodTheme {
        RestaurantFormScreen(
            onNavigateBack = {}
        )
    }
}