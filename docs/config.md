# API Changes — Order ETA (Track Order)

Tài liệu này mô tả thay đổi BE liên quan **thời gian giao dự kiến (ETA)** cho màn **track order**. Các API khác (home, checkout, list nhà hàng) **không đổi**.

---

## Tóm tắt hành vi mới

| Giai đoạn | `expected_arrival` |
|---|---|
| `PENDING` / `PREPARING` | `null` — không countdown giao hàng |
| `DELIVERING` | ISO timestamp — `delivering_at + delivery_minutes` |
| `DELIVERED` / `CONFIRMED` / `CANCELLED` | `null` — dùng `delivered_at` nếu đã giao |

**Mốc bắt đầu giao:** khi nhà hàng chuyển status sang `DELIVERING` (BE ghi `deliveringAt`).

**Số phút giao:** tính **một lần lúc tạo đơn** từ khoảng cách NH ↔ địa chỉ giao, lưu `deliveryMinutes`.

### Công thức (BE)

```
deliveryMinutes = clamp(10, round(5 + distanceKm × 4), 45)
expected_arrival  = deliveringAt + deliveryMinutes   // chỉ khi status = DELIVERING
```

---

## Endpoints bị ảnh hưởng

### 1. `GET /api/orders/:orderId` — Chi tiết đơn (màn track)

**Thay đổi response:**

| Field | Type | Mô tả |
|---|---|---|
| `expected_arrival` | `string \| null` | **Đổi logic:** chỉ có giá trị khi `status === "DELIVERING"` |
| `delivering_at` | `string \| null` | **Mới** — thời điểm NH chuyển sang giao (ISO) |
| `delivery_minutes` | `number \| null` | **Mới** — số phút giao ước tính (snapshot lúc đặt) |

**Trước:** `expected_arrival` luôn có, tính từ `payment.createdAt + restaurant.estimatedDeliveryTime`.

**Sau:** ETA gắn với status `DELIVERING`, không dùng config nhà hàng.

**Ví dụ — `PREPARING`:**

```json
{
  "status": "PREPARING",
  "status_step": 1,
  "backend_status": "PREPARING",
  "delivery_minutes": 17,
  "delivering_at": null,
  "expected_arrival": null,
  "delivered_at": null
}
```

**Ví dụ — `DELIVERING`:**

```json
{
  "status": "DELIVERING",
  "status_step": 2,
  "backend_status": "DELIVERING",
  "delivery_minutes": 17,
  "delivering_at": "2026-06-27T10:00:00.000Z",
  "expected_arrival": "2026-06-27T10:17:00.000Z",
  "delivered_at": null
}
```

**Ví dụ — `DELIVERED`:**

```json
{
  "status": "DELIVERED",
  "status_step": 3,
  "delivery_minutes": 17,
  "delivering_at": "2026-06-27T10:00:00.000Z",
  "expected_arrival": null,
  "delivered_at": "2026-06-27T10:15:00.000Z"
}
```

---

### 2. `GET /api/orders/:orderId/status` — Poll trạng thái

**Thêm field** (cùng semantics với detail):

| Field | Type |
|---|---|
| `expected_arrival` | `string \| null` |
| `delivering_at` | `string \| null` |
| `delivery_minutes` | `number \| null` |

**`updated_at`:** ưu tiên timestamp theo status (`delivering_at` → `delivered_at` → `confirmed_at` → payment), thay vì chỉ `payment.updatedAt`.

**Ví dụ poll khi đang giao:**

```json
{
  "order_id": 100,
  "status": "DELIVERING",
  "status_step": 2,
  "backend_status": "DELIVERING",
  "updated_at": "2026-06-27T10:00:00.000Z",
  "delivery_minutes": 17,
  "delivering_at": "2026-06-27T10:00:00.000Z",
  "expected_arrival": "2026-06-27T10:17:00.000Z",
  "delivered_at": null,
  "confirmed_at": null,
  "confirmed_by": null,
  "auto_confirm_at": null,
  "hours_until_auto_confirm": null
}
```

---

### 3. `PATCH /api/orders/:orderId` — Cập nhật status (NH)

**Không đổi contract request/response URL.**

**Side effect mới:** khi body `{ "status": "DELIVERING" }`, BE ghi `deliveringAt = now()` trên order.

→ Sau khi NH bấm “Bắt đầu giao”, FE track order poll/detail sẽ nhận `expected_arrival`.

---

### 4. `POST /api/orders` — Tạo đơn

**Không đổi response contract** cho FE (không bắt buộc đọc field mới).

**Side effect:** BE lưu `deliveryMinutes` trong DB (tính từ km lúc tạo đơn).

---

## Hướng dẫn chỉnh FE (màn track order)

### Logic hiển thị ETA

```ts
if (order.status === 'DELIVERING' && order.expected_arrival) {
  // Hiển thị countdown / "Dự kiến giao lúc ..."
  showEtaCountdown(order.expected_arrival);
} else if (order.status === 'DELIVERED' && order.delivered_at) {
  showDeliveredAt(order.delivered_at);
} else {
  // PENDING / PREPARING: chỉ text trạng thái, KHÔNG countdown
  showStatusOnly(order.status);
}
```

### Không nên làm

- Countdown từ `expected_arrival` khi `PREPARING` / `PENDING`
- Tự tính `now + restaurant.estimatedDeliveryTime`
- Map field `expectedArrivalTime` (camelCase) — BE trả **`expected_arrival`** (snake_case)

### Poll

- Tiếp tục poll `GET /orders/:orderId` hoặc `GET /orders/:orderId/status` khi đơn ongoing
- Khi NH chuyển `DELIVERING`, response poll sẽ có `expected_arrival` — bật countdown

### Ai chuyển status

| Status | Actor |
|---|---|
| `PREPARING` | NH (accept đơn) |
| `DELIVERING` | **NH** (bắt đầu giao) — không phải app khách |
| `DELIVERED` | NH |

---

## Đơn cũ (trước migration)

- `deliveryMinutes` / `deliveringAt` có thể `null`
- Nếu đang `DELIVERING` nhưng thiếu snapshot: BE cố tính `delivery_minutes` từ tọa độ (detail); `expected_arrival` chỉ có khi đủ `delivering_at` + `delivery_minutes`

---

## Không thay đổi trong scope này

- `GET /api/orders` (list ongoing/history) — không thêm ETA
- `GET /api/orders/deliveryFee/:restaurantId` — không thêm quote phút
- `restaurant.estimatedDeliveryTime` — vẫn tồn tại, **không dùng** cho ETA đơn
- Home / checkout UI — không cần đổi vì ETA chỉ dùng màn track

---

## Database (tham khảo)

Bảng `Order` thêm:

| Column | Type | Ghi chú |
|---|---|---|
| `deliveryMinutes` | `Int?` | Snapshot lúc `createOrder` |
| `deliveringAt` | `DateTime?` | Set khi → `DELIVERING` |

Migration: `20260627120000_add_order_delivery_eta_fields`

---

## Checklist FE

- [ ] Track order: chỉ show ETA khi `status === 'DELIVERING'`
- [ ] Đọc `expected_arrival`, `delivering_at`, `delivery_minutes` (snake_case)
- [ ] Bỏ logic ETA từ payment time / `estimatedDeliveryTime`
- [ ] Poll status/detail sau khi NH chuyển `DELIVERING`
- [ ] `DELIVERED`: hiển thị `delivered_at`, tắt countdown
