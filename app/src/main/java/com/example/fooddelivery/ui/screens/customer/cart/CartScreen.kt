package com.example.fooddelivery.ui.screens.customer.cart

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.fooddelivery.domain.model.Voucher
import com.example.fooddelivery.ui.components.button.DFoodButton
import com.example.fooddelivery.ui.components.topbar.DFoodTopBar
import com.example.fooddelivery.ui.screens.customer.cart.components.BillBreakdown
import com.example.fooddelivery.ui.screens.customer.cart.components.CartItemCard
import com.example.fooddelivery.ui.screens.customer.cart.components.EmptyCartView
import com.example.fooddelivery.ui.screens.customer.cart.components.RestaurantHeader
import com.example.fooddelivery.ui.screens.customer.cart.components.SwipeToDeleteContainer
import com.example.fooddelivery.ui.screens.customer.cart.components.VoucherDetailBottomSheet
import com.example.fooddelivery.ui.screens.customer.cart.components.VoucherSection
import com.example.fooddelivery.ui.screens.customer.cart.components.VoucherSelectionSheet
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    onNavigateBack: () -> Unit,
    onNavigateToCheckout: (String, String, Double) -> Unit,
    viewModel: CartViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var showVoucherSheet by remember { mutableStateOf(false) }
    var selectedDetailVoucher by remember { mutableStateOf<Voucher?>(null) }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.uiEffect.collectLatest { effect ->
            when (effect) {
                is CartUiEffect.NavigateToCheckout -> {
                    onNavigateToCheckout(effect.restaurantId, effect.restaurantName, effect.discount)
                }
                is CartUiEffect.ShowError -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    CartContent(
        state = state,
        onEvent = viewModel::onEvent,
        onBackClick = onNavigateBack,
        onCheckoutClick = { viewModel.onEvent(CartEvent.ProceedToCheckout) },
        onShowVoucherSheet = { showVoucherSheet = true }
    )

    if (showVoucherSheet) {
        ModalBottomSheet(
            onDismissRequest = { showVoucherSheet = false },
            containerColor = MaterialTheme.colorScheme.surface,
            dragHandle = { BottomSheetDefaults.DragHandle() }
        ) {
            VoucherSelectionSheet(
                vouchers = state.availableVouchers,
                selectedVoucherId = state.selectedVoucher?.id,
                promoCode = state.promoCode,
                promoError = state.promoError,
                onPromoCodeChange = { viewModel.onEvent(CartEvent.PromoCodeChanged(it)) },
                onApplyPromoCode = { viewModel.onEvent(CartEvent.ApplyPromoCode) },
                onVoucherDetailClick = { selectedDetailVoucher = it },
                onConfirm = { voucher ->
                    voucher?.let { viewModel.onEvent(CartEvent.ApplyVoucher(it)) }
                    showVoucherSheet = false
                },
                onDismiss = { showVoucherSheet = false }
            )
        }
    }

    selectedDetailVoucher?.let { voucher ->
        VoucherDetailBottomSheet(
            voucher = voucher,
            onDismissRequest = { selectedDetailVoucher = null },
            onApplyVoucher = {
                viewModel.onEvent(CartEvent.ApplyVoucher(it))
                showVoucherSheet = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartContent(
    state: CartState,
    onEvent: (CartEvent) -> Unit,
    onBackClick: () -> Unit,
    onCheckoutClick: () -> Unit,
    onShowVoucherSheet: () -> Unit
) {
    Scaffold(
        topBar = {
            DFoodTopBar(
                title = "My Cart",
                onBackClick = onBackClick,
                actions = {
                    if (!state.isCartEmpty) {
                        IconButton(onClick = { onEvent(CartEvent.ClearCart) }) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = "Clear Cart",
                                tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            )
        },
        bottomBar = {
            if (!state.isCartEmpty) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    DFoodButton(
                        text = if (state.isLoading) "Processing..." else "Proceed to Checkout",
                        onClick = onCheckoutClick,
                        enabled = state.canCheckout && !state.isLoading,
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    )
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        if (state.isCartEmpty) {
            EmptyCartView(modifier = Modifier.padding(innerPadding))
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(bottom = 120.dp)
            ) {
                state.itemsByRestaurant.forEach { (restaurantName, groupedItems) ->
                    item {
                        RestaurantHeader(
                            restaurantName = restaurantName,
                            restaurantAddress = null,
                            isSelected = state.selectedRestaurantName == restaurantName,
                            onSelect = { onEvent(CartEvent.SelectRestaurant(restaurantName)) }
                        )
                    }

                    items(
                        items = groupedItems,
                        key = { it.cartItemId ?: it.food.id }
                    ) { item ->
                        SwipeToDeleteContainer(
                            onDelete = {
                                item.cartItemId?.let { onEvent(CartEvent.RemoveItem(it)) }
                            }
                        ) {
                            CartItemCard(
                                item = item,
                                onIncrease = {
                                    item.cartItemId?.let {
                                        onEvent(
                                            CartEvent.UpdateQuantity(
                                                it,
                                                item.quantity + 1
                                            )
                                        )
                                    }
                                },
                                onDecrease = {
                                    item.cartItemId?.let {
                                        if (item.quantity > 1) {
                                            onEvent(CartEvent.UpdateQuantity(it, item.quantity - 1))
                                        } else {
                                            onEvent(CartEvent.RemoveItem(it))
                                        }
                                    }
                                },
                                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                            )
                        }
                    }
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 24.dp),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
                if (state.canCheckout) {
                    item {
                        VoucherSection(
                            promoCode = state.promoCode,
                            onPromoCodeChange = { onEvent(CartEvent.PromoCodeChanged(it)) },
                            onApplyPromoCode = { onEvent(CartEvent.ApplyPromoCode) },
                            onSelectVoucherClick = onShowVoucherSheet,
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
                        )
                    }

                    item {
                        BillBreakdown(
                            subtotal = state.subTotal,
                            discount = state.discount,
                            total = state.total,
                            modifier = Modifier.padding(horizontal = 24.dp)
                        )
                    }
                }
            }
        }
    }
}
