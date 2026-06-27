package com.example.fooddelivery.ui.screens.customer.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fooddelivery.domain.repository.DeliveryAddressOption
import com.example.fooddelivery.ui.components.bounceClick
import com.example.fooddelivery.ui.components.cart.CartIconWithBadge
import com.example.fooddelivery.ui.components.message.MessageIconWithBadge
import com.example.fooddelivery.ui.theme.CustomerDimens

@Composable
fun HomeTopBar(
    selectedLocationLabel: String,
    selectedLocationDetail: String,
    addressOptions: List<DeliveryAddressOption>,
    onAddressSelected: (Int) -> Unit,
    onManageAddressClick: () -> Unit,
    cartItemCount: Int,
    unreadMessageCount: Int,
    onCartClick: () -> Unit,
    onMessageClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    val hasAddresses = addressOptions.isNotEmpty()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = CustomerDimens.screenHorizontalPadding),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(modifier = Modifier.weight(1f)) {
            Column(
                modifier = Modifier
                    .bounceClick {
                        if (hasAddresses) {
                            expanded = true
                        } else {
                            onManageAddressClick()
                        }
                    }
                    .padding(vertical = 4.dp),
            ) {
                Text(
                    text = "DELIVER TO:",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.5.sp,
                    ),
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f, fill = false)) {
                        if (selectedLocationLabel.isNotBlank()) {
                            Text(
                                text = selectedLocationLabel,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onBackground,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                        Text(
                            text = selectedLocationDetail,
                            style = if (selectedLocationLabel.isNotBlank()) {
                                MaterialTheme.typography.bodySmall
                            } else {
                                MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            },
                            color = if (hasAddresses) {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            } else {
                                MaterialTheme.colorScheme.onBackground
                            },
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    Icon(
                        Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }

            DropdownMenu(
                expanded = expanded && hasAddresses,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(MaterialTheme.colorScheme.surface),
            ) {
                addressOptions.forEach { option ->
                    DropdownMenuItem(
                        text = {
                            Column {
                                Text(
                                    text = option.label,
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                                )
                                if (option.fullText.isNotBlank()) {
                                    Text(
                                        text = option.fullText,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                }
                            }
                        },
                        onClick = {
                            onAddressSelected(option.id)
                            expanded = false
                        },
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(16.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            MessageIconWithBadge(
                unreadCount = unreadMessageCount,
                onClick = onMessageClick,
                badgeBorderColor = MaterialTheme.colorScheme.background,
            )
            CartIconWithBadge(
                itemCount = cartItemCount,
                onClick = onCartClick,
                badgeBorderColor = MaterialTheme.colorScheme.background,
            )
        }
    }
}
