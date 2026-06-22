package com.example.fooddelivery.ui.screens.restaurant.profile
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.fooddelivery.R
import com.example.fooddelivery.ui.components.sectionheader.SectionHeader
import com.example.fooddelivery.ui.components.topbar.DFoodTopBar
import com.example.fooddelivery.ui.theme.DFoodTheme
import com.example.fooddelivery.ui.screens.restaurant.component.RestaurantInputField
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RestaurantPersonalInfoScreen(
    onNavigateBack: () -> Unit,
    viewModel: RestaurantPersonalInfoViewModel = hiltViewModel()
) {
    val state = viewModel.uiState
    val context = LocalContext.current

    LaunchedEffect(key1 = state.isSuccess) {
        if (state.isSuccess) {
            Toast.makeText(context, "Cập nhật thông tin thành công!", Toast.LENGTH_SHORT).show()
            viewModel.resetSuccessState()
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            DFoodTopBar(title = "Restaurant Information", onBackClick = onNavigateBack)
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        if (state.isLoading && state.name.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.shapes.medium),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_add),
                            contentDescription = "Upload Ảnh",
                            tint = MaterialTheme.colorScheme.primary
                        )
                        TextButton(onClick = { /* Mở Gallery Picker */ }) {
                            Text("Thay đổi ảnh nhà hàng")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                SectionHeader("THÔNG TIN CHUNG")
                RestaurantInputField(
                    label = "Tên nhà hàng",
                    placeholder = "Ví dụ: King Burger",
                    value = state.name,
                    onValueChange = { viewModel.onEvent(RestaurantPersonalInfoEvent.NameChanged(it)) }
                )
                RestaurantInputField(
                    label = "Số điện thoại hotline",
                    placeholder = "090xxxxxxx",
                    value = state.phone,
                    onValueChange = { viewModel.onEvent(RestaurantPersonalInfoEvent.PhoneChanged(it)) }
                )

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                Spacer(modifier = Modifier.height(16.dp))

                SectionHeader("ĐỊA CHỈ KINH DOANH")
                RestaurantInputField(
                    label = "Số nhà, tên đường",
                    placeholder = "Ví dụ: 123 Đường số 1",
                    value = state.street,
                    onValueChange = { viewModel.onEvent(RestaurantPersonalInfoEvent.StreetChanged(it)) }
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    RestaurantInputField(
                        label = "Quận / Huyện",
                        placeholder = "Quận 1",
                        value = state.district,
                        onValueChange = { viewModel.onEvent(RestaurantPersonalInfoEvent.DistrictChanged(it)) },
                        modifier = Modifier.weight(1f)
                    )
                    RestaurantInputField(
                        label = "Thành phố",
                        placeholder = "TP HCM",
                        value = state.city,
                        onValueChange = { viewModel.onEvent(RestaurantPersonalInfoEvent.CityChanged(it)) },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = { viewModel.onEvent(RestaurantPersonalInfoEvent.Submit) },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !state.isLoading
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Text("Lưu thay đổi", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    }
                }

                if (state.error != null) {
                    Text(
                        text = state.error,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 12.dp).align(Alignment.CenterHorizontally)
                    )
                }

                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun RestaurantPersonalInfoPreview() {
    DFoodTheme {
        RestaurantPersonalInfoScreen(
            onNavigateBack = {}
        )
    }
}