# Glossary — Thuật ngữ DFood

Bảng thuật ngữ dùng trong code, API và docs — tránh nhầm lẫn.

---

## Roles

| Thuật ngữ | Ý nghĩa | Trong code |
|-----------|---------|------------|
| **CUSTOMER** | Khách đặt đồ ăn | `roles` từ JWT / `MeResponse` |
| **BUSINESS** | Chủ nhà hàng / seller | Route tới `RestaurantGraph` |
| **ADMIN** | Quản trị hệ thống | Route tới `AdminGraph` |

---

## Chat

| Thuật ngữ | Ý nghĩa | Ghi chú |
|-----------|---------|---------|
| **Conversation** | Phòng chat 1 customer ↔ 1 seller | Unique theo cặp `(customerId, sellerId)` |
| **seller** | User owner của restaurant | Không phải shipper |
| **customer** | User đặt hàng | Role CUSTOMER |
| **join-room** | Socket event tham gia room | Room id: `room-{conversationId}` |
| **text-chat** | Socket event gửi/nhận tin | Có thể kèm `image` URL |
| **Conservation** | Typo trong codebase | `ConservationViewModel` = Conversation list |

---

## Order

| Thuật ngữ | Ý nghĩa | Ghi chú |
|-----------|---------|---------|
| **PENDING** | Đơn mới, chờ NH | |
| **PREPARING** | NH đang làm món | |
| **DELIVERING** | Đang giao | ETA bắt đầu có giá trị |
| **DELIVERED** | NH xác nhận đã giao | Khách có thể confirm received |
| **CONFIRMED** | Khách đã nhận hàng | **Không** = đã thanh toán |
| **CANCELLED** | Đã hủy | |
| **paymentStatus** | Trạng thái thanh toán MoMo/Cash | Tách biệt order status |
| **deliveryMinutes** | Số phút giao (snapshot lúc tạo đơn) | BE tính từ khoảng cách |
| **expected_arrival** | ISO timestamp ETA | Chỉ khi DELIVERING |
| **clearCartAfterOrder** | Flag xóa giỏ sau order | FE gửi `false` — chỉ xóa món cùng NH |

---

## Cart

| Thuật ngữ | Ý nghĩa |
|-----------|---------|
| **restaurantGroups** | Nhóm món theo NH trong cart |
| **lineTotal** | Tổng dòng (sau size/topping) |
| **foodSizeId** | ID size món (S/M/L…) |
| **optimistic snapshot** | State trước khi add — dùng rollback |

---

## Notification

| Type | Ý nghĩa | Navigate |
|------|---------|----------|
| **ORDER** | Liên quan đơn hàng | Track order |
| **PAYMENT** | Thanh toán | Track order |
| **CHAT** | Tin nhắn mới | Chat screen |
| **PROMOTION** | Khuyến mãi | Tuỳ implement |
| **SYSTEM** | Hệ thống | Có thể restaurant approval |
| **ACCEPT_ORDER** | Action cho NH | Order management |
| **CONFIRM_RECEIVED** | Nhắc khách xác nhận | Track order |

---

## Payment

| Thuật ngữ | Ý nghĩa |
|-----------|---------|
| **MoMo deeplink** | URL mở app MoMo (không phải http) |
| **momoOrderId** | Format `MOMO-ORDER-XXX` |
| **checkPayment** | API xác nhận IPN / manual check |
| **CASH** | Thanh toán tiền mặt khi nhận |

---

## Technical

| Thuật ngữ | Ý nghĩa |
|-----------|---------|
| **UiEffect** | One-shot event (navigate, snackbar) |
| **unwrapData** | Parse `{ success, data }` wrapper BE |
| **TokenManager** | DataStore lưu JWT + user info |
| **DeliveryLocationState** | Địa chỉ đang chọn + list addresses |
| **StateFlow** | Hot flow cho UI state |
| **collectLatest** | Cancel collect cũ khi có emit mới |

---

## File naming quirks

| Tên | Thực tế |
|-----|---------|
| `VertificationViewModel` | Verification (typo) |
| `conservation.md` | Conversation API notes |
| `services/ChatSocketService` | Android Service (s) |
| `service/DFoodMessagingService` | FCM Service (no s) |
