package com.example.fooddelivery.ui.screens.auth.role

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fooddelivery.R
import com.example.fooddelivery.ui.components.button.DFoodButton
import com.example.fooddelivery.ui.theme.DFoodTheme

@Composable
fun RoleSelectionScreen(
    onRoleConfirmed: (UserRole) -> Unit
) {
    var selectedRole by remember { mutableStateOf<UserRole?>(null) }

    val gradientBackground = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
            MaterialTheme.colorScheme.background
        )
    )

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradientBackground)
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
                    .navigationBarsPadding(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = buildAnnotatedString {
                        append("Join ")
                        withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary)) {
                            append("D")
                        }
                        append("Food As")
                    },
                    style = MaterialTheme.typography.headlineLarge.copy(fontSize = 32.sp),
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Choose your account type to continue",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp, bottom = 40.dp)
                )

                RoleCard(
                    title = "Customer",
                    description = "Order food from your favorite local restaurants right to your doorstep.",
                    iconRes = R.drawable.ic_customer,
                    isSelected = selectedRole == UserRole.CUSTOMER,
                    onClick = { selectedRole = UserRole.CUSTOMER }
                )

                Spacer(modifier = Modifier.height(16.dp))

                RoleCard(
                    title = "Restaurant Partner",
                    description = "Manage your digital kitchen, reach more customers, and boost your sales.",
                    iconRes = R.drawable.ic_restaurant,
                    isSelected = selectedRole == UserRole.RESTAURANT,
                    onClick = { selectedRole = UserRole.RESTAURANT }
                )

                Spacer(modifier = Modifier.height(48.dp))

                DFoodButton(
                    text = "Continue",
                    onClick = { selectedRole?.let { onRoleConfirmed(it) } },
                    enabled = selectedRole != null,
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = null,
                            tint = LocalContentColor.current
                        )
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoleCard(
    title: String,
    description: String,
    iconRes: Int,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
    val borderStroke = if (isSelected) {
        BorderStroke(2.dp, borderColor)
    } else {
        BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    }
    
    val elevation = if (isSelected) 8.dp else 0.dp

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        border = borderStroke,
        elevation = CardDefaults.cardElevation(defaultElevation = elevation),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(56.dp),
                shape = RoundedCornerShape(16.dp),
                color = if (isSelected) {
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                } else {
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        painter = painterResource(id = iconRes),
                        contentDescription = null,
                        tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Selected",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun RoleSelectionPreview() {
    DFoodTheme(darkTheme = false) {
        RoleSelectionScreen(
            onRoleConfirmed = { selectedRole ->
            }
        )
    }
}
