# API Documentation & Requirements for Search Module (Customer)

Tài liệu này tổng hợp các API hiện có và các yêu cầu bổ sung/chỉnh sửa cần thiết từ phía Backend để hoàn thiện tính năng Search trên ứng dụng FoodDelivery.

---

## 1. Unified Search (Tìm kiếm hợp nhất)
Dùng khi User bắt đầu nhập từ khóa và nhấn tìm kiếm hoặc sau khi debounce.

- **Endpoint:** `GET /api/search`
- **Authentication:** Public
- **Query Parameters:**
  - `q` (string, required): Từ khóa tìm kiếm (tên món, tên nhà hàng, tags).
  - `lat` (number, optional): Vĩ độ hiện tại của User.
  - `lng` (number, optional): Kinh độ hiện tại của User.
  - `limit` (number, default: 20): Số lượng bản ghi mỗi loại.
  - **[Bổ sung]** `page` hoặc `offset` (number): Để hỗ trợ phân trang (Infinite Scroll).
  - **[Bổ sung]** `sort` (string): `distance`, `rating`, `price_low_to_high`.
  - **[Bổ sung]** `categoryId` (int): Lọc theo danh mục món ăn.

- **Response Requirements:**
  - `foods`: Mỗi object món ăn cần có `restaurantName`, `restaurantId` và `promoTag` (ví dụ: "Giảm 20%").
  - `restaurants`: Mỗi object nhà hàng cần có `distance` (nếu có lat/lng), `tags` (mảng string), và `averageRating`.

---

## 2. Search Suggestions (Gợi ý mặc định)
Dùng để hiển thị dữ liệu "Suggested Restaurants" và "Popular Fast Food" khi thanh search còn trống.

- **Endpoint:** `GET /api/search/suggestions`
- **Authentication:** Public
- **Query Parameters:**
  - **[Bổ sung]** `lat`, `lng` (number): Để gợi ý các nhà hàng ở gần User thay vì gợi ý toàn hệ thống.
- **Business Logic:**
  - Foods: Sắp xếp theo `soldCount` giảm dần (DELIVERED status).
  - Restaurants: Có Voucher đang active HOẶC rating >= 4.5.

---

## 3. Search History (Lịch sử tìm kiếm)
Quản lý các từ khóa User đã tìm kiếm.

### 3.1. Get History
- **Endpoint:** `GET /api/search/history`
- **Authentication:** Required (JWT)
- **Response:** Danh sách `{ id, keyword, createdAt }` sắp xếp mới nhất lên đầu.

### 3.2. Save History
- **Endpoint:** `POST /api/search/history`
- **Authentication:** Required (JWT)
- **Body:** `{ "keyword": string }`
- **Logic:** Upsert (nếu trùng thì cập nhật `createdAt` mới nhất).

### 3.3. Clear All History
- **Endpoint:** `DELETE /api/search/history`
- **Authentication:** Required (JWT)
- **Logic:** Xóa toàn bộ lịch sử của User hiện tại.

### 3.4. [Bổ sung] Delete Single Item
- **Endpoint:** `DELETE /api/search/history/{id}`
- **Authentication:** Required (JWT)
- **Logic:** Xóa một từ khóa cụ thể khi User nhấn nút "X" trên UI.

---

## 4. [Đề xuất thêm] Trending Keywords
- **Endpoint:** `GET /api/search/trending`
- **Description:** Trả về các từ khóa được tìm kiếm nhiều nhất hệ thống trong 7 ngày qua. Giúp User dễ dàng chọn món khi không biết ăn gì.

---

## 5. Yêu cầu chung về Dữ liệu (DTO)

Để UI hiển thị đồng nhất, BE cần đảm bảo Schema trả về chứa các thông tin sau:

### Food Object:
```json
{
  "id": "string/int",
  "name": "string",
  "price": 100.0,
  "imageUrl": "string",
  "restaurantId": "string",
  "restaurantName": "string",
  "rating": 4.5,
  "soldCount": 100,
  "promoTag": "string (null nếu không có)"
}
```

### Restaurant Object:
```json
{
  "id": "string/int",
  "name": "string",
  "imageUrl": "string",
  "averageRating": 4.8,
  "deliveryFee": 1.5,
  "distance": 2.3, // km
  "tags": ["Burger", "Fast Food"],
  "hasVoucher": true
}
```
