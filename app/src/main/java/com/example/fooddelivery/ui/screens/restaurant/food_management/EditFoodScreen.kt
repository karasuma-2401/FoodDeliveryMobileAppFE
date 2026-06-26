package com.example.fooddelivery.ui.screens.restaurant.food_management

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.fooddelivery.R
import com.example.fooddelivery.ui.components.textfield.DFoodFTextField
import com.example.fooddelivery.ui.screens.restaurant.component.DFoodActionTopBar
import com.example.fooddelivery.ui.screens.restaurant.component.DFoodBottomBar
import com.example.fooddelivery.ui.screens.restaurant.component.DFoodSectionLabel
import com.example.fooddelivery.ui.screens.restaurant.component.DFoodTextArea
import com.example.fooddelivery.ui.theme.DFoodTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import coil.compose.rememberAsyncImagePainter

@Composable
fun EditFoodScreen(
    onNavigateBack: () -> Unit,
    viewModel: EditFoodViewModel = hiltViewModel()
) {
    val state by viewModel.state
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            scope.launch {
                try {
                    val file = withContext(Dispatchers.IO) {
                        val tempFile = File(context.cacheDir, "food_image_${System.currentTimeMillis()}.jpg")
                        context.contentResolver.openInputStream(it)?.use { input ->
                            FileOutputStream(tempFile).use { output ->
                                input.copyTo(output)
                            }
                        }
                        tempFile
                    }
                    viewModel.onImageSelected(file, it)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess) {
            onNavigateBack()
        }
    }

    EditFoodContent(
        state = state,
        onNavigateBack = onNavigateBack,
        onNameChange = viewModel::onNameChange,
        onCategorySelect = viewModel::onCategorySelect,
        onSizeToggle = viewModel::onSizeToggle,
        onSizePriceChange = viewModel::onSizePriceChange,
        onIngredientToggle = viewModel::onIngredientToggle,
        onDetailsChange = viewModel::onDetailsChange,
        onSaveClick = viewModel::updateFoodItem,
        onPickImageClick = { imagePickerLauncher.launch("image/*") }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditFoodContent(
    state: EditFoodState,
    onNavigateBack: () -> Unit,
    onNameChange: (String) -> Unit,
    onCategorySelect: (String) -> Unit,
    onSizeToggle: (String, Boolean) -> Unit,
    onSizePriceChange: (String, String) -> Unit,
    onIngredientToggle: (Int) -> Unit,
    onDetailsChange: (String) -> Unit,
    onSaveClick: () -> Unit,
    onPickImageClick: () -> Unit,
    onAddFoodClick: () -> Unit = {},
    onNavigate: (String) -> Unit = {}
) {
    var isCategoryDropdownExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            DFoodActionTopBar(
                title = "Food Details",
                actionText = if (state.isLoading) "SAVING..." else "SAVE",
                onActionClick = onSaveClick,
                onBackClick = onNavigateBack
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
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {

                FoodImageWithTags(
                    selectedImage = state.selectedImageUri ?: state.imageUrl ?: "",
                    tags = listOf(state.selectedCategory),
                    onClick = onPickImageClick
                )

                Column(
                    modifier = Modifier.padding(horizontal = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    Column {
                        DFoodSectionLabel(text = "ITEM NAME")
                        DFoodFTextField(value = state.itemName, onValueChange = onNameChange, label = "Enter Food Name")
                    }

                    Column {
                        DFoodSectionLabel(text = "CATEGORY")
                        ExposedDropdownMenuBox(
                            expanded = isCategoryDropdownExpanded,
                            onExpandedChange = { isCategoryDropdownExpanded = !isCategoryDropdownExpanded }
                        ) {
                            OutlinedTextField(
                                value = state.selectedCategory.ifEmpty { "Select Category" },
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = { Icon(Icons.Default.ArrowDropDown, null) },
                                modifier = Modifier.fillMaxWidth().menuAnchor(),
                                shape = MaterialTheme.shapes.medium,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                                )
                            )
                            ExposedDropdownMenu(
                                expanded = isCategoryDropdownExpanded,
                                onDismissRequest = { isCategoryDropdownExpanded = false }
                            ) {
                                state.categories.forEach { category ->
                                    DropdownMenuItem(
                                        text = { Text(text = category) },
                                        onClick = {
                                            onCategorySelect(category)
                                            isCategoryDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Column {
                        DFoodSectionLabel(text = "AVAILABLE SIZES & PRICE")
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            listOf("S", "M", "L", "XL").forEach { size ->
                                val isSelected = state.selectedSizes.containsKey(size)
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { onSizeToggle(size, !isSelected) },
                                    label = { Text(text = size, fontWeight = FontWeight.Bold) },
                                    shape = CircleShape,
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                    )
                                )
                            }
                        }

                        state.selectedSizes.keys.sorted().forEach { size ->
                            AnimatedVisibility(visible = true) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    Text(
                                        text = "Price for Size $size",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                        modifier = Modifier.width(100.dp)
                                    )
                                    Box(modifier = Modifier.weight(1f)) {
                                        DFoodFTextField(
                                            value = state.selectedSizes[size] ?: "",
                                            onValueChange = { price -> onSizePriceChange(size, price) },
                                            label = "đ 0.00"
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Column {
                        DFoodSectionLabel(text = "INGREDIENTS")
                        Spacer(modifier = Modifier.height(12.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            itemsIndexed(state.ingredients) { index, item ->
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .clickable { onIngredientToggle(index) }
                                        .width(64.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(54.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (item.isSelected) MaterialTheme.colorScheme.primary
                                                else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                                            )
                                            .border(
                                                width = 2.dp,
                                                color = if (item.isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                                shape = CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            painter = rememberAsyncImagePainter(model = item.iconUrl),
                                            contentDescription = null,
                                            tint = if (item.isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = item.name,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (item.isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontWeight = if (item.isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }

                    Column {
                        DFoodSectionLabel(text = "DESCRIPTION")
                        DFoodTextArea(
                            value = state.details,
                            onValueChange = onDetailsChange,
                            placeholder = "Enter food description..."
                        )
                    }
                }

                if (state.error != null) {
                    Text(
                        text = state.error,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier
                            .padding(16.dp)
                            .align(Alignment.CenterHorizontally)
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun FoodImageWithTags(selectedImage: Any?, tags: List<String>, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp)
            .padding(horizontal = 24.dp, vertical = 12.dp)
            .clickable { onClick() }
    ) {
        AsyncImage(
            model = selectedImage,
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
                selectedCategory = "Pizza",
                selectedSizes = mapOf("M" to "60"),
                details = "Lorem ipsum dolor sit amet..."
            ),
            onNavigateBack = {},
            onNameChange = {},
            onCategorySelect = {},
            onSizeToggle = { _, _ -> },
            onSizePriceChange = { _, _ -> },
            onIngredientToggle = {},
            onDetailsChange = {},
            onSaveClick = {},
            onPickImageClick = {}
        )
    }
}