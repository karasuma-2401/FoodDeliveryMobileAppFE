# Restaurant & Admin — Vendor Flows

## 1. Bài toán

Một APK phục vụ thêm:
- **Restaurant (BUSINESS):** quản lý đơn, thực đơn, doanh thu, coupon, chat khách
- **Admin:** duyệt NH, categories, coupons, orders toàn hệ thống

## 2. Role routing

Sau login, role `BUSINESS` → `RestaurantGraph`, `ADMIN` → `AdminGraph`.

Xem [auth-session.md](auth-session.md), [navigation.md](navigation.md).

## 3. Restaurant (BUSINESS) modules

### 3.1. Dashboard

| File | Nội dung |
|------|----------|
| `ui/screens/restaurant/dashboard/DashboardScreen.kt` | Tổng quan |
| `ui/screens/restaurant/component/dashboard/MessagesQuickAccessCard.kt` | Shortcut chat |
| `ui/screens/restaurant/component/dashboard/DashBoardReview.kt` | Reviews |

### 3.2. Order management

| File | Nội dung |
|------|----------|
| `ui/screens/restaurant/order/OrderManagementScreen.kt` | Accept/reject, đổi status |
| `ui/screens/restaurant/component/OrderCard.kt` | Card đơn |

**Status flow NH:** PENDING → PREPARING → DELIVERING → DELIVERED

API: `docs/OrderFlow.md` — NH dùng `PATCH /orders/:id`

Notification actions: `ACCEPT_ORDER`, `REJECT_ORDER` từ push.

### 3.3. Food management

| File | Nội dung |
|------|----------|
| `ui/screens/restaurant/food_management/MyFoodListScreen.kt` | List món |
| `ui/screens/restaurant/food_management/EditFoodScreen.kt` | Thêm/sửa |
| `ui/screens/restaurant/food_management/EditFoodViewModel.kt` | Upload ảnh, sizes, ingredients |

Upload ảnh: `domain/usecase/UploadImageUseCase.kt`

### 3.4. Coupon / Voucher

| File | Nội dung |
|------|----------|
| `ui/screens/restaurant/coupon/RestaurantCouponScreen.kt` | Quản lý coupon NH |
| `ui/screens/restaurant/component/coupon/ActiveRestaurantCoupons.kt` | Active list |

### 3.5. Revenue & Wallet

| File | Nội dung |
|------|----------|
| `ui/screens/restaurant/revenue/RestaurantRevenueScreen.kt` | Doanh thu |
| `ui/screens/restaurant/component/profile/BalanceHeader.kt` | Số dư ví |

### 3.6. Profile & Chat

| File | Nội dung |
|------|----------|
| `ui/screens/restaurant/profile/RestaurantProfileScreen.kt` | Settings NH |
| `ui/screens/chat/ConversationScreen.kt` | Shared với customer — role BUSINESS thấy customer name |

`ChatViewModel.initChatFromOrder` — BUSINESS **không** auto `createConversation`, chỉ sync by order.

### 3.7. Bottom bar

`ui/screens/restaurant/component/DFoodBottomBar.kt` — tabs riêng NH + unread chat badge.

## 4. Admin modules

| Module | Screen | ViewModel |
|--------|--------|-----------|
| Dashboard | `AdminDashboardScreen` | — |
| Restaurants | `AdminRestaurantScreen` | `AdminRestaurantViewModel` |
| Categories | `AdminCategoryScreen`, `CreateCategoryScreen` | `CategoryViewModel` |
| Coupons | `AdminCouponScreen`, `CreateCouponScreen` | `CreateCouponViewModel` |
| Orders | `AdminOrderScreen` | — |
| Revenue | `AdminRevenueScreen` | — |
| Users | `UserListScreen` | — |
| Settings | `AdminSettingScreen` | — |

Doc: `docs/ADMIN_DASHBOARD.md`

Bottom bar: `ui/screens/admin/components/AdminBottomBar.kt`

## 5. Shared patterns với Customer

| Pattern | Restaurant/Admin usage |
|---------|------------------------|
| Chat | Cùng `ChatRepositoryImpl`, khác `resolveOtherUser` display name |
| Notification | SYSTEM + restaurant approval destination |
| Upload image | Food management, chat image |
| Order status | PATCH thay vì chỉ read |

## 6. Demo scenarios (Restaurant)

```
1. Nhận push đơn PENDING → OrderManagement → Accept → PREPARING
2. Sửa món → upload ảnh → list refresh
3. Chat với khách từ order card
```

## 7. Demo scenarios (Admin)

```
1. Duyệt restaurant pending → notification SYSTEM
2. Tạo category global → hiện trên customer home
```

## 8. Nếu làm lại từ đầu

1. Tách `RestaurantGraph` + `AdminGraph` trong NavGraph
2. Guard API bằng role (BE) — FE route theo role
3. Reuse components (OrderCard, Chat) với props khác
4. Dashboard gọi API stats riêng role

## 9. Tham chiếu

- [chat.md](chat.md)
- [notification.md](notification.md)
- [order-tracking.md](order-tracking.md)
- [docs/ADMIN_DASHBOARD.md](../ADMIN_DASHBOARD.md)
