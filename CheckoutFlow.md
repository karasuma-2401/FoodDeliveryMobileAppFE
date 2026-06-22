# Checkout & Payment Flow Documentation

Tài liệu này mô tả chi tiết luồng thanh toán và xử lý đơn hàng giữa Mobile App (Android) và Backend, bao gồm tích hợp cổng thanh toán MoMo và phương thức Tiền mặt (Cash).

---

## 1. Khởi tạo Đơn hàng (Checkout)

Người dùng thực hiện nhấn nút đặt hàng từ màn hình Checkout.

- **Endpoint:** `POST /api/orders`
- **Authentication:** Bearer Token (Bắt buộc)
- **Request Body:**
```json
{  
  "restaurantId": 123,  
  "voucherId": 1, // Optional  
  "savedAddressId": 45, // Chọn 1 trong 2: savedAddressId hoặc customAddress
  "orderFoods": [
    {  
      "foodId": 1,  
      "quantity": 2,  
      "foodSizeId": 10,  
      "fullText": "Không hành" 
    }
  ],  
  "note": "Giao tầng 5",  
  "paymentMethod": "CASH" | "MOMO",  
  "clearCartAfterOrder": true
}
```

---

## 2. Luồng Thanh toán MoMo (Online Payment)

Đây là luồng thanh toán tự động thông qua cổng MoMo.

### Quy trình:
1. **Gửi Request:** App gọi API tạo đơn hàng với `paymentMethod: "MOMO"`.
2. **Nhận Deeplink:** Backend trả về object `momoPayment` chứa `deeplink` và `payUrl`.
3. **Mở App MoMo:** App Android sử dụng `Intent.ACTION_VIEW` để mở ứng dụng MoMo thông qua `deeplink`.
4. **Bắt đầu Polling:** Ngay sau khi mở MoMo, App bắt đầu chạy một Job Polling gọi API `GET /api/orders/{id}/status` mỗi 3 giây.
5. **Xử lý Webhook (Server-to-Server):**
    - Khi khách hàng hoàn tất thanh toán trên MoMo, MoMo Server gọi Webhook tới Backend (`POST /api/payment/check-payment`).
    - Backend xác thực chữ ký và cập nhật trạng thái đơn hàng từ `PENDING` sang `CONFIRMED` (`status_step` chuyển từ 1 lên 2).
6. **Kết thúc Polling:** 
    - Job Polling trên App nhận được `status_step >= 2`.
    - App dừng Polling và điều hướng người dùng đến màn hình **Payment Successful**.

---

## 3. Luồng Tiền mặt (Cash on Delivery)

Luồng thanh toán thủ công khi nhận hàng.

### Quy trình:
1. **Gửi Request:** App gọi API tạo đơn hàng với `paymentMethod: "CASH"`.
2. **Nhận Phản hồi:** Backend tạo đơn hàng với trạng thái `PENDING` và Payment trạng thái `UNPAID`.
3. **Hoàn tất trên App:** App điều hướng người dùng đến màn hình Thành công ngay lập tức.
4. **Xác nhận (Manual):** Admin hoặc Chủ cửa hàng sẽ xác nhận đơn hàng và thanh toán thủ công thông qua Dashboard khi nhận được tiền mặt.

---

## 4. Trạng thái Đơn hàng (Status Mapping)

Hệ thống sử dụng `status_step` để đồng bộ hiển thị Progress Bar trên App:

| Step | Backend Status | Mô tả hiển thị trên App |
| :--- | :--- | :--- |
| **1** | `PENDING` | Chờ xác nhận (Order Received) |
| **2** | `CONFIRMED` | Đã xác nhận (Confirmed) |
| **3** | `PREPARING` | Đang chuẩn bị (Preparing Food) |
| **4** | `DELIVERING` | Đang giao hàng (On the Way) |
| **5** | `DELIVERED` | Đã giao hàng (Delivered) |
| **5** | `CANCELLED` | Đã hủy đơn hàng |

---

## 5. Xem Chi tiết Đơn hàng (Order Details)

Sau khi đặt hàng, người dùng có thể xem lại toàn bộ thông tin tại màn hình **Track Order**.

- **Endpoint:** `GET /api/orders/{orderId}`
- **Thông tin hiển thị:**
    - **Live Tracking:** Progress bar dựa trên `status_step`.
    - **Expected Arrival:** Thời gian dự kiến giao hàng (tính bằng: thời điểm thanh toán + thời gian ước tính của nhà hàng).
    - **Payment Details:** Phương thức thanh toán, trạng thái (DONE, SOLVING, FAILED, UNPAID).
    - **Order Summary:** Danh sách món ăn, số lượng, giá tiền, ghi chú món.
    - **Voucher:** Chi tiết mã giảm giá đã áp dụng.
    - **Contact:** Nút gọi điện hoặc chat trực tiếp với nhà hàng thông qua `conversationId`.

---

## 6. Lưu ý cho Nhà phát triển

1. **Polling vs Redirect:** App sử dụng Polling thay vì chờ `redirectUrl` của MoMo để đảm bảo tính ổn định (tránh trường hợp khách hàng không quay lại App sau khi thanh toán).
2. **Webhook (IPN):** Backend cần được deploy lên một server có IP Public hoặc sử dụng Tunnel (Ngrok) để MoMo Server có thể gọi Webhook thành công.
3. **Timeouts:** Polling trên App nên giới hạn số lần thử (ví dụ: tối đa 10-15 lần) để tránh tiêu tốn tài nguyên nếu giao dịch bị treo.
