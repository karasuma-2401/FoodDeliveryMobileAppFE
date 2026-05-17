package com.example.fooddelivery.ui.screens.checkout

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.ui.unit.dp
import com.example.fooddelivery.R
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.fooddelivery.ui.components.button.DFoodButton
import com.example.fooddelivery.ui.components.topbar.DFoodTopBar
import com.example.fooddelivery.ui.screens.checkout.components.*
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    onNavigateBack: () -> Unit,
    onNavigateToAddAddress: () -> Unit,
    onNavigateToPaymentSuccessful: () -> Unit,
    viewModel: CheckoutViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState()
    var showPaymentSheet by remember { mutableStateOf(false) }
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
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
                is CheckoutUiEffect.NavigateToPaymentSuccessful -> onNavigateToPaymentSuccessful()
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

                SectionTitle("Delivery Options")
                DeliveryOptionsCard(
                    selectedOption = state.selectedDeliveryOption,
                    onOptionSelected = { viewModel.onEvent(CheckoutEvent.DeliveryOptionSelected(it)) }
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

                CheckoutBillBreakdown(
                    subtotal = state.subtotal,
                    deliveryFee = state.deliveryFee,
                    discount = state.discount,
                    total = state.total
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

    if (showPaymentSheet) {
        PaymentMethodBottomSheet(
            onDismissRequest = { showPaymentSheet = false },
            onPaymentMethodSelected = { method ->
                viewModel.onEvent(CheckoutEvent.PaymentMethodSelected(method))
                scope.launch { 
                    sheetState.hide() 
                }.invokeOnCompletion { 
                    if (!sheetState.isVisible) showPaymentSheet = false 
                }
            },
            selectedPaymentMethod = state.paymentMethod,
            sheetState = sheetState
        )
    }
}
