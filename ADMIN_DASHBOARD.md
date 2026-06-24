# Admin Dashboard API Documentation

Tài liệu này mô tả chi tiết các yêu cầu API cho từng màn hình của phân hệ Admin, được tổng hợp từ logic xử lý và các interface API trong ứng dụng Mobile.

---

## 1. Thông tin chung
- **Base URL**: `{BASE_URL}/api/`
- **Authentication**: Bearer Token (Yêu cầu quyền Admin)
- **Format**: JSON

---

## 2. Màn hình: Dashboard (Tổng quan)
Hiển thị các chỉ số kinh doanh chính và thống kê nhanh toàn hệ thống.

- **Endpoint**: `GET /api/admin/dashboard`
- **Auth required**: Yes (Admin Role)
- **Response**: `BaseResponse<AdminDashboardResponse>`

| Trường | Kiểu dữ liệu | Mô tả |
| :--- | :--- | :--- |
| `users` | Int | Tổng số người dùng |
| `restaurants` | Int | Tổng số nhà hàng |
| `orders` | Int | Tổng số đơn hàng |
| `payments` | Int | Tổng số lượt thanh toán thành công |
| `categories` | Int | Số lượng danh mục món ăn |
| `vouchers` | Int | Số lượng mã giảm giá đang tồn tại |
| `deliveredRevenue` | Double | Tổng doanh thu từ các đơn hàng đã giao thành công |

---

## 3. Chi tiết về Doanh thu (Delivered Revenue)

Để đảm bảo tính nhất quán giữa App và Server, chỉ số này cần được tính theo các quy tắc sau:

### 3.1. Quy tắc tính toán
- **Trạng thái đơn hàng**: Chỉ tính các đơn hàng có `status = "CONFIRMED"`.
- **Trạng thái thanh toán**: Chỉ tính các đơn đã hoàn tất thanh toán.
- **Giá trị**: Tổng tiền sau khi đã trừ các mã giảm giá do ADMIN tạo (Final Total).

### 3.2. API lấy chi tiết danh sách thu nhập từ từng đơn hàng
Truy vấn danh sách chi tiết các đơn hàng đóng góp vào doanh thu tổng của Admin.
- **Endpoint**: `GET /api/admin/dashboard/revenue-details`
- **Query Params**: 
    - `limit`, `offset`: Phân trang dữ liệu.
    - `startDate`, `endDate`: Lọc đơn hàng theo khoảng thời gian (Optional).
- **Mô tả**: Trả về danh sách các đơn hàng đã hoàn thành, bao gồm mã đơn hàng, nhà hàng, tổng tiền, hoa hồng/thu nhập thực tế của hệ thống và thời gian hoàn tất.
- **Ví dụ Response**:
```json
{
  "success": true,
  "data": [
    {
      "orderId": "ORD-10023",
      "restaurantName": "Pizza Hut - CMT8",
      "totalAmount": 250000.0,
      "netRevenue": 25000.0,
      "completedAt": "2023-10-25T14:30:00Z"
    }
  ]
}
```

---

## 4. Màn hình: Quản lý Danh mục (Category Management)
Dùng để quản lý các loại món ăn (Pizza, Burger, Sushi, Drinks...).

### 4.1. Lấy danh sách danh mục
- **Endpoint**: `GET /api/categories`
- **Query Params**: `keyword`, `limit`, `offset`
- **Response**: `BaseListResponse<CategoryResponse>`

### 4.2. Thêm mới / Cập nhật Danh mục (Yêu cầu bổ sung)
- **Endpoint**: `POST /api/categories` hoặc `PATCH /api/categories/{id}`
- **Method**: Multipart Form Data
- **Body Fields**: `name`, `displayOrder`, `isActive`, `image` (File).

---

## 5. Màn hình: Quản lý Nhà hàng (Restaurant Management)
Quản lý thông tin và xét duyệt trạng thái hoạt động của các đối tác nhà hàng.

### 5.1. Lấy danh sách nhà hàng
- **Endpoint**: `GET /api/restaurant`
- **Query Params**: `keyword`, `categoryId`, `limit`, `offset`

### 5.2. Cập nhật trạng thái nhà hàng (Yêu cầu bổ sung)
- **Endpoint**: `PATCH /api/admin/restaurant/{id}/status`
- **Body**: `{ "isActive": Boolean }`

---

## 6. Màn hình: Quản lý Voucher (Voucher Management)

### 6.1. Lấy danh sách Voucher
- **Endpoint**: `GET /api/vouchers`

### 6.2. Tạo mới / Cập nhật Voucher
- **Endpoint**: `POST /api/vouchers` hoặc `PATCH /api/vouchers/{id}`
- **Method**: Multipart Form Data

---

## 7. Màn hình: Quản lý Người dùng (User Management)

### 7.1. Danh sách người dùng (Yêu cầu bổ sung)
- **Endpoint**: `GET /api/admin/users`
- **Query Params**: `role`, `keyword`

### 7.2. Khóa/Mở khóa tài khoản
- **Endpoint**: `POST /api/admin/users/{id}/block`
- **Body**: `{ "isBlocked": Boolean, "reason": String }`

---
---
*Ghi chú: Các mục "Yêu cầu bổ sung" là những tính năng UI đã sẵn sàng ở Frontend nhưng cần Backend triển khai Endpoint tương ứng.*
