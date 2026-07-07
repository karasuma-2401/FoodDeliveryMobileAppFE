# DFood — Case Study & Portfolio Summary

Tài liệu tổng kết project để báo cáo đồ án, portfolio, hoặc phỏng vấn.

---

## 1. Tổng quan

**DFood** là ứng dụng Android đặt đồ ăn full-stack FE, kết nối Backend NestJS trên Azure. Một APK phục vụ **3 vai trò**: Khách hàng, Nhà hàng, Admin.

| Hạng mục | Chi tiết |
|----------|----------|
| Ngôn ngữ | Kotlin |
| UI | Jetpack Compose |
| Kiến trúc | Clean Architecture + MVVM |
| DI | Dagger Hilt |
| Network | Retrofit + OkHttp |
| Realtime | Socket.IO |
| Local DB | Room |
| Preferences | DataStore |
| Push | Firebase Cloud Messaging |
| CI/CD | GitHub Actions → APK release — [learning/cicd-release.md](learning/cicd-release.md) |

---

## 2. Điểm nổi bật kỹ thuật

### 2.1. Multi-restaurant cart (kiểu Shopee)
- Một giỏ nhiều nhà hàng; checkout từng NH
- Optimistic add + rollback snapshot
- Server-as-source-of-truth sync

→ [learning/cart.md](learning/cart.md)

### 2.2. Realtime chat hybrid
- Socket.IO + REST polling fallback (8s)
- Optimistic message + dedup + 15s timeout
- Upload ảnh REST → emit socket

→ [learning/chat.md](learning/chat.md)

### 2.3. Push notification + offline cache
- FCM + register device token
- Room cache, pagination, offline fallback
- In-app deep link (`resolveDestination`)

→ [learning/notification.md](learning/notification.md)

### 2.4. MoMo payment integration
- Deeplink mở app MoMo
- Poll payment status sau khi user quay lại
- UiEffect pattern tách navigation

→ [learning/checkout-payment.md](learning/checkout-payment.md)

### 2.5. Order tracking với ETA
- Poll 15s với generation guard chống race
- ETA countdown chỉ khi DELIVERING
- Confirm received flow

→ [learning/order-tracking.md](learning/order-tracking.md)

### 2.6. Shared delivery location state
- Singleton `DeliveryLocationRepository`
- Home / Search / Checkout đồng bộ địa chỉ

→ [learning/delivery-location.md](learning/delivery-location.md)

### 2.7. Session management
- Silent token refresh
- Reconnect socket sau refresh/login

→ [learning/auth-session.md](learning/auth-session.md)

---

## 3. Số liệu codebase (ước lượng)

| Metric | ~Value |
|--------|--------|
| Kotlin source files | 400+ |
| UI screens | 50+ |
| ViewModels | 40+ |
| Use cases | 34 |
| Room entities | 4 (notification, message, conversation, cart) |
| Unit tests | ChatMessageTime, AddressMapping |
| Docs learning | 16 feature guides |
| ADRs | 5 |

---

## 4. Patterns đã áp dụng

| Pattern | Feature |
|---------|---------|
| State + Event + UiEffect | Checkout, TrackOrder, Home |
| Optimistic UI + Rollback | Cart, Chat |
| Debounce + Job cancel | Search |
| Polling + generation guard | TrackOrder, MoMo |
| Offline-first read | Notification |
| Semaphore + cache | Voucher badge enrichment |
| Flow + Room reactive UI | Chat, Cart badge |

Chi tiết: [learning/patterns.md](learning/patterns.md)

---

## 5. Kiến trúc (tóm tắt)

```
UI (Compose) → ViewModel → UseCase? → Repository → API / Room / Socket / DataStore
```

→ [learning/architecture.md](learning/architecture.md)

---

## 6. Technical debt & hạn chế (trung thực)

| Hạn chế | Ghi chú |
|---------|---------|
| FCM tap chưa deep link | Chỉ mở MainActivity |
| Chat online status hardcode | Chưa có presence API |
| Naming typos | Conservation, Vertification |
| Test coverage thấp | Chủ yếu manual + vài unit test |
| Refresh token chưa global interceptor | Tập trung ValidateSession |

→ [troubleshooting.md](troubleshooting.md)

---

## 7. Câu hỏi phỏng vấn — gợi ý trả lời từ project

**Q: Làm sao đảm bảo chat không mất tin khi socket rớt?**  
A: Hybrid socket + poll REST 8s; Room cache; join-room lại khi reconnect.

**Q: Optimistic UI xử lý fail thế nào?**  
A: Snapshot trước khi đổi local; API fail rollback; chat có timeout 15s mark failed.

**Q: Multi-restaurant cart reconcile ra sao?**  
A: Server là source of truth — mỗi API success replace local cart; optimistic chỉ window ngắn.

**Q: Tách state navigation khỏi UI state?**  
A: UiEffect (SharedFlow) cho navigate/MoMo — không put trong StateFlow.

---

## 8. Demo kịch bản cho presentation

1. **Multi-cart:** 2 NH → checkout 1 → cart còn 1
2. **Chat realtime:** Gửi tin + ảnh giữa customer và NH
3. **MoMo:** Place order → MoMo → quay lại → success
4. **Track order:** Timeline + ETA khi DELIVERING
5. **Push:** Notification đơn mới → badge → tap in-app list

→ [journeys/customer-journey.md](journeys/customer-journey.md)

---

## 9. Link tài liệu đầy đủ

- [Documentation Index](README.md)
- [Learning guides](learning/README.md)
- [CI/CD & auto release](learning/cicd-release.md)
- [Architecture Decisions](decisions/)
- [Cookbook](cookbook/)
- [Glossary](glossary.md)

---

## 10. Nếu làm lại project từ đầu

1. Giữ Clean Architecture + Compose — proven
2. Viết ADR ngay khi có quyết định (socket hybrid, cart sync)
3. Thêm OkHttp Authenticator cho refresh token global
4. FCM deep link từ đầu
5. Test ViewModel critical paths (cart rollback, chat dedup)
6. Unify naming (Conversation, Verification)
