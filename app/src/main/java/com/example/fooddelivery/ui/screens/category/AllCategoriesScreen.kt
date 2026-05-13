package com.example.fooddelivery.ui.screens.category

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.fooddelivery.ui.components.topbar.DFoodTopBar
import com.example.fooddelivery.ui.screens.home.components.CategoryItem

@Composable
fun AllCategoriesScreen(
    onNavigateBack: () -> Unit,
    onNavigateToCategory: (String) -> Unit,
    viewModel: AllCategoriesViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    AllCategoriesContent(
        state = state,
        onNavigateBack = onNavigateBack,
        onNavigateToCategory = onNavigateToCategory
    )
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllCategoriesContent(
    state: AllCategoriesState,
    onNavigateBack: () -> Unit,
    onNavigateToCategory: (String) -> Unit
) {
    Scaffold(
        topBar = {
            DFoodTopBar(
                title = "All Categories",
                onBackClick = onNavigateBack,
                scrollBehavior = null
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        if (state.isLoading) {
            Box(modifier = Modifier.padding(innerPadding).fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(state.categories) { category ->
                    CategoryItem(
                        category = category,
                        onClick = { onNavigateToCategory(category.id) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}