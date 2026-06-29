package com.example.fooddelivery.ui.screens.admin.components



import androidx.compose.foundation.layout.*

import androidx.compose.foundation.shape.RoundedCornerShape

import androidx.compose.material.icons.Icons

import androidx.compose.material.icons.filled.ArrowDropDown

import androidx.compose.material3.*

import androidx.compose.runtime.*

import androidx.compose.ui.Modifier

import androidx.compose.ui.text.font.FontWeight

import androidx.compose.ui.unit.dp



@OptIn(ExperimentalMaterial3Api::class)

@Composable

fun DiscountDetailsSection(

    discountType: String,

    onTypeChange: (String) -> Unit,

    discountValue: String,

    onValueChange: (String) -> Unit,

    maxDiscount: String,

    onMaxDiscountChange: (String) -> Unit

) {

    var expanded by remember { mutableStateOf(false) }

    val types = listOf("Percentage Discount", "Fixed Amount Discount")



    Column {

        SectionHeader("Discount Details")



        Text("Coupon Type", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onBackground)

        Spacer(modifier = Modifier.height(4.dp))



        ExposedDropdownMenuBox(

            expanded = expanded,

            onExpandedChange = { expanded = !expanded }

        ) {

            OutlinedTextField(

                value = discountType,

                onValueChange = {},

                readOnly = true,

                trailingIcon = { Icon(Icons.Default.ArrowDropDown, null, tint = MaterialTheme.colorScheme.primary) },

                modifier = Modifier.fillMaxWidth().menuAnchor(),

                shape = RoundedCornerShape(12.dp),

                colors = OutlinedTextFieldDefaults.colors(

                    focusedContainerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),

                    unfocusedContainerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f),

                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,

                    focusedBorderColor = MaterialTheme.colorScheme.primary

                )

            )

            ExposedDropdownMenu(

                expanded = expanded,

                onDismissRequest = { expanded = false }

            ) {

                types.forEach { type ->

                    DropdownMenuItem(

                        text = { Text(type) },

                        onClick = {

                            onTypeChange(type)

                            expanded = false

                        }

                    )

                }

            }

        }



        Spacer(modifier = Modifier.height(12.dp))



        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {

            Column(modifier = Modifier.weight(1f)) {

                Text("Discount Value", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onBackground)

                Spacer(modifier = Modifier.height(4.dp))

                val isPercent = discountType.contains("percent", ignoreCase = true)

                OutlinedTextField(

                    value = discountValue,

                    onValueChange = onValueChange,

                    suffix = { Text(if (isPercent) "%" else "$", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold) },

                    shape = RoundedCornerShape(12.dp),

                    colors = OutlinedTextFieldDefaults.colors(

                        focusedContainerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),

                        unfocusedContainerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f),

                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,

                        focusedBorderColor = MaterialTheme.colorScheme.primary

                    )

                )

            }

            if (discountType.contains("percent", ignoreCase = true)) {

                Column(modifier = Modifier.weight(1f)) {

                    Text("Max Discount", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onBackground)

                    Spacer(modifier = Modifier.height(4.dp))

                    OutlinedTextField(

                        value = maxDiscount,

                        onValueChange = onMaxDiscountChange,

                        prefix = { Text("$ ", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold) },

                        shape = RoundedCornerShape(12.dp),

                        colors = OutlinedTextFieldDefaults.colors(

                            focusedContainerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),

                            unfocusedContainerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f),

                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,

                            focusedBorderColor = MaterialTheme.colorScheme.primary

                        )

                    )

                }

            }

        }

    }

}