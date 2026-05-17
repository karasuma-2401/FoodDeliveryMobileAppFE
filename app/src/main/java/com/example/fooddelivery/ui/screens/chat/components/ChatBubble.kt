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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.fooddelivery.data.local.room.entity.MessageEntity
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ChatBubble(
    message: MessageEntity,
    currentUserId: String,
    restaurantImage: String,
    restaurantName: String = "Restaurant"
) {
    val isMe = message.senderId == currentUserId
    
    // Modern chat bubble colors
    val bubbleColor = if (isMe) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer
    val textColor = if (isMe) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondaryContainer

    val timeStr = try {
        val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
        sdf.format(Date(message.createdAt.toLong()))
    } catch (e: Exception) {
        message.createdAt
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
                AsyncImage(
                    model = restaurantImage,
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
                color = bubbleColor,
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
                    if (message.imageUrl != null) {
                        AsyncImage(
                            model = message.imageUrl,
                            contentDescription = "Image message",
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 200.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .padding(4.dp),
                            contentScale = ContentScale.Fit
                        )
                    }
                    if (message.content.isNotBlank()) {
                        Text(
                            text = message.content,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = textColor
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
            Text(
                text = timeStr,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
            if (isMe) {
                Spacer(modifier = Modifier.width(4.dp))
                if (message.isSending) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(10.dp), 
                        strokeWidth = 1.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                } else if (message.isFailed) {
                    Text("!", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
