package com.example.fooddelivery.ui.screens.admin.user

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.fooddelivery.data.remote.dto.AdminUserItemDto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserListScreen(
    onBackClick: () -> Unit,
    viewModel: AdminUserViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("User Management", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = viewModel::onSearchQueryChanged,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text("Search by name, email, phone...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                shape = CircleShape,
                singleLine = true
            )

            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(uiState.users, key = { it.id }) { user ->
                        UserRowItem(user = user)
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun UserRowItem(user: AdminUserItemDto) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (user.avatar?.isNotEmpty() == true) {
                AsyncImage(
                    model = user.avatar,
                    contentDescription = user.name,
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            shape = CircleShape
                        ),
                    contentScale = ContentScale.Crop
                )
            } else {
                Text(
                    text = user.name.take(1).uppercase(),
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = user.name,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                    maxLines = 1
                )
                Spacer(modifier = Modifier.width(8.dp))

                RoleBadge(role = user.roles.firstOrNull() ?: "CUSTOMER")
            }

            Text(
                text = user.email,
                style = MaterialTheme.typography.bodyMedium.copy(color = Color.Gray),
                maxLines = 1
            )

            Text(
                text = user.phone ?: "Phone not updated",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = if (user.phone != null) Color.DarkGray else Color.LightGray
                )
            )
        }
    }
}

@Composable
fun RoleBadge(role: String) {
    val containerColor = when (role.uppercase()) {
        "ADMIN" -> Color(0xFFFEE2E2)
        "BUSINESS" -> Color(0xFFE0F2FE)
        else -> Color(0xFFDCFCE7)
    }
    val textColor = when (role.uppercase()) {
        "ADMIN" -> Color(0xFF991B1B)
        "BUSINESS" -> Color(0xFF075985)
        else -> Color(0xFF166534)
    }

    Surface(
        color = containerColor,
        shape = CircleShape,
        modifier = Modifier.padding(vertical = 2.dp)
    ) {
        Text(
            text = role,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall.copy(
                color = textColor,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp
            )
        )
    }
}