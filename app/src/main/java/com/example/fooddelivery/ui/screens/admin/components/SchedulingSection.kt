package com.example.fooddelivery.ui.screens.admin.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun SchedulingSection(
    startDate: String,
    endDate: String,
    neverExpires: Boolean,
    onNeverExpiresChange: (Boolean) -> Unit,
    onStartDateClick: () -> Unit,
    onEndDateClick: () -> Unit
) {
    Column {
        // Hãy đảm bảo ông đã định nghĩa hoặc import đúng SectionHeader này nhé
        SectionHeader("Scheduling")

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ---- Ô CHỌN NGÀY BẮT ĐẦU ----
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Start Date",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(4.dp))

                // Dùng Box bọc lại để tạo lớp phủ click
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = startDate,
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth(), // Bỏ clickable ở đây đi
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                            unfocusedContainerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f),
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                            focusedBorderColor = MaterialTheme.colorScheme.primary
                        )
                    )

                    // Lớp phủ tàng hình trên cùng bắt sự kiện click
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = { onStartDateClick() }
                            )
                    )
                }
            }

            // ---- Ô CHỌN NGÀY KẾT THÚC ----
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "End Date",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(4.dp))

                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = endDate,
                        onValueChange = {},
                        readOnly = true,
                        enabled = !neverExpires,
                        modifier = Modifier.fillMaxWidth(), // Bỏ clickable ở đây đi
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                            unfocusedContainerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f),
                            disabledContainerColor = MaterialTheme.colorScheme.surfaceDim.copy(alpha = 0.4f),
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                            disabledBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        )
                    )

                    // Lớp phủ tàng hình trên cùng bắt sự kiện click của End Date
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                enabled = !neverExpires, // Vô hiệu hóa click nếu chọn "Never expires"
                                onClick = { onEndDateClick() }
                            )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = neverExpires,
                onCheckedChange = onNeverExpiresChange,
                colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary)
            )
            Text(
                text = "This coupon never expires",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}