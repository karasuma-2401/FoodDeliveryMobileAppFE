package com.example.fooddelivery.ui.screens.restaurant.component.coupon

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CouponPaginationBar(
    startItem: Int,
    endItem: Int,
    totalItems: Int,
    currentPage: Int,
    onPageClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Showing $startItem-$endItem of $totalItems vouchers",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium
        )

        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            PaginationItem(text = "<", isSelected = false, onClick = {})
            PaginationItem(text = "1", isSelected = currentPage == 1, onClick = { onPageClick(1) })
            PaginationItem(text = "2", isSelected = currentPage == 2, onClick = { onPageClick(2) })
            PaginationItem(text = "3", isSelected = currentPage == 3, onClick = { onPageClick(3) })
            PaginationItem(text = ">", isSelected = false, onClick = {})
        }
    }
}

@Composable
private fun PaginationItem(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
        border = if (isSelected) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier
            .size(36.dp)
            .clickable { onClick() }
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = text,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }
    }
}