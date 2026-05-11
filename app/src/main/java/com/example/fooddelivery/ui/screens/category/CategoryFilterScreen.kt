package com.example.fooddelivery.ui.screens.category

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fooddelivery.ui.components.topbar.DFoodTopBar
import com.example.fooddelivery.ui.screens.category.components.CategoryTabRow
import com.example.fooddelivery.ui.screens.category.components.EmptyCategoryView
import com.example.fooddelivery.ui.screens.category.components.FoodGrid
import com.example.fooddelivery.ui.theme.DFoodTheme

@Composable
fun CategoryFilterScreen(
    onNavigateBack: () -> Unit,
    onNavigateToFoodDetail: (String) -> Unit,
    viewModel: CategoryFilterViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    CategoryFilterContent(
        state = state,
        onNavigateBack = onNavigateBack,
        onNavigateToFoodDetail = onNavigateToFoodDetail,
        onEvent = viewModel::onEvent
    )
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryFilterContent(
    state: CategoryFilterState,
    onNavigateBack: () -> Unit,
    onNavigateToFoodDetail: (String) -> Unit,
    onEvent: (CategoryFilterEvent) -> Unit
) {
    Scaffold(
        topBar = {
            DFoodTopBar(
                title = "Category",
                onBackClick = onNavigateBack,
                actions = {},
                scrollBehavior = null
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            CategoryTabRow(
                categories = state.categories,
                selectedCategoryId = state.selectedCategoryId,
                onCategorySelected = { onEvent.invoke(CategoryFilterEvent.SelectCategory(it)) }
            )
            if (state.isLoading) {
                Box(modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else if (state.foods.isEmpty()) {
                EmptyCategoryView()
            } else {
                FoodGrid(
                    foods = state.foods,
                    onFoodClick = onNavigateToFoodDetail
                )
            }
        }
    }
}
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun CategoryFilterScreenPreview() {
    DFoodTheme(darkTheme = false) {
        CategoryFilterContent(
            state = CategoryFilterState(),
            onNavigateBack = {},
            onNavigateToFoodDetail = {},
            onEvent = {}
        )
    }
}