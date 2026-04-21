package com.example.fooddelivery.ui.components.header
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.fooddelivery.R
@Composable
fun HeaderSection(
    location: String,
    onLocationClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Outlined.Menu, contentDescription = null)

        Column(
            modifier = Modifier.clickable { onLocationClick() }
        ) {
            Text(stringResource(R.string.dashboard_location).uppercase(), style = MaterialTheme.typography.labelSmall,color = MaterialTheme.colorScheme.primary)
            Text(location, fontWeight = FontWeight.Bold)
        }

        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(50))
                .background(MaterialTheme.colorScheme.primary)
        )
    }
}