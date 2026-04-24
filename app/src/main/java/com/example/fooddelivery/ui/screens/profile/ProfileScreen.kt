package com.example.fooddelivery.ui.screens.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.fooddelivery.R
import com.example.fooddelivery.ui.components.card.ProfileMenuCard
import com.example.fooddelivery.ui.components.card.ProfileMenuItem
import com.example.fooddelivery.ui.components.topbar.DFoodTopBar
import com.example.fooddelivery.ui.theme.DFoodTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen (
    onNavigateBack: () -> Unit,
    onEditProfile: () -> Unit,
    onLogout: () -> Unit,
) {
    Scaffold(
        topBar = {
            DFoodTopBar(
                title = "Profile",
                onBackClick = onNavigateBack,
                scrollBehavior = null
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(R.drawable.food_bowl),
                    contentDescription = "User Avatar",
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(20.dp))
                Column{
                    Text(
                        text = "Win Pear",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "I love fast food",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(modifier = Modifier.height(32.dp))

            ProfileMenuCard {
                ProfileMenuItem(
                    icon = Icons.Default.Person,
                    iconContainerColor = Color(0xFFE8F0FE),
                    iconTint = Color(0xFF4285F4),
                    tittle = "Personal Info",
                    onClick = onEditProfile
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
                ProfileMenuItem(
                    icon = Icons.Default.LocationOn,
                    iconContainerColor = Color(0xFFFFF4E5),
                    iconTint = Color(0xFFFF9800),
                    tittle = "Addresses",
                    onClick = {}
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            ProfileMenuCard {
                ProfileMenuItem(
                    icon = Icons.Default.ShoppingCart,
                    iconContainerColor = Color(0xFFE6F7EF),
                    iconTint = Color(0xFF00C569),
                    tittle = "Cart",
                    onClick = {}
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
                ProfileMenuItem(
                    icon = Icons.Default.Favorite,
                    iconContainerColor = Color(0xFFFFEBEE),
                    iconTint = Color(0xFFF44336),
                    tittle = "Favourite",
                    onClick = {}
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
                ProfileMenuItem(
                    icon = Icons.Default.Notifications,
                    iconContainerColor = Color(0xFFFFFDE7),
                    iconTint = Color(0xFFFFEB3B),
                    tittle = "Notification",
                    onClick = {}
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
                ProfileMenuItem(
                    icon = Icons.Default.CreditCard,
                    iconContainerColor = Color(0xFFF3E5F5),
                    iconTint = Color(0xFF9C27B0),
                    tittle = "Payment Method",
                    onClick = {}
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            ProfileMenuCard {
                ProfileMenuItem(
                    icon = Icons.Default.RateReview,
                    iconContainerColor = Color(0xFFE0F7FA),
                    iconTint = Color(0xFF00BCD4),
                    tittle = "Review",
                    onClick = {}
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
                ProfileMenuItem(
                    icon = Icons.Default.Settings,
                    iconContainerColor = Color(0xFFECEFF1),
                    iconTint = Color(0xFF607D8B),
                    tittle = "Settings",
                    onClick = {}
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            ProfileMenuCard {
                ProfileMenuItem(
                    icon = Icons.Default.Logout,
                    iconContainerColor = Color(0xFFFFEBEE),
                    iconTint = Color(0xFFF44336),
                    tittle = "Log out",
                    tittleColor = Color(0xFFF44336),
                    onClick = onLogout
                )
            }
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewProfileScreen() {
    DFoodTheme(darkTheme = false){
        ProfileScreen(
            onNavigateBack = {},
            onEditProfile = {},
            onLogout = {}
        )
    }
}
