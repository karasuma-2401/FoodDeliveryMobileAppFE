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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.fooddelivery.R
import com.example.fooddelivery.ui.components.card.ProfileMenuCard
import com.example.fooddelivery.ui.components.topbar.DFoodTopBar
import com.example.fooddelivery.ui.theme.DFoodTheme

@Composable
fun ProfileScreen(
    onNavigateBack: () -> Unit,
    onEditProfile: () -> Unit,
    onManageAddress: () -> Unit,
    onNavigateToCart: () -> Unit,
    onNavigateToFavourite: () -> Unit,
    onNavigateToNotification: () -> Unit,
    onNavigateToPaymentMethod: () -> Unit,
    onNavigateToReview: () -> Unit,
    onChangePassword: () -> Unit,
    onResetEmail: () -> Unit,
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
        onNavigateToNotification = onNavigateToNotification,
        onNavigateToPaymentMethod = onNavigateToPaymentMethod,
        onNavigateToReview = onNavigateToReview,
        onChangePassword = onChangePassword,
        onResetEmail = onResetEmail,
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
    onNavigateToNotification: () -> Unit,
    onNavigateToPaymentMethod: () -> Unit,
    onNavigateToReview: () -> Unit,
    onChangePassword: () -> Unit,
    onResetEmail: () -> Unit,
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
            isRefreshing = state.isRefreshing,
            onRefresh = { onEvent(ProfileEvent.RefreshUserProfile) },
            modifier = Modifier.padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(24.dp))
                state.user?.let { user ->
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
                                model = user.profileImage,
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
                                text = user.fullName.ifEmpty { "Lê Minh" },
                                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = user.bio.ifEmpty { "Welcome to FoodDelivery" },
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
                ProfileMenuCard(
                    icon = Icons.Default.Person,
                    iconContainerColor = Color(0xFFE8F0FE),
                    iconTint = Color(0xFF4285F4),
                    tittle = "Personal Info",
                    onClick = onEditProfile
                )
                ProfileMenuCard(
                    icon = Icons.Default.Email,
                    iconContainerColor = Color(0xFFE6F7EF),
                    iconTint = Color(0xFF00C569),
                    tittle = "Change Email",
                    onClick = onResetEmail
                )
                ProfileMenuCard(
                    icon = Icons.Default.Lock,
                    iconContainerColor = Color(0xFFF3E5F5),
                    iconTint = Color(0xFF9C27B0),
                    tittle = "Change Password",
                    onClick = onChangePassword
                )
                ProfileMenuCard(
                    icon = Icons.Default.LocationOn,
                    iconContainerColor = Color(0xFFFFF4E5),
                    iconTint = Color(0xFFFF9800),
                    tittle = "Addresses",
                    onClick = onManageAddress
                )

                Spacer(modifier = Modifier.height(16.dp))
                ProfileMenuCard(
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
                ProfileMenuCard(
                    icon = Icons.Default.Favorite,
                    iconContainerColor = Color(0xFFFFEBEE),
                    iconTint = Color(0xFFF44336),
                    tittle = "Favourite",
                    onClick = onNavigateToFavourite
                )
                ProfileMenuCard(
                    icon = Icons.Default.Notifications,
                    iconContainerColor = Color(0xFFFFFDE7),
                    iconTint = Color(0xFFFFEB3B),
                    tittle = "Notification",
                    onClick = onNavigateToNotification,
                    trailing = {
                        if (state.unreadNotificationCount > 0) {
                            Badge(
                                containerColor = Color(0xFFFF7622),
                                contentColor = Color.White,
                            ) {
                                Text(
                                    text = if (state.unreadNotificationCount > 99) "99+" else state.unreadNotificationCount.toString(),
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                )
                ProfileMenuCard(
                    icon = Icons.Default.CreditCard,
                    iconContainerColor = Color(0xFFF3E5F5),
                    iconTint = Color(0xFF9C27B0),
                    tittle = "Payment Method",
                    onClick = onNavigateToPaymentMethod
                )
                ProfileMenuCard(
                    icon = Icons.Default.RateReview,
                    iconContainerColor = Color(0xFFE0F7FA),
                    iconTint = Color(0xFF00BCD4),
                    tittle = "My Reviews",
                    onClick = onNavigateToReview
                )

                Spacer(modifier = Modifier.height(16.dp))
                ProfileMenuCard(
                    icon = if (state.isDarkMode) Icons.Default.DarkMode else Icons.Default.LightMode,
                    iconContainerColor = if (state.isDarkMode) Color(0xFF2D2D2D) else Color(0xFFFFF9C4),
                    iconTint = if (state.isDarkMode) Color(0xFFBB86FC) else Color(0xFFFBC02D),
                    tittle = "Dark Mode",
                    onClick = { onEvent(ProfileEvent.ToggleDarkMode(!state.isDarkMode)) },
                    trailing = {
                        Switch(
                            checked = state.isDarkMode,
                            onCheckedChange = { onEvent(ProfileEvent.ToggleDarkMode(it)) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color(0xFFFF7622)
                            )
                        )
                    }
                )
                ProfileMenuCard(
                    icon = if (state.isNotificationsEnabled) Icons.Default.NotificationsActive else Icons.Default.NotificationsOff,
                    iconContainerColor = Color(0xFFE8F5E9),
                    iconTint = Color(0xFF4CAF50),
                    tittle = "Push Notifications",
                    onClick = { onEvent(ProfileEvent.ToggleNotifications(!state.isNotificationsEnabled)) },
                    trailing = {
                        Switch(
                            checked = state.isNotificationsEnabled,
                            onCheckedChange = { onEvent(ProfileEvent.ToggleNotifications(it)) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                                checkedTrackColor = MaterialTheme.colorScheme.primary,
                                uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant,
                                uncheckedBorderColor = MaterialTheme.colorScheme.outline
                            )
                        )
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))
                ProfileMenuCard(
                    icon = Icons.AutoMirrored.Filled.Logout,
                    iconContainerColor = Color(0xFFFFEBEE),
                    iconTint = Color(0xFFF44336),
                    tittle = "Log out",
                    tittleColor = Color(0xFFF44336),
                    onClick = onShowLogoutDialog
                )

                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ProfileScreenPreview() {
    DFoodTheme(darkTheme = false) {
        ProfileContent(
            state = ProfileState(),
            onEvent = {},
            onNavigateBack = {},
            onEditProfile = {},
            onManageAddress = {},
            onNavigateToCart = {},
            onNavigateToFavourite = {},
            onNavigateToNotification = {},
            onNavigateToPaymentMethod = {},
            onNavigateToReview = {},
            onChangePassword = {},
            onResetEmail = {},
            onShowLogoutDialog = {},
            snackBarHostState = SnackbarHostState()
        )
    }
}