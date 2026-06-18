package com.example.fooddelivery.ui.screens.restaurant.food_management

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.fooddelivery.R
import com.example.fooddelivery.ui.screens.restaurant.component.DFoodActionTopBar
import com.example.fooddelivery.ui.screens.restaurant.component.dashboard.DFoodBottomBar
import com.example.fooddelivery.ui.screens.restaurant.component.DFoodSectionLabel
import com.example.fooddelivery.ui.screens.restaurant.component.DFoodTextArea
import com.example.fooddelivery.ui.theme.DFoodTheme

@Composable
fun EditFoodScreen(
    onNavigateBack: () -> Unit,
    viewModel: EditFoodViewModel = hiltViewModel()
) {
    val state by viewModel.state

    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess) {
            onNavigateBack()
        }
    }

    EditFoodContent(
        state = state,
        onNavigateBack = onNavigateBack,
        onNameChange = viewModel::onNameChange,
        onPriceChange = viewModel::onPriceChange,
        onDetailsChange = viewModel::onDetailsChange,
        onSaveClick = viewModel::updateFoodItem
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditFoodContent(
    state: EditFoodState,
    onNavigateBack: () -> Unit,
    onNameChange: (String) -> Unit,
    onPriceChange: (String) -> Unit,
    onDetailsChange: (String) -> Unit,
    onSaveClick: () -> Unit,
    onAddFoodClick: () -> Unit = {},
    onNavigate: (String) -> Unit = {}
) {
    Scaffold(
        topBar = {
            DFoodActionTopBar(
                title = "Food Details",
                actionText = if (state.isLoading) "SAVING..." else "SAVE",
                onActionClick = onSaveClick,
                onBackClick = onNavigateBack
            )
        },
        bottomBar = {
            DFoodBottomBar(
                currentRoute = "",
                onNavigate = onNavigate,
                onAddClick = onAddFoodClick
            )
        }
    ) { innerPadding ->
        if (state.isLoading && state.itemName.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
            ) {
                FoodImageWithTags(
                    imageUrl = state.imageUrl ?: "",
                    tags = listOf(state.category)
                )

                FoodHeaderInfo(
                    name = state.itemName,
                    price = state.price,
                    onNameChange = onNameChange,
                    onPriceChange = onPriceChange
                )

                Spacer(modifier = Modifier.height(24.dp))

                Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                    DFoodSectionLabel(text = "Description")
                    DFoodTextArea(
                        value = state.details,
                        onValueChange = onDetailsChange,
                        placeholder = "Enter food description..."
                    )
                }

                if (state.error != null) {
                    Text(
                        text = state.error!!,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(16.dp).align(Alignment.CenterHorizontally)
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun FoodHeaderInfo(
    name: String,
    price: String,
    onNameChange: (String) -> Unit,
    onPriceChange: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
    ) {
        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            label = { Text("Food Name") },
            modifier = Modifier.fillMaxWidth(),
            textStyle = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = price,
            onValueChange = onPriceChange,
            label = { Text("Price") },
            prefix = { Text("$") },
            modifier = Modifier.fillMaxWidth(),
            textStyle = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        )
    }
}

@Composable
fun FoodImageWithTags(imageUrl: String, tags: List<String>) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp)
            .padding(24.dp)
    ) {
        AsyncImage(
            model = imageUrl,
            contentDescription = null,
            placeholder = painterResource(id = R.drawable.food_bowl),
            error = painterResource(id = R.drawable.food_bowl),
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(24.dp)),
            contentScale = ContentScale.Crop
        )
        
        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            tags.filter { it.isNotEmpty() }.forEach { tag ->
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Text(
                        text = tag,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun EditFoodScreenPreview() {
    DFoodTheme {
        EditFoodContent(
            state = EditFoodState(
                itemName = "Chicken Thai Biriyani",
                price = "60",
                details = "Lorem ipsum dolor sit amet...",
                category = "Breakfast"
            ),
            onNavigateBack = {},
            onNameChange = {},
            onPriceChange = {},
            onDetailsChange = {},
            onSaveClick = {}
        )
    }
}
