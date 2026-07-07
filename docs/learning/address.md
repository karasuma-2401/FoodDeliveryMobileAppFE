# Address — CRUD, Map Picker & Places Search

## 1. Bài toán

- Khách quản lý nhiều địa chỉ giao hàng
- Thêm/sửa trên bản đồ + tìm kiếm địa điểm
- Đồng bộ với `DeliveryLocationRepository` khi chọn địa chỉ mặc định

## 2. Walkthrough source

| File | Vai trò |
|------|---------|
| `ui/screens/address/CustomerAddressScreen.kt` | Danh sách địa chỉ |
| `ui/screens/address/AddAddressScreen.kt` | Form + map |
| `ui/screens/address/AddAddressViewModel.kt` | Logic map, search places |
| `domain/usecase/GetAddressesUseCase.kt` | List |
| `domain/usecase/AddAddressUseCase.kt` | Create |
| `domain/usecase/UpdateAddressUseCase.kt` | Update |
| `domain/usecase/DeleteAddressUseCase.kt` | Delete |
| `domain/usecase/SearchPlacesUseCase.kt` | Photon geocoding |
| `data/repository/AddressRepositoryImpl.kt` | API layer |
| `data/remote/api/PhotonService.kt` | OpenStreetMap Photon API |

## 3. Integration với Delivery Location

Sau khi add/edit/delete address:
- Gọi `deliveryLocationRepository.refreshAddresses()`
- Checkout/Home tự cập nhật qua observe

Chi tiết: [delivery-location.md](delivery-location.md)

## 4. Map picker flow

`AddAddressViewModel` thường handle:
- Pin trên map → lat/lng
- Reverse geocode / search qua `SearchPlacesUseCase`
- Label: Home, Work, Other + `deliveryNote`

Components: `ui/screens/address/components/AddressFormComponents.kt`, `CustomAddressTextField.kt`

## 5. API

Xem `docs/address.md`

## 6. Test

`AddressResponseMappingTest.kt` — đảm bảo DTO snake_case map đúng domain.

## 7. Demo scenarios

```
1. Thêm địa chỉ mới → xuất hiện trong list + DeliveryLocation refresh
2. Chọn địa chỉ trên Checkout → selectById → phí ship tính lại
3. Xóa địa chỉ đang chọn → fallback địa chỉ đầu tiên
```

## 8. Nếu làm lại từ đầu

1. `Address` domain model + CRUD API
2. `DeliveryLocationRepository` (singleton selected id)
3. List screen + Add/Edit screen với Google Map / OSM
4. `SearchPlacesUseCase` cho autocomplete
5. Wire vào Checkout `loadDeliveryFee`

## 9. Tham chiếu

- [delivery-location.md](delivery-location.md)
- [checkout-payment.md](checkout-payment.md)
