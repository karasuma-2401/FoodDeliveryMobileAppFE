package com.example.fooddelivery.ui.components.ingredient

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BakeryDining
import androidx.compose.material.icons.filled.BubbleChart
import androidx.compose.material.icons.filled.Grass
import androidx.compose.material.icons.filled.Grain
import androidx.compose.material.icons.filled.KebabDining
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.LocalFlorist
import androidx.compose.material.icons.filled.RamenDining
import androidx.compose.material.icons.filled.RiceBowl
import androidx.compose.material.icons.filled.SetMeal
import androidx.compose.ui.graphics.vector.ImageVector

object IngredientIconMapper {
    private val icons = mapOf(
        "pork" to Icons.Filled.KebabDining,
        "herbs" to Icons.Filled.Grass,
        "mint" to Icons.Filled.LocalFlorist,
        "bun" to Icons.Filled.BakeryDining,
        "rice" to Icons.Filled.RiceBowl,
        "noodle" to Icons.Filled.RamenDining,
        "rice-noodle" to Icons.Filled.RamenDining,
        "chili" to Icons.Filled.LocalFireDepartment,
        "milk" to Icons.Filled.LocalDrink,
        "boba" to Icons.Filled.BubbleChart,
        "tea" to Icons.Filled.LocalCafe,
        "corn" to Icons.Filled.Grain,
        "seafood" to Icons.Filled.SetMeal,
    )

    fun resolve(iconKey: String?): ImageVector {
        if (iconKey.isNullOrBlank()) return Icons.Filled.SetMeal
        return icons[iconKey.trim().lowercase()] ?: Icons.Filled.SetMeal
    }
}
