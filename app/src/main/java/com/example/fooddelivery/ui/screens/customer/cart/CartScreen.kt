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
import com.example.fooddelivery.ui.components.dialog.ConfirmDialogType
import com.example.fooddelivery.ui.components.dialog.DFoodConfirmDialog
import com.example.fooddelivery.ui.components.topbar.DFoodTopBar
import com.example.fooddelivery.ui.screens.customer.cart.components.BillBreakdown
import com.example.fooddelivery.ui.screens.customer.cart.components.CartItemCard
import com.example.fooddelivery.ui.screens.customer.cart.components.EmptyCartView
import com.example.fooddelivery.ui.screens.customer.cart.components.RestaurantHeader
import com.example.fooddelivery.ui.screens.customer.cart.components.SwipeToDeleteContainer
import com.example.fooddelivery.ui.screens.customer.voucher.VoucherDetailBottomSheet
import com.example.fooddelivery.ui.screens.customer.voucher.VoucherEntryCard
import com.example.fooddelivery.ui.screens.customer.voucher.VoucherSelectionSheet
import com.example.fooddelivery.ui.theme.CustomerDimens
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    onNavigateBack: () -> Unit,
    onNavigateToCheckout: (String, String, Double, Int?) -> Unit,
    viewModel: CartViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var showVoucherSheet by remember { mutableStateOf(false) }
    var selectedDetailVoucher by remember { mutableStateOf<Voucher?>(null) }
    var showClearCartDialog by remember { mutableStateOf(false) }
    var restaurantGroupToClear by remember { mutableStateOf<Pair<String, String>?>(null) }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.uiEffect.collectLatest { effect ->
            when (effect) {
                is CartUiEffect.NavigateToCheckout -> {
                    onNavigateToCheckout(
                        effect.restaurantId,
                        effect.restaurantName,
                        effect.discount,
                        effect.voucherId
                    )
                }
                is CartUiEffect.ShowError -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                }
                is CartUiEffect.ShowMessage -> {
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
        onShowVoucherSheet = { showVoucherSheet = true },
        onShowClearCartDialog = { showClearCartDialog = true },
        onShowClearGroupDialog = { restaurantId, restaurantName ->
            restaurantGroupToClear = restaurantId to restaurantName
        }
    )

    if (showClearCartDialog) {
        DFoodConfirmDialog(
            title = "Clear Cart",
            message = "Remove all items from your cart?",
            confirmText = "Clear",
            type = ConfirmDialogType.Destructive,
            isLoading = state.isLoading,
            onConfirm = {
                showClearCartDialog = false
                viewModel.onEvent(CartEvent.ClearCart)
            },
            onDismiss = { showClearCartDialog = false }
        )
    }

    restaurantGroupToClear?.let { (restaurantId, restaurantName) ->
        DFoodConfirmDialog(
            title = "Remove Items",
            message = "Remove all items from $restaurantName?",
            confirmText = "Remove",
            type = ConfirmDialogType.Destructive,
            isLoading = state.isLoading,
            onConfirm = {
                restaurantGroupToClear = null
                viewModel.onEvent(CartEvent.ClearRestaurantGroup(restaurantId))
            },
            onDismiss = { restaurantGroupToClear = null }
        )
    }

    if (showVoucherSheet) {
        ModalBottomSheet(
            onDismissRequest = { showVoucherSheet = false },
            containerColor = MaterialTheme.colorScheme.surface,
            dragHandle = { BottomSheetDefaults.DragHandle() }
        ) {
            VoucherSelectionSheet(
                vouchers = state.availableVouchers,
                selectedVoucherId = state.selectedVoucher?.id,
                subtotal = state.subTotal,
                promoCode = state.promoCode,
                promoError = state.promoError,
                onPromoCodeChange = { viewModel.onEvent(CartEvent.PromoCodeChanged(it)) },
                onApplyPromoCode = { viewModel.onEvent(CartEvent.ApplyPromoCode) },
                onVoucherSelect = { voucher ->
                    viewModel.onEvent(CartEvent.ApplyVoucher(voucher))
                    showVoucherSheet = false
                },
                onRemoveVoucher = {
                    viewModel.onEvent(CartEvent.ApplyVoucher(null))
                    showVoucherSheet = false
                },
                onVoucherDetailClick = { selectedDetailVoucher = it },
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
    onShowVoucherSheet: () -> Unit,
    onShowClearCartDialog: () -> Unit = {},
    onShowClearGroupDialog: (restaurantId: String, restaurantName: String) -> Unit = { _, _ -> }
) {
    Scaffold(
        topBar = {
            DFoodTopBar(
                title = "My Cart",
                onBackClick = onBackClick,
                actions = {
                    if (!state.isCartEmpty) {
                        IconButton(onClick = onShowClearCartDialog) {
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
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 12.dp,
                    tonalElevation = 2.dp,
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(CustomerDimens.bottomBarPadding)
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
                        Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
                    }
                }
            }
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        if (state.isCartEmpty) {
            EmptyCartView(modifier = Modifier.padding(innerPadding))
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(bottom = CustomerDimens.screenBottomSpacer)
            ) {
                state.restaurantSections.forEach { section ->
                    val group = section.group
                    item {
                        RestaurantHeader(
                            restaurantName = group.restaurantName,
                            deliveryFee = group.deliveryFee,
                            estimatedDeliveryTime = group.estimatedDeliveryTime,
                            isSelected = state.selectedRestaurantId == group.restaurantId,
                            onSelect = { onEvent(CartEvent.SelectRestaurant(group.restaurantId)) },
                            onClearGroup = {
                                onShowClearGroupDialog(group.restaurantId, group.restaurantName)
                            }
                        )
                    }

                    items(
                        items = section.items,
                        key = { it.cartItemId ?: "${it.food.id}_${it.foodSizeId}" }
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
                                        if (item.quantity >= 99) return@let
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
                                modifier = Modifier.padding(
                                    horizontal = CustomerDimens.screenHorizontalPadding,
                                    vertical = CustomerDimens.itemVerticalPadding
                                )
                            )
                        }
                    }
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = CustomerDimens.screenHorizontalPadding),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
                if (state.canCheckout) {
                    item {
                        VoucherEntryCard(
                            selectedVoucher = state.selectedVoucher,
                            availableCount = state.availableVouchers.count { it.isApplicable },
                            discount = state.discount,
                            onClick = onShowVoucherSheet,
                            onRemove = { onEvent(CartEvent.ApplyVoucher(null)) },
                            modifier = Modifier.padding(
                                horizontal = CustomerDimens.screenHorizontalPadding,
                                vertical = CustomerDimens.cardPaddingLg
                            )
                        )
                    }

                    item {
                        BillBreakdown(
                            subtotal = state.subTotal,
                            deliveryFee = state.selectedDeliveryFee,
                            discount = state.discount,
                            total = state.total,
                            voucherLabel = state.selectedVoucher?.code,
                            modifier = Modifier.padding(horizontal = 24.dp)
                        )
                    }
                }
            }
        }
    }
}
