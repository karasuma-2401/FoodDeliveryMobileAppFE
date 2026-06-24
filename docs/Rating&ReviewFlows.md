# Tài liệu Tính năng Rating & Review

Tài liệu này mô tả luồng hoạt động và danh sách các API cho tính năng Đánh giá và Nhận xét dành cho cả Khách hàng và Nhà hàng.

---

## 1. Luồng nghiệp vụ (Workflow)

### A. Phía Khách hàng (Customer)
1. **Kích hoạt:** Người dùng hoàn thành đơn hàng (Status: `COMPLETED`).
2. **Truy cập:** Vào `My Orders` -> `History` -> Nhấn nút `Rate`.
3. **Nhập liệu:** 
    - Chọn số sao (1-5).
    - Chọn Tags gợi ý (Món ăn ngon, Giao hàng nhanh, ...).
    - Viết nhận xét (Optional).
4. **Xử lý:** Gọi API gửi đánh giá. Sau khi thành công, trạng thái đơn hàng sẽ được cập nhật là đã đánh giá.
5. **Quản lý:** Xem lại tại màn hình `My Reviews` trong Profile.

### B. Phía Nhà hàng (Restaurant/Vendor)
1. **Theo dõi:** Xem tổng điểm đánh giá trung bình và thống kê sao tại màn hình **Dashboard**.
2. **Quản lý:** Truy cập màn hình **Reviews** để xem danh sách chi tiết tất cả nhận xét của khách hàng.
3. **Phản hồi:** Chủ cửa hàng có thể phản hồi (Reply) lại đánh giá của khách để giải đáp hoặc cảm ơn.
4. **Cải thiện:** Dựa vào các Tags và bình luận để cải thiện chất lượng dịch vụ/món ăn.

---

## 2. Danh sách API chi tiết

### Nhóm API dành cho Khách hàng
- **API 1: Gửi đánh giá mới**
    - **Method:** `POST`
    - **Endpoint:** `/api/restaurant/{id}/ratings`
    - **Body:** `{ "restaurantId": 101, "orderId": 162432, "vote": 5, "comment": "...", "tags": [...] }`
- **API 2: Lấy danh sách review của tôi**
    - **Method:** `GET`
    - **Endpoint:** `/api/user/reviews`
- **API 3: Cập nhật / Xóa đánh giá**
    - `PUT /api/ratings/{reviewId}`
    - `DELETE /api/ratings/{reviewId}`
- **API 4: Xem review của nhà hàng (Dành cho khách)**
    - **Method:** `GET`
    - **Endpoint:** `/api/restaurant/{id}/reviews`

### Nhóm API dành cho Nhà hàng
- **API 5: Lấy danh sách review của nhà hàng hiện tại (Vendor View)**
    - **Method:** `GET`
    - **Endpoint:** `/api/restaurant/my-reviews`
- **API 6: Phản hồi đánh giá**
    - **Method:** `POST`
    - **Endpoint:** `/api/restaurant/reviews/{reviewId}/reply`
    - **Body:** `{ "replyMessage": "Cảm ơn bạn đã ủng hộ quán!" }`
- **API 7: Thống kê đánh giá tại Dashboard**
    - **Method:** `GET`
    - **Endpoint:** `/api/restaurant/stats/ratings`
    - **Response:** `{ "averageRating": 4.5, "totalReviews": 100, "starCount": { "5": 80, "4": 15, ... } }`

---

## 3. Cấu trúc dữ liệu (Model)

### Model: UserReview (Phía Customer)
```kotlin
data class UserReview(
    val id: String,
    val restaurantId: String,
    val restaurantName: String,
    val rating: Int,
    val comment: String,
    val tags: List<String>,
    val createdAt: Long,
    val orderId: String
)
```

### Model: ReviewItem (Phía Restaurant)
```kotlin
data class ReviewItem(
    val id: String,
    val userName: String,
    val userAvatarUrl: String?,
    val date: String,
    val rating: Int,
    val description: String,
    val replyMessage: String? = null
)
```
