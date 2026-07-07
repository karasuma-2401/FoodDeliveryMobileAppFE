# Rating & Review — Sau Đơn CONFIRMED

## 1. Bài toán

- Khách đánh giá món / nhà hàng sau khi đơn **CONFIRMED**
- Restaurant xem và phản hồi review
- User xem lịch sử review của mình

## 2. Walkthrough source

| File | Vai trò |
|------|---------|
| `ui/screens/rating_reviews/RatingReviewScreen.kt` | Form đánh giá từ order |
| `ui/screens/rating_reviews/restaurant_reviews/ReviewScreen.kt` | Reviews của NH |
| `ui/screens/customer/review/UserReviewScreen.kt` | Review của user |
| `ui/screens/restaurant/component/dashboard/DashBoardReview.kt` | Widget dashboard NH |
| `ui/screens/rating_reviews/components/StarRatingBar.kt` | UI sao |

## 3. Business rules (từ BE)

Xem `docs/Rating&ReviewFlows.md`:

| Rule | Chi tiết |
|------|----------|
| Điều kiện review | Order status = `CONFIRMED` |
| Ai review | Customer của order |
| Đánh giá | Món và/hoặc nhà hàng, sao + comment + tags |

## 4. Navigation

`RatingReviewRoute(orderId, restaurantId, restaurantName, ...)` — có thể pre-fill từ order flow hoặc track order sau confirm.

`LikeRestaurant` flow: `docs/LikeRestaurant.md`

## 5. Restaurant side

- `DashBoardReview` trên dashboard — quick access reviews
- Restaurant có thể reply (API theo `Rating&ReviewFlows.md`)

## 6. Pattern UI

- Star rating bar reusable components
- `ReviewContentCard` — hiển thị review item
- Skeleton / empty states theo pattern Home/Chat

## 7. Demo scenarios

```
1. Order CONFIRMED → mở RatingReview → submit
2. Vào Restaurant Detail → tab reviews
3. Profile → UserReview — xem lại đã đánh giá
```

## 8. Nếu làm lại từ đầu

1. API review CRUD (gắn orderId, restaurantId, foodId)
2. `RatingReviewScreen` + ViewModel validate CONFIRMED
3. Star rating component
4. List reviews trên restaurant detail
5. Dashboard widget cho NH

## 9. Tham chiếu

- [order-tracking.md](order-tracking.md) — confirm received → CONFIRMED
- [docs/Rating&ReviewFlows.md](../Rating&ReviewFlows.md)
