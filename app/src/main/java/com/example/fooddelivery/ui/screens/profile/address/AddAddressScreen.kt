package com.example.fooddelivery.ui.screens.profile.address

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.WorkOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fooddelivery.ui.screens.profile.address.components.AddressTypeItem
import com.example.fooddelivery.ui.components.button.DFoodButton
import com.example.fooddelivery.ui.components.header.LocationPickerHeader
import com.example.fooddelivery.ui.screens.profile.address.components.CustomAddressTextField
import com.example.fooddelivery.ui.components.topbar.DFoodTopBar
import com.example.fooddelivery.ui.theme.DFoodTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAddressScreen(
    onNavigateBack: () -> Unit,
    onSaveLocation: () -> Unit
) {
    var selectedType by remember { mutableStateOf("Home") }
    var city by remember { mutableStateOf("") }
    var streetName by remember { mutableStateOf("2425 Market Street") }

    Scaffold(
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
            LocationPickerHeader(onSearchClick = { /* function xử lý when search location */ } )
            
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = (-24).dp),
                shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                color = MaterialTheme.colorScheme.surface,
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
                            isSelected = selectedType == "Home",
                            selectedColor = Color(0xFF4285F4),
                            onClick = { selectedType = "Home" },
                            modifier = Modifier.weight(1f)
                        )
                        AddressTypeItem(
                            label = "Work",
                            icon = Icons.Outlined.WorkOutline,
                            isSelected = selectedType == "Work",
                            selectedColor = Color(0xFF9C27B0),
                            onClick = { selectedType = "Work" },
                            modifier = Modifier.weight(1f)
                        )
                        AddressTypeItem(
                            label = "Other",
                            icon = Icons.Outlined.MoreHoriz,
                            isSelected = selectedType == "Other",
                            selectedColor = MaterialTheme.colorScheme.primary,
                            onClick = { selectedType = "Other" },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "CITY",
                        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    CustomAddressTextField(
                        value = city,
                        onValueChange = { city = it },
                        placeholder = "Optional"
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "STREET NAME",
                        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    CustomAddressTextField(
                        value = streetName,
                        onValueChange = { streetName = it },
                        leadingIcon = Icons.Default.Apartment
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    DFoodButton(
                        text = "SAVE LOCATION",
                        onClick = onSaveLocation,
                        leadingIcon = {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewAddAddressScreen() {
    DFoodTheme {
        AddAddressScreen(onNavigateBack = {}, onSaveLocation = {})
    }
}
