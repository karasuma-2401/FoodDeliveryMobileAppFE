package com.example.fooddelivery.ui.screens.restaurant.component
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
@Composable
fun ReviewSection(rating: Double, total: Int, onSeeAllClicked: () -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ){
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ){
                Text("Reviews")
                TextButton(onClick = onSeeAllClicked) {
                    Text("See All Reviews", color = MaterialTheme.colorScheme.primary)
                }
            }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Icon(Icons.Default.Star, contentDescription = null)

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                "$rating",
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text("Total $total Reviews")

        }
    }
}
}