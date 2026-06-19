package com.example.fooddelivery.ui.screens.auth.register

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fooddelivery.ui.components.topbar.DFoodTopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PolicyScreen(
    type: String,
    onNavigateBack: () -> Unit
) {
    val title = if (type == "terms") "Terms of Service" else "Privacy Policy"
    val content = if (type == "terms") {
        """
        1. Acceptance of Terms
        By accessing and using DFood, you agree to be bound by these Terms of Service. If you do not agree, please do not use our services.

        2. Use of Services
        You must be at least 18 years old to use this app. You are responsible for maintaining the confidentiality of your account credentials.

        3. Ordering and Payment
        All orders are subject to availability. Prices are subject to change without notice. Payments must be made through authorized payment methods provided in the app.

        4. Delivery
        We aim to deliver within the estimated timeframe, but delays may occur due to traffic, weather, or other factors beyond our control.

        5. Cancellations and Refunds
        Cancellations are only allowed within 2 minutes of placing an order. Refunds are handled on a case-by-case basis.

        6. Limitation of Liability
        DFood is not liable for any indirect, incidental, or consequential damages arising from your use of the service.
        """.trimIndent()
    } else {
        """
        1. Information Collection
        We collect information you provide directly to us, such as your name, email address, phone number, and delivery address when you create an account.

        2. Use of Information
        We use your information to process orders, improve our services, and communicate with you about promotions and updates.

        3. Data Sharing
        We share your information with restaurants and delivery partners to fulfill your orders. We do not sell your personal data to third parties.

        4. Security
        We implement industry-standard security measures to protect your data. However, no method of transmission over the internet is 100% secure.

        5. Your Rights
        You have the right to access, update, or delete your personal information at any time through your profile settings.

        6. Cookies
        We use cookies and similar technologies to enhance your experience and analyze app usage.
        """.trimIndent()
    }

    Scaffold(
        topBar = {
            DFoodTopBar(
                title = title,
                onBackClick = onNavigateBack
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Last updated: October 2023",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = content,
                style = MaterialTheme.typography.bodyLarge.copy(
                    lineHeight = 24.sp,
                    letterSpacing = 0.5.sp
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}
