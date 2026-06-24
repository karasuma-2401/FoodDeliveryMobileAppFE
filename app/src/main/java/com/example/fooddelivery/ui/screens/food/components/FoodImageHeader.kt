package com.example.fooddelivery.ui.screens.food.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.fooddelivery.R

@Composable
fun FoodImageHeader(
    imageRes: Int = R.drawable.food_bowl,
    imageUrl: String? = null,
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
       AsyncImage(
           model = imageUrl ?: imageRes,
           contentDescription = null,
           modifier = Modifier.align(Alignment.Center).size(220.dp),
           contentScale = ContentScale.Crop
       )
   }
}
