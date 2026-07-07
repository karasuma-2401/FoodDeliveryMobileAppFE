# Troubleshooting Playbook

Hướng dẫn debug nhanh: **triệu chứng → nguyên nhân → file kiểm tra**.

---

## Auth & Session

| Triệu chứng | Nguyên nhân có thể | Kiểm tra |
|-------------|-------------------|----------|
| App luôn về Login sau mở | Token cleared / refresh fail | `ValidateSessionUseCase`, `TokenManager` |
| Login OK nhưng vào sai màn | Role mapping sai | `toStartDestination()`, `saveMeInfo` roles |
| Google/Facebook login fail | Thiếu keys | `local.properties`, `google-services.json` |

---

## Cart & Checkout

| Triệu chứng | Nguyên nhân có thể | Kiểm tra |
|-------------|-------------------|----------|
| Thêm món không hiện | API fail sau optimistic rollback | `CartRepositoryImpl`, Logcat network |
| Cart sai số lượng | Server sync chưa chạy | `applyServerCart`, gọi `syncCart()` |
| Checkout báo cart empty | Filter sai `restaurantId` | `CheckoutViewModel.handlePlaceOrder` |
| Phí ship không hiện | Address thiếu lat/lng | `loadDeliveryFee`, `address.latitude` |
| MoMo quay lại không success | Poll payment fail | `confirmMoMoPayment`, `pollPaymentStatus` |
| Voucher không apply | `minOrderAmount` hoặc không suitable | `loadSuitableVouchers`, `handleApplyPromoCode` |

---

## Order Tracking

| Triệu chứng | Nguyên nhân có thể | Kiểm tra |
|-------------|-------------------|----------|
| ETA không hiện khi đang giao | Status chưa DELIVERING hoặc thiếu `expected_arrival` | BE response, `buildEtaState` |
| ETA hiện khi PREPARING | FE logic cũ | Chỉ show khi `backendStatus == DELIVERING` |
| Timeline không cập nhật | Poll stopped | `startPolling`, terminal state check |
| Confirm received fail | Order chưa DELIVERED | `TrackOrderViewModel.confirmReceived` |
| Double confirm error | Expected — handle `already confirmed` | `isAlreadyConfirmedError()` |

---

## Chat

| Triệu chứng | Nguyên nhân có thể | Kiểm tra |
|-------------|-------------------|----------|
| Tin gửi treo "sending" | Socket disconnect / timeout 15s | `ChatSocketManager.isConnected()`, `scheduleSendTimeout` |
| Tin gửi failed | Socket exception | Log `ChatRepository` exception handler |
| Không nhận tin mới | Chưa join room | `emitJoinRoom`, `activeConversationId` |
| Tin duplicate | Optimistic + server cùng lúc | `deleteOptimisticDuplicates` |
| Mở chat từ order lỗi (customer) | Chưa có conversation | `createConversation(sellerId)` path |
| Mở chat từ order lỗi (NH) | Conversation chưa tồn tại | Customer phải chat trước hoặc BE tạo |
| Online luôn hiện | Hardcoded | `ChatState.isOnline = true` — known limitation |

---

## Notification

| Triệu chứng | Nguyên nhân có thể | Kiểm tra |
|-------------|-------------------|----------|
| Không nhận push | FCM token chưa register | `RegisterDeviceTokenUseCase`, notification setting |
| Push tắt trong app | `readNotificationsState` false | `ProfileViewModel`, DataStore |
| Badge sai | Room chưa sync | `UnreadNotificationViewModel.refresh()` |
| Tap push không vào đúng màn | Chưa implement deep link | `DFoodMessagingService` — intent chỉ mở MainActivity |
| List trống offline | API fail + Room empty | `getNotificationsPaged` fallback |

---

## Search & Location

| Triệu chứng | Nguyên nhân có thể | Kiểm tra |
|-------------|-------------------|----------|
| Kết quả search không đổi khi đổi địa chỉ | Observer chưa chạy | `SearchViewModel` `selectedAddressId` collect |
| Search gọi API quá nhiều | Thiếu debounce | `delay(500L)` trong `QueryChanged` |
| NH Home không sort theo khoảng cách | Thiếu lat/lng | `DeliveryLocationRepository`, GPS permission |

---

## Network & Config

| Triệu chứng | Kiểm tra |
|-------------|----------|
| Mọi API fail | `API_BASE_URL` trong `local.properties` / Azure BE |
| Socket không connect | `SOCKET_URL`, JWT trong `ChatSocketManager` |
| 401 hàng loạt | Token expired — `ValidateSessionUseCase` |

### Config mặc định

```
API: https://food-deliver-be-...azurewebsites.net/api/
SOCKET: cùng host không có /api
```

Local BE: `API_BASE_URL=http://10.0.2.2:4000/api/`, `SOCKET_URL=http://10.0.2.2:4000`

---

## CI/CD & Release

| Triệu chứng | Nguyên nhân có thể | Kiểm tra |
|-------------|-------------------|----------|
| CI fail thiếu `google-services.json` | Chưa copy placeholder CI | Step `cp google-services.json.ci` trong `ci.yml` |
| Release fail decode keystore | `KEYSTORE_BASE64` sai | Re-encode file `.jks`, check Environment `production` |
| `Invalid version format` | Tag không semver 3 phần | Dùng `v1.0.0`, không `v1.0` |
| Release Please không tạo PR | Commit không conventional | Prefix `feat:`, `fix:` |
| APK không cài đè được | `versionCode` thấp | CI dùng `github.run_number`; local tăng `VERSION_CODE` |
| Login social fail trên release APK | SHA-1 release chưa đăng ký | `keytool -list -v -keystore` → Firebase/Facebook console |
| Crash release khó đọc | Cần ProGuard mapping | Tải `mapping.txt` từ GitHub Release |

Chi tiết setup: [learning/cicd-release.md](learning/cicd-release.md)

---

## Log tags hữu ích

| Tag | Module |
|-----|--------|
| `DFoodMessagingService` | FCM |
| `ChatSocketService` | Socket background |
| `ChatRepository` | Chat send/receive |
| `ChatSocketManager` | Connection |

---

## Khi vẫn không fix được

1. Đọc learning doc tính năng tương ứng trong `docs/learning/`
2. Đọc API doc BE trong `docs/`
3. Kiểm tra Swagger BE: `GET /api/docs`
4. Reproduce với demo scenario trong `docs/journeys/`
