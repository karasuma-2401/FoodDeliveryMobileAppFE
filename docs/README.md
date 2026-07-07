# DFood — Documentation Index

Tài liệu tổng hợp cho project **FoodDeliveryMobileAppFE** (Android / Kotlin / Jetpack Compose).

---

## Đọc theo mục đích

| Mục đích | Bắt đầu từ |
|----------|------------|
| Setup & chạy app | [README.md](../README.md) (root) |
| Học cách FE implement từng tính năng | [learning/README.md](learning/README.md) |
| Pattern tái sử dụng / copy recipe | [cookbook/](cookbook/) |
| Vì sao chọn thiết kế X | [decisions/](decisions/) |
| Luồng user end-to-end | [journeys/](journeys/) |
| Debug lỗi thường gặp | [troubleshooting.md](troubleshooting.md) |
| CI/CD & auto release (setup portable) | [learning/cicd-release.md](learning/cicd-release.md) |
| Thuật ngữ domain | [glossary.md](glossary.md) |
| Tổng kết portfolio / đồ án | [case-study.md](case-study.md) |
| API contract BE | [Apidocs.md](Apidocs.md) |

---

## Cấu trúc thư mục

```
docs/
├── README.md                 ← Bạn đang ở đây
├── glossary.md
├── troubleshooting.md
├── case-study.md
│
├── learning/                 ← Học từ source code (Android FE)
│   ├── README.md
│   ├── architecture.md
│   ├── patterns.md
│   ├── auth-session.md
│   ├── navigation.md
│   ├── delivery-location.md
│   ├── home.md
│   ├── search.md
│   ├── cart.md
│   ├── checkout-payment.md
│   ├── order-tracking.md
│   ├── address.md
│   ├── chat.md
│   ├── notification.md
│   ├── rating-review.md
│   ├── restaurant-admin.md
│   └── cicd-release.md
│
├── decisions/                ← ADR — quyết định kiến trúc
│   ├── 001-socket-plus-polling.md
│   ├── 002-room-cache-strategy.md
│   ├── 003-optimistic-ui.md
│   ├── 004-token-refresh-chain.md
│   └── 005-server-as-cart-source-of-truth.md
│
├── cookbook/                 ← Snippet recipe ngắn
│   ├── debounce-search.md
│   ├── optimistic-ui-rollback.md
│   ├── ui-effect-navigation.md
│   ├── room-flow-compose.md
│   ├── pagination-infinite-scroll.md
│   ├── polling-with-cancel.md
│   └── android-cicd-template.md
│
├── journeys/                 ← User journey maps
│   ├── customer-journey.md
│   └── restaurant-journey.md
│
└── (API / business docs hiện có)
    ├── Apidocs.md
    ├── chat.md               ← API Socket/REST (BE contract)
    ├── Cart.md
    ├── CheckoutFlow.md
    ├── OrderFlow.md
    ├── Home.md
    ├── Search.md
    ├── address.md
    ├── payment-guide.md
    └── ...
```

---

## Lộ trình học đề xuất

### Tuần 1 — Nền tảng
1. [learning/architecture.md](learning/architecture.md)
2. [learning/patterns.md](learning/patterns.md)
3. [learning/auth-session.md](learning/auth-session.md)
4. [learning/navigation.md](learning/navigation.md)

### Tuần 2 — Customer core
5. [learning/delivery-location.md](learning/delivery-location.md)
6. [learning/home.md](learning/home.md)
7. [learning/search.md](learning/search.md)
8. [learning/cart.md](learning/cart.md)

### Tuần 3 — Order & realtime
9. [learning/checkout-payment.md](learning/checkout-payment.md)
10. [learning/order-tracking.md](learning/order-tracking.md)
11. [learning/chat.md](learning/chat.md)
12. [learning/notification.md](learning/notification.md)

### Tuần 4 — Bổ sung & DevOps
13. [learning/address.md](learning/address.md)
14. [learning/rating-review.md](learning/rating-review.md)
15. [learning/restaurant-admin.md](learning/restaurant-admin.md)
16. [learning/cicd-release.md](learning/cicd-release.md)
17. [case-study.md](case-study.md)

---

## Phân biệt 2 loại tài liệu

| Loại | Ví dụ | Trả lời câu hỏi |
|------|-------|-----------------|
| **API / Business** | `OrderFlow.md`, `docs/chat.md` | BE trả gì? Luồng nghiệp vụ thế nào? |
| **Learning / Implementation** | `learning/*.md` | Android code xử lý ra sao? Pattern nào học được? |

Cả hai bổ sung cho nhau — đọc API doc trước, rồi đọc learning doc để hiểu FE.
