
Tài liệu này mô tả các API cần thiết để thực hiện tính năng "Yêu thích nhà hàng" cho người dùng trong ứng dụng FoodDelivery.

## 1. Tổng quan
- **Base URL**: `https://api.fooddelivery.com/api` (hoặc cấu hình theo môi trường)
- **Authentication**: Yêu cầu `Authorization: Bearer <token>` trong header.
- **Format**: `application/json`

---

## 2. Các Endpoint

### 2.1. Toggle Yêu thích/Bỏ yêu thích Nhà hàng
Sử dụng để thêm hoặc xóa nhà hàng khỏi danh sách yêu thích của người dùng (Action đảo ngược trạng thái hiện tại).

- **URL**: `/restaurants/{id}/like`
- **Method**: `POST`
- **Path Parameters**:
  - `id` (Long/Int): ID của nhà hàng.

- **Response (200 OK)**:
```json
{
  "success": true,
  "message": "Update favorite status successfully",
  "data": {
    "restaurantId": 123,
    "isLiked": true,
    "totalLikes": 1502
  }
}
```

- **Error Responses**:
  - `401 Unauthorized`: Token không hợp lệ hoặc hết hạn.
  - `404 Not Found`: Không tìm thấy nhà hàng với ID tương ứng.
  - `500 Internal Server Error`: Lỗi hệ thống.

---

### 2.2. Lấy danh sách Nhà hàng đã yêu thích
Lấy danh sách các nhà hàng mà người dùng hiện tại đã nhấn "Like".

- **URL**: `/user/favorites/restaurants`
- **Method**: `GET`
- **Query Parameters**:
  - `limit` (Int, Optional): Số lượng bản ghi mỗi trang (Mặc định: 20).
  - `offset` (Int, Optional): Vị trí bắt đầu lấy (Mặc định: 0).

- **Response (200 OK)**:
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "name": "Rose Garden Restaurant",
      "image": "https://cdn.example.com/images/res1.jpg",
      "rating": 4.7,
      "deliveryFee": 0.0,
      "tags": ["Burger", "Chicken"],
      "isLiked": true
    },
    {
      "id": 2,
      "name": "KFC - Ho Chi Minh",
      "image": "https://cdn.example.com/images/kfc.jpg",
      "rating": 4.5,
      "deliveryFee": 1.5,
      "tags": ["Fast Food"],
      "isLiked": true
    }
  ],
  "pagination": {
    "total": 45,
    "limit": 20,
    "offset": 0
  }
}
```

---

### 2.3. Kiểm tra trạng thái yêu thích (Dành cho trang chi tiết)
Kiểm tra xem một nhà hàng cụ thể có đang được người dùng hiện tại yêu thích hay không.

- **URL**: `/restaurants/{id}/like-status`
- **Method**: `GET`
- **Path Parameters**:
  - `id` (Long/Int): ID của nhà hàng.

- **Response (200 OK)**:
```json
{
  "success": true,
  "data": {
    "isLiked": true
  }
}
```

---

## 3. Cấu trúc dữ liệu chung (DTO)

### BaseResponse
Mọi API nên bọc trong một cấu trúc chung:
| Trường | Kiểu dữ liệu | Mô tả |
| :--- | :--- | :--- |
| `success` | Boolean | Trạng thái thành công của request |
| `message` | String | Thông báo đi kèm (thường dùng khi lỗi) |
| `data` | Object/Array | Dữ liệu trả về chính |

### RestaurantDTO (trong danh sách yêu thích)
| Trường | Kiểu dữ liệu | Mô tả |
| :--- | :--- | :--- |
| `id` | Int | Mã định danh nhà hàng |
| `name` | String | Tên nhà hàng |
| `image` | String | URL ảnh đại diện |
| `rating` | Float | Điểm đánh giá trung bình |
| `isLiked` | Boolean | Luôn là `true` trong list favorites |

---

## 4. Ghi chú cho Backend
1. **DB Schema**: Cần một bảng trung gian `user_favorite_restaurants` (user_id, restaurant_id) với Unique Constraint trên cả 2 trường.
2. **Performance**: Khi lấy danh sách nhà hàng (All Restaurants), cần `LEFT JOIN` với bảng favorites để trả về đúng field `isLiked` theo `user_id` đang đăng nhập.
3. **CORS**: Đảm bảo cấu hình cho phép các domain từ Frontend (Mobile/Web).
