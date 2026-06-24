# Tài liệu API cho màn hình Home (Customer) - Không bao gồm Banner

Tài liệu này tổng hợp các API cần thiết để vận hành màn hình chính (Home) phía khách hàng. Các yêu cầu về Banner và Logic lọc giờ mở cửa đã được loại bỏ để phù hợp với cấu trúc Database hiện tại.

---

## 1. API Thông tin người dùng (User Profile)
Dùng để hiển thị lời chào và tên người dùng (ví dụ: "Hey [Name], Good Morning!").

- **Endpoint:** `GET /api/user/profile`
- **Authentication:** Bắt buộc (Bearer Token)
- **Response Format:**
```json
{
  "success": true,
  "data": {
    "id": "string",
    "fullName": "string",
    "avatarUrl": "string"
  }
}
```

---

## 2. API Danh mục (Categories)
Lấy danh sách các danh mục món ăn hiển thị ở thanh cuộn ngang.

- **Endpoint:** `GET /api/categories`
- **Authentication:** Public
- **Query Parameters:**
  - `limit` (number, optional): Giới hạn số lượng (mặc định 10).
- **Response Format:**
```json
{
  "success": true,
  "data": [
    {
      "id": "string",
      "name": "string",
      "imageUrl": "string"
    }
  ]
}
```

---

## 3. API Nhà hàng (Restaurants)
Lấy danh sách tất cả nhà hàng, sắp xếp theo khoảng cách nếu có tọa độ người dùng.

- **Endpoint:** `GET /api/restaurants`
- **Authentication:** Public
- **Query Parameters:**
  - `lat` (number, optional): Vĩ độ của user.
  - `lng` (number, optional): Kinh độ của user.
  - `limit` (number, optional): Số lượng bản ghi (mặc định 20).
- **Response Format:**
```json
{
  "success": true,
  "data": [
    {
      "id": "string",
      "name": "string",
      "imageUrl": "string",
      "averageRating": 4.8,
      "deliveryFee": 1.5,
      "distance": 2.3, // đơn vị: km
      "tags": ["Fast Food", "Burger"],
      "estimatedDeliveryTime": 25 // đơn vị: phút
    }
  ]
}
```

---

## 4. API Bộ đếm (Home Counters)
Lấy số lượng item trong giỏ hàng và tin nhắn chưa đọc cho TopBar.

- **Endpoint:** `GET /api/home/counters`
- **Authentication:** Bắt buộc (Bearer Token)
- **Response Format:**
```json
{
  "success": true,
  "data": {
    "cartItemCount": 5,
    "unreadMessageCount": 2
  }
}
```

---

## 5. [Khuyên dùng] API Dashboard Hợp nhất
Gộp các thông tin trên vào 1 request để tối ưu tốc độ tải trang khi mở app.

- **Endpoint:** `GET /api/home/dashboard`
- **Authentication:** Optional (Nếu có token trả về thêm User & Counters).
- **Query Parameters:** `lat`, `lng`.
- **Response Format:** Trả về object chứa cả `user`, `categories`, `restaurants`, `counters`.

---

## Yêu cầu kỹ thuật đối với Backend:
1. **Tính toán khoảng cách:** Nếu Client gửi `lat/lng`, BE tính toán khoảng cách thực tế giữa User và Nhà hàng để trả về trường `distance`.
2. **Xử lý khi chưa đăng nhập:** API Dashboard phải hoạt động được khi không có Token (trả về null cho phần thông tin cá nhân).
3. **Đơn giản hóa logic:** Do DB không lưu giờ hoạt động, API sẽ trả về toàn bộ nhà hàng mà không cần lọc trạng thái Đóng/Mở cửa.
