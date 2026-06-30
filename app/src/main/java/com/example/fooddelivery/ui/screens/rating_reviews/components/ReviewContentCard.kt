package com.example.fooddelivery.ui.screens.rating_reviews.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fooddelivery.R
import com.example.fooddelivery.domain.model.ReviewItem

enum class UserRole { CUSTOMER, BUSINESS, ADMIN }

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ReviewContentCard(
    review: ReviewItem,
    userRole: UserRole,
    currentUserId: Int? = null,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onReplyClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isMenuExpanded by remember { mutableStateOf(false) }
    val isReviewOwner = currentUserId != null && review.userId != 0 && currentUserId == review.userId

    val shouldShowMenu = when (userRole) {
        UserRole.CUSTOMER -> isReviewOwner
        UserRole.BUSINESS -> true
        UserRole.ADMIN -> true
    }

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = review.date,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (shouldShowMenu) {
                    Box {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_more_horiz),
                            contentDescription = "More options",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .size(18.dp)
                                .clickable { isMenuExpanded = true }
                        )

                        DropdownMenu(
                            expanded = isMenuExpanded,
                            onDismissRequest = { isMenuExpanded = false }
                        ) {
                            when (userRole) {
                                UserRole.CUSTOMER -> {
                                    if (isReviewOwner) {
                                        DropdownMenuItem(
                                            text = { Text("Edit") },
                                            onClick = { isMenuExpanded = false; onEditClick() }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("Delete") },
                                            onClick = { isMenuExpanded = false; onDeleteClick() }
                                        )
                                    }
                                }
                                UserRole.BUSINESS -> {
                                    DropdownMenuItem(
                                        text = { Text("Reply") },
                                        onClick = { isMenuExpanded = false; onReplyClick() }
                                    )
                                }
                                UserRole.ADMIN -> {
                                    DropdownMenuItem(
                                        text = { Text("Reply") },
                                        onClick = { isMenuExpanded = false; onReplyClick() }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Delete") },
                                        onClick = { isMenuExpanded = false; onDeleteClick() }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = review.title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(6.dp))

            StarRatingBar(rating = review.rating)

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = review.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 20.sp
            )

            if (!review.tags.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    review.tags.forEach { tag ->
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
                        ) {
                            Text(
                                text = tag,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            if (!review.reply.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(14.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(12.dp)
                ) {
                    Column {
                        Text(
                            text = "Restaurant's reply",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = review.reply,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}