package com.example.fooddelivery.ui.screens.food.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

@Composable
fun FoodImageHeader(
    imageRes: Int,
    isFavorite: Boolean,
    onFavoriteToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
   Box(modifier = modifier.fillMaxWidth().height(250.dp)) {
       Box(
           modifier = Modifier
               .fillMaxSize()
               .padding(bottom = 30.dp)
               .clip(RoundedCornerShape(32.dp))
               .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
       )
       Image(
           painter = painterResource(id = imageRes),
           contentDescription = null,
           modifier = Modifier.align(Alignment.Center).size(220.dp),
           contentScale = ContentScale.Crop
       )
       Surface(
           modifier = Modifier
               .align(Alignment.BottomEnd)
               .padding(end = 24.dp)
               .size(44.dp)
               .clickable { onFavoriteToggle() },
           shape = CircleShape,
           color = MaterialTheme.colorScheme.surface,
           shadowElevation = 6.dp,
           tonalElevation = 2.dp
       ) {
           Box(contentAlignment = Alignment.Center) {
               Icon(
                   imageVector =  if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                   contentDescription = null,
                   tint = if (isFavorite) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                   modifier = Modifier.size(24.dp)
               )
           }
       }
   }
}
