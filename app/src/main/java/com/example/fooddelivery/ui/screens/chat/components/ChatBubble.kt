package com.example.fooddelivery.ui.screens.chat.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.example.fooddelivery.data.local.room.entity.MessageEntity
import com.example.fooddelivery.util.formatMessageDisplayTime
import com.example.fooddelivery.util.senderIdsMatch

@Composable
fun ChatBubble(
    message: MessageEntity,
    currentUserId: String,
    restaurantImage: String,
    restaurantName: String = "Restaurant"
) {
    val context = LocalContext.current
    val isMe = senderIdsMatch(message.senderId, currentUserId)
    
    val bubbleColor = if (isMe) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer
    val textColor = if (isMe) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondaryContainer

    val timeStr = remember(message.createdAt, message.isSending) {
        if (message.isSending) "" else formatMessageDisplayTime(message.createdAt)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
    ) {
        Row(
            verticalAlignment = Alignment.Bottom,
            modifier = Modifier.padding(horizontal = 8.dp)
        ) {
            if (!isMe) {
                SubcomposeAsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(restaurantImage)
                        .crossfade(true)
                        .build(),
                    contentDescription = "$restaurantName avatar",
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            
            Surface(
                color = if (message.imageUrl.isNullOrBlank()) bubbleColor else MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(
                    topStart = 20.dp,
                    topEnd = 20.dp,
                    bottomStart = if (isMe) 20.dp else 4.dp,
                    bottomEnd = if (isMe) 4.dp else 20.dp
                ),
                shadowElevation = if (isMe) 2.dp else 1.dp,
                tonalElevation = 2.dp,
                modifier = Modifier.widthIn(max = 280.dp)
            ) {
                Column {
                    if (!message.imageUrl.isNullOrBlank()) {
                        SubcomposeAsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(message.imageUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Image message",
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 120.dp, max = 240.dp)
                                .clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Crop,
                            loading = {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(120.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        strokeWidth = 2.dp
                                    )
                                }
                            },
                            error = {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(120.dp)
                                        .background(MaterialTheme.colorScheme.surfaceVariant),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Image unavailable",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        )
                    }
                    if (message.content.isNotBlank()) {
                        Text(
                            text = message.content,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (message.imageUrl.isNullOrBlank()) textColor
                            else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(
                top = 4.dp, 
                start = if (isMe) 0.dp else 48.dp,
                end = if (isMe) 12.dp else 0.dp
            )
        ) {
            if (timeStr.isNotBlank()) {
                Text(
                    text = timeStr,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
            if (isMe) {
                if (message.isSending) {
                    if (timeStr.isNotBlank()) {
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    CircularProgressIndicator(
                        modifier = Modifier.size(10.dp), 
                        strokeWidth = 1.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                } else if (message.isFailed) {
                    if (timeStr.isNotBlank()) {
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    Text("!", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                } else if (message.isRead) {
                    Text(
                        text = " · Seen",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}
