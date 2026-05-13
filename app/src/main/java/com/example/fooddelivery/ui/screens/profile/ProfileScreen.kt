package com.example.fooddelivery.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.fooddelivery.R
import com.example.fooddelivery.ui.components.card.ProfileMenuCard
import com.example.fooddelivery.ui.components.card.ProfileMenuItem
import com.example.fooddelivery.ui.components.topbar.DFoodTopBar

@Composable
fun ProfileScreen(
    onNavigateBack: () -> Unit,
    onEditProfile: () -> Unit,
    onManageAddress: () -> Unit,
    onNavigateToCart: () -> Unit,
    onNavigateToFavourite: () -> Unit,
    onLogout: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackBarHostState = remember { SnackbarHostState() }
    var showLogoutDialog by remember { mutableStateOf(false) }

    LaunchedEffect(state.isLogoutSuccess) {
        if (state.isLogoutSuccess) {
            onLogout()
        }
    }

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let {
            snackBarHostState.showSnackbar(it)
            viewModel.onEvent(ProfileEvent.ErrorDismissed)
        }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text(text = "Log Out", fontWeight = FontWeight.Bold) },
            text = { Text(text = "Are you sure you want to log out?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                        viewModel.onEvent(ProfileEvent.LogoutClicked)
                    }
                ) {
                    Text("Yes", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    ProfileContent(
        state = state,
        onEvent = viewModel::onEvent,
        onNavigateBack = onNavigateBack,
        onEditProfile = onEditProfile,
        onManageAddress = onManageAddress,
        onNavigateToCart = onNavigateToCart,
        onNavigateToFavourite = onNavigateToFavourite,
        onShowLogoutDialog = { showLogoutDialog = true },
        snackBarHostState = snackBarHostState
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileContent(
    state: ProfileState,
    onEvent: (ProfileEvent) -> Unit,
    onNavigateBack: () -> Unit,
    onEditProfile: () -> Unit,
    onManageAddress: () -> Unit,
    onNavigateToCart: () -> Unit,
    onNavigateToFavourite: () -> Unit,
    onShowLogoutDialog: () -> Unit,
    snackBarHostState: SnackbarHostState
) {
    val refreshState = rememberPullToRefreshState()

    Scaffold(
        snackbarHost = { SnackbarHost(snackBarHostState) },
        topBar = {
            Column {
                DFoodTopBar(
                    title = "Profile",
                    onBackClick = onNavigateBack,
                    scrollBehavior = null
                )
                if (state.isLoading) {
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.primaryContainer
                    )
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        PullToRefreshBox(
            state = refreshState,
            isRefreshing = state.isLoading,
            onRefresh = { onEvent(ProfileEvent.LoadUserProfile) },
            modifier = Modifier.padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
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
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        AsyncImage(
                            model = state.user.profileImage,
                            contentDescription = "User Avatar",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                            placeholder = painterResource(R.drawable.food_bowl),
                            error = painterResource(R.drawable.food_bowl)
                        )
                    }
                    Spacer(modifier = Modifier.width(20.dp))
                    Column {
                        Text(
                            text = state.user.fullName.ifEmpty { "Lê Minh" },
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = state.user.bio.ifEmpty { "Welcome to FoodDelivery" },
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
                        onClick = onManageAddress
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))

                ProfileMenuCard {
                    ProfileMenuItem(
                        icon = Icons.Default.ShoppingCart,
                        iconContainerColor = Color(0xFFE6F7EF),
                        iconTint = Color(0xFF00C569),
                        tittle = "Cart",
                        onClick = onNavigateToCart,
                        trailing = {
                            if (state.cartItemCount > 0) {
                                Badge(
                                    containerColor = Color(0xFFFF7622),
                                    contentColor = Color.White,
                                ) {
                                    Text(
                                        text = if (state.cartItemCount > 99) "99+" else state.cartItemCount.toString(),
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
                    ProfileMenuItem(
                        icon = Icons.Default.Favorite,
                        iconContainerColor = Color(0xFFFFEBEE),
                        iconTint = Color(0xFFF44336),
                        tittle = "Favourite",
                        onClick = onNavigateToFavourite
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
                        icon = Icons.AutoMirrored.Filled.Logout,
                        iconContainerColor = Color(0xFFFFEBEE),
                        iconTint = Color(0xFFF44336),
                        tittle = "Log out",
                        tittleColor = Color(0xFFF44336),
                        onClick = onShowLogoutDialog
                    )
                }
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}
