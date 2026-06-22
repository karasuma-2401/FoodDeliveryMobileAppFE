# API Documentation: Đánh giá Nhà hàng (Rate & Review)

API này cho phép người dùng gửi đánh giá, số sao và nhận xét cho một nhà hàng sau khi hoàn thành đơn hàng.

## 1. Thông tin chung
- **Endpoint:** `POST /api/restaurant/{id}/ratings`
- **Content-Type:** `application/json`
- **Authentication:** `Bearer Token` (Yêu cầu đăng nhập)

## 2. Tham số đường dẫn (Path Parameters)
| Tham số | Kiểu dữ liệu | Mô tả |
| :--- | :--- | :--- |
| `id` | `Integer` | ID của nhà hàng cần đánh giá. |

## 3. Cấu trúc Request Body
Định dạng JSON gửi lên:

| Trường | Kiểu dữ liệu | Bắt buộc | Mô tả |
| :--- | :--- | :--- | :--- |
| `restaurantId` | `Integer` | Có | ID của nhà hàng (thường khớp với ID trên đường dẫn). |
| `orderId` | `Integer` | Có | ID của đơn hàng liên quan đến đánh giá này. |
| `vote` | `Integer` | Có | Số sao đánh giá (thường từ 1 đến 5). |
| `comment` | `String` | Không | Nội dung nhận xét chi tiết của người dùng. |
| `tags` | `Array[String]` | Không | Danh sách các nhãn đánh giá nhanh (VD: "Giao hàng nhanh", "Món ăn ngon"). |

**Ví dụ Request:**

```json
{
  "restaurantId": 101,
  "orderId": 162432,
  "vote": 5,
  "comment": "Đồ ăn rất ngon, đóng gói cẩn thận. Sẽ ủng hộ lần sau!",
  "tags": ["Món ăn ngon", "Đóng gói cẩn thận"]
}
```

## 4. Cấu trúc Response
### 4.1. Thành công (200 OK hoặc 201 Created)
| Trường | Kiểu dữ liệu | Mô tả |
| :--- | :--- | :--- |
| `message` | `String` | Thông báo xác nhận gửi đánh giá thành công. |

**Ví dụ Response:**

```json
{
  "message": "Đánh giá đã được gửi thành công!"
}
```

### 4.2. Lỗi thường gặp
- **400 Bad Request:** Dữ liệu gửi lên không hợp lệ (số sao ngoài khoảng 1-5, thiếu ID đơn hàng...).
- **401 Unauthorized:** Token hết hạn hoặc không có quyền truy cập.
- **404 Not Found:** Không tìm thấy nhà hàng hoặc đơn hàng tương ứng.
- **500 Internal Server Error:** Lỗi hệ thống phía backend.

## 5. Danh sách Tags gợi ý
- `Món ăn ngon`
- `Giao hàng nhanh`
- `Đóng gói cẩn thận`
- `Giá cả hợp lý`
- `Phục vụ tốt`
