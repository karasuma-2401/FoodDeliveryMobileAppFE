package com.example.fooddelivery.ui.screens.admin.categories
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.fooddelivery.ui.components.textfield.DFoodFTextField
import com.example.fooddelivery.ui.theme.DFoodTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateCategoryScreen(
    onNavigateBack: () -> Unit,
    viewModel: CategoryViewModel = hiltViewModel()
) {
    val state = viewModel.uiState
    val scrollState = rememberScrollState()

    val colorScheme = MaterialTheme.colorScheme

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Create Category",
                        color = colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null, tint = colorScheme.primary)
                    }
                },
                actions = {
                    IconButton(onClick = { /* View Mode */ }) {
                        Icon(Icons.Default.Visibility, contentDescription = null, tint = colorScheme.onSurfaceVariant)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = colorScheme.background
                )
            )
        },
        containerColor = colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
                .verticalScroll(scrollState)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .border(BorderStroke(1.dp, colorScheme.outlineVariant), RoundedCornerShape(16.dp))
                    .background(colorScheme.surface),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Surface(
                        modifier = Modifier.size(70.dp),
                        shape = RoundedCornerShape(35.dp),
                        color = colorScheme.primaryContainer
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddAPhoto,
                            contentDescription = null,
                            modifier = Modifier.padding(18.dp),
                            tint = colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "Upload Category Image",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = colorScheme.onBackground
                    )
                    Text(
                        "SVG, PNG, or JPG (max. 2MB)",
                        fontSize = 11.sp,
                        color = colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                "Category Name",
                fontWeight = FontWeight.Bold,
                color = colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            DFoodFTextField(
                value = state.categoryName,
                onValueChange = { viewModel.onEvent(CategoryEvent.NameChanged(it)) },
                label = "e.g. Italian Cuisine",
                isError = state.error != null && state.categoryName.isBlank(),
                errorMessage = state.error
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text("Parent Category", fontWeight = FontWeight.Bold, color = colorScheme.onBackground)
            DFoodFTextField(
                value = state.parentCategory,
                onValueChange = { },
                label = "",
                leadingIcon = { Icon(Icons.Default.Category, null) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Display Order", fontWeight = FontWeight.Bold, color = colorScheme.onBackground)
                    DFoodFTextField(
                        value = state.displayOrder,
                        onValueChange = { viewModel.onEvent(CategoryEvent.OrderChanged(it)) },
                        label = "",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
                Column(modifier = Modifier.weight(1.2f)) {
                    Text("Status", fontWeight = FontWeight.Bold, color = colorScheme.onBackground)
                    Surface(
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = MaterialTheme.shapes.medium,
                        border = BorderStroke(1.dp, colorScheme.outlineVariant),
                        color = colorScheme.surface
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Active", color = colorScheme.onSurface)
                            Switch(
                                checked = state.isActive,
                                onCheckedChange = { viewModel.onEvent(CategoryEvent.StatusChanged(it)) },
                                colors = SwitchDefaults.colors(
                                    checkedTrackColor = colorScheme.primary,
                                    checkedThumbColor = colorScheme.onPrimary
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = colorScheme.primaryContainer,
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Lightbulb,
                        null,
                        tint = colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            "Pro Tip",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = colorScheme.onPrimaryContainer
                        )
                        Text(
                            "High-quality square images (1:1) perform best.",
                            fontSize = 11.sp,
                            color = colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // 6. Buttons
            Button(
                onClick = { viewModel.onEvent(CategoryEvent.SaveCategory) },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorScheme.primary,
                    contentColor = colorScheme.onPrimary
                )
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(color = colorScheme.onPrimary, modifier = Modifier.size(24.dp))
                } else {
                    Text("Save Category", fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = onNavigateBack,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, colorScheme.outline) // md_theme_light_outline
            ) {
                Text("Cancel", color = colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun CreateCategoryScreenReview() {
    DFoodTheme {
        CreateCategoryScreen(
            onNavigateBack = {},
        )
    }
}