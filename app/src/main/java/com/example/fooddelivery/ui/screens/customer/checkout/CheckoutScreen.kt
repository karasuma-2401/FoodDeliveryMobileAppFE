package com.example.fooddelivery.ui.screens.customer.checkout

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.fooddelivery.R
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.fooddelivery.domain.model.Voucher
import com.example.fooddelivery.ui.components.button.DFoodButton
import com.example.fooddelivery.ui.components.topbar.DFoodTopBar
import com.example.fooddelivery.ui.screens.customer.voucher.VoucherDetailBottomSheet
import com.example.fooddelivery.ui.screens.customer.voucher.VoucherEntryCard
import com.example.fooddelivery.ui.screens.customer.voucher.VoucherSelectionSheet
import com.example.fooddelivery.ui.screens.customer.checkout.components.AddressCard
import com.example.fooddelivery.ui.screens.customer.checkout.components.AddressSelectionBottomSheet
import com.example.fooddelivery.ui.screens.customer.checkout.components.CheckoutBillBreakdown
import com.example.fooddelivery.ui.screens.customer.checkout.components.OrderNotesCard
import com.example.fooddelivery.ui.screens.customer.checkout.components.PaymentMethodBottomSheet
import com.example.fooddelivery.ui.screens.customer.checkout.components.PaymentMethodCard
import com.example.fooddelivery.ui.screens.customer.checkout.components.SectionTitle
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    onNavigateBack: () -> Unit,
    onNavigateToAddAddress: () -> Unit,
    onNavigateToPaymentSuccessful: (String) -> Unit,
    viewModel: CheckoutViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val paymentSheetState = rememberModalBottomSheetState()
    var showPaymentSheet by remember { mutableStateOf(false) }
    
    val voucherSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showVoucherSheet by remember { mutableStateOf(false) }

    val addressSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    
    var selectedVoucherForDetail by remember { mutableStateOf<Voucher?>(null) }
    
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.onEvent(CheckoutEvent.RefreshAddresses)
                if (state.paymentMethod is PaymentMethod.MoMo && !state.isPolling && !state.isLoading) {
                    viewModel.onEvent(CheckoutEvent.ReturnFromMoMo)
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(Unit) {
        viewModel.uiEffect.collectLatest { effect ->
            when (effect) {
                is CheckoutUiEffect.NavigateToPaymentSuccessful -> onNavigateToPaymentSuccessful(effect.orderId.toString())
                is CheckoutUiEffect.NavigateToAddAddress -> onNavigateToAddAddress()
                is CheckoutUiEffect.OpenMoMoApp -> {
                    val formattedTotal = String.format(Locale.US,"$%.2f", effect.total)
                    val message = context.getString(R.string.open_momo_app, formattedTotal)
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                     context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(effect.deeplink)))
                }
                is CheckoutUiEffect.ShowError -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    Scaffold(
        topBar = {
            DFoodTopBar(title = "Checkout", onBackClick = onNavigateBack)
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 16.dp,
                tonalElevation = 4.dp
            ) {
                Box(modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(24.dp)) {
                    val buttonText = if (state.paymentMethod is PaymentMethod.MoMo) {
                        val formattedTotal = String.format(Locale.US,"$%.2f", state.total)
                        stringResource(R.string.pay_with_momo, formattedTotal)
                    } else stringResource(R.string.place_order)

                    DFoodButton(
                        text = buttonText,
                        onClick = { viewModel.onEvent(CheckoutEvent.PlaceOrder) },
                        isLoading = state.isLoading || state.isPolling,
                        containerColor = if (state.paymentMethod is PaymentMethod.MoMo) Color(0xFFA50064) else MaterialTheme.colorScheme.primary,
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
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                SectionTitle("Delivery Address")
                AddressCard(
                    address = state.address,
                    onChangeClick = { viewModel.onEvent(CheckoutEvent.ChangeAddress) }
                )

                SectionTitle("Order Notes")
                OrderNotesCard(
                    note = state.orderNote,
                    onNoteChange = { viewModel.onEvent(CheckoutEvent.NoteChanged(it)) }
                )

                SectionTitle("Payment Method")
                PaymentMethodCard(
                    paymentMethod = state.paymentMethod,
                    onClick = { showPaymentSheet = true }
                )

                SectionTitle("Voucher giảm giá")
                VoucherEntryCard(
                    selectedVoucher = state.selectedVoucher,
                    availableCount = state.availableVouchers.count { it.isApplicable },
                    discount = state.discount,
                    onClick = { showVoucherSheet = true },
                    onRemove = { viewModel.onEvent(CheckoutEvent.ApplyVoucher(null)) }
                )

                CheckoutBillBreakdown(
                    subtotal = state.subtotal,
                    discount = state.discount,
                    total = state.total,
                    voucherLabel = state.selectedVoucher?.code
                )

                Spacer(modifier = Modifier.height(120.dp))
            }
            
            if (state.isLoading || state.isPolling) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.scrim.copy(alpha = 0.5f)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(color = Color.White)
                        if (state.isPolling) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Checking payment status...", 
                                color = Color.White, 
                                fontWeight = FontWeight.Medium,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            }
        }
    }

    if (state.showAddressSheet) {
        AddressSelectionBottomSheet(
            addresses = state.addresses,
            selectedAddressId = state.address?.id,
            onDismissRequest = { viewModel.onEvent(CheckoutEvent.DismissAddressSheet) },
            onAddressSelected = { viewModel.onEvent(CheckoutEvent.AddressSelected(it)) },
            onAddNewAddress = { viewModel.onEvent(CheckoutEvent.AddNewAddress) },
            sheetState = addressSheetState
        )
    }

    if (showPaymentSheet) {
        PaymentMethodBottomSheet(
            onDismissRequest = { showPaymentSheet = false },
            onPaymentMethodSelected = { method ->
                viewModel.onEvent(CheckoutEvent.PaymentMethodSelected(method))
                scope.launch {
                    paymentSheetState.hide()
                }.invokeOnCompletion {
                    if (!paymentSheetState.isVisible) showPaymentSheet = false
                }
            },
            selectedPaymentMethod = state.paymentMethod,
            sheetState = paymentSheetState
        )
    }

    if (showVoucherSheet) {
        ModalBottomSheet(
            onDismissRequest = { showVoucherSheet = false },
            sheetState = voucherSheetState,
            dragHandle = { BottomSheetDefaults.DragHandle() },
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            VoucherSelectionSheet(
                vouchers = state.availableVouchers,
                selectedVoucherId = state.selectedVoucher?.id,
                subtotal = state.subtotal,
                promoCode = state.promoCode,
                promoError = state.promoError,
                onPromoCodeChange = { viewModel.onEvent(CheckoutEvent.PromoCodeChanged(it)) },
                onApplyPromoCode = { viewModel.onEvent(CheckoutEvent.ApplyPromoCode) },
                onVoucherSelect = { voucher ->
                    viewModel.onEvent(CheckoutEvent.ApplyVoucher(voucher))
                    scope.launch { voucherSheetState.hide() }.invokeOnCompletion {
                        showVoucherSheet = false
                    }
                },
                onRemoveVoucher = {
                    viewModel.onEvent(CheckoutEvent.ApplyVoucher(null))
                    scope.launch { voucherSheetState.hide() }.invokeOnCompletion {
                        showVoucherSheet = false
                    }
                },
                onVoucherDetailClick = { voucher ->
                    selectedVoucherForDetail = voucher
                },
                onDismiss = {
                    scope.launch { voucherSheetState.hide() }.invokeOnCompletion {
                        showVoucherSheet = false
                    }
                }
            )
        }
    }
    
    selectedVoucherForDetail?.let { voucher ->
        VoucherDetailBottomSheet(
            voucher = voucher,
            onDismissRequest = { selectedVoucherForDetail = null },
            onApplyVoucher = {
                viewModel.onEvent(CheckoutEvent.ApplyVoucher(it))
                scope.launch { voucherSheetState.hide() }.invokeOnCompletion {
                    showVoucherSheet = false
                }
            }
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun CheckoutScreenPreview() {
    CheckoutScreen(
        onNavigateBack = {},
        onNavigateToAddAddress = {},
        onNavigateToPaymentSuccessful = { _ -> }
    )
}
