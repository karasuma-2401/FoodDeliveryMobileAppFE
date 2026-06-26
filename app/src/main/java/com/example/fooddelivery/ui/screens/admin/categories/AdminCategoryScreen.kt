package com.example.fooddelivery.ui.screens.admin.categories

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.* // Quan trọng nhất: để nhận getValue/setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.fooddelivery.ui.components.textfield.DFoodFTextField
import com.example.fooddelivery.ui.screens.admin.components.AdminBottomBar
import com.example.fooddelivery.ui.theme.DFoodTheme
import androidx.hilt.navigation.compose.hiltViewModel
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminCategoryScreen(
    onNavigateBack: () -> Unit,
    onNavigateToAdd: () -> Unit,
    onNavigateToEdit: (Int) -> Unit,
    onNavigate: (String) -> Unit,
    viewModel: CategoryListViewModel = hiltViewModel()
) {
    // Đổi tên thành uiState để tránh lỗi 'Unresolved' hoặc 'Candidate mismatch'
    val uiState by viewModel.state
    val colorScheme = MaterialTheme.colorScheme

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Categories", fontWeight = FontWeight.Bold, color = colorScheme.primary) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, null, tint = colorScheme.primary)
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToAdd) {
                        Icon(Icons.Default.AddCircle, null, tint = colorScheme.primary)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = colorScheme.background)
            )
        },
        containerColor = colorScheme.background
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            DFoodFTextField(
                value = uiState.searchQuery,
                onValueChange = { viewModel.onSearchChange(it) },
                label = "Search category name...",
                modifier = Modifier.padding(16.dp),
                leadingIcon = { Icon(Icons.Default.Search, null, tint = colorScheme.onSurfaceVariant) }
            )

            if (uiState.isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = colorScheme.primary)
            }

            uiState.error?.let { error ->
                Text(
                    text = error,
                    color = colorScheme.error,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(uiState.categories) { category ->
                    CategoryRowItem(
                        category = category,
                        onEdit = { onNavigateToEdit(category.id) },
                        onDelete = { viewModel.deleteCategory(category.id) }
                    )
                }
            }
        }
    }
}
@Composable
fun CategoryRowItem(
    category: CategoryItem,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    val colorScheme = MaterialTheme.colorScheme

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
        border = BorderStroke(1.dp, colorScheme.outlineVariant),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Category Image Placeholder
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .background(colorScheme.surfaceVariant, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Image, null, tint = colorScheme.primary, modifier = Modifier.size(24.dp))
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    text = category.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = colorScheme.onBackground,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Order: ${category.displayOrder} • ${if (category.isActive) "Active" else "Hidden"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (category.isActive) Color(0xFF4CAF50) else colorScheme.error
                )
            }

            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(Icons.Default.MoreVert, null, tint = colorScheme.onSurfaceVariant)
                }
                DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                    DropdownMenuItem(
                        text = { Text("Edit") },
                        onClick = { showMenu = false; onEdit() },
                        leadingIcon = { Icon(Icons.Default.Edit, null) }
                    )
                    DropdownMenuItem(
                        text = { Text("Delete", color = colorScheme.error) },
                        onClick = { showMenu = false; onDelete() },
                        leadingIcon = { Icon(Icons.Default.Delete, null, tint = colorScheme.error) }
                    )
                }
            }
        }
    }
}
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun AdminCategoryScreenReview() {
    DFoodTheme {
        AdminCategoryScreen(
            onNavigateBack = {},
            onNavigateToAdd = {},
            onNavigateToEdit = {},
            onNavigate = {}
        )
    }
}
