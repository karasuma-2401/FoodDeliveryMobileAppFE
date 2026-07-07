# Learning Docs — Học từ Source Code Android

Tài liệu này mô tả **cách DFood Android FE đã implement** từng tính năng: file nào, pattern nào, bài học gì — để đọc lại sau này và áp dụng ở project khác.

> **Không thay thế** API docs (`docs/Apidocs.md`, `docs/OrderFlow.md`…). Đọc API trước, learning doc sau.

---

## Mỗi doc trả lời 5 câu hỏi

1. **Vấn đề gì?** — Nghiệp vụ + ràng buộc kỹ thuật
2. **Giải pháp chọn gì?** — Và vì sao không chọn cách khác
3. **Code chạy thế nào?** — Walkthrough file theo thứ tự
4. **Pattern học được gì?** — Tái sử dụng ở app khác
5. **Hạn chế / TODO** — Technical debt, lesson learned

---

## Danh sách tài liệu

### Nền tảng

| Doc | Nội dung |
|-----|----------|
| [architecture.md](architecture.md) | Clean Architecture, MVVM, package structure, DI |
| [patterns.md](patterns.md) | Catalog pattern lặp lại trong toàn app |

### Auth & Navigation

| Doc | Nội dung |
|-----|----------|
| [auth-session.md](auth-session.md) | Login, refresh token, session validate, social login |
| [navigation.md](navigation.md) | Multi-role graphs, routes, bottom bar badge |

### Customer — Khám phá & Mua hàng

| Doc | Nội dung |
|-----|----------|
| [delivery-location.md](delivery-location.md) | Shared state địa chỉ giao hàng |
| [home.md](home.md) | Dashboard, voucher badge enrichment |
| [search.md](search.md) | Debounce, filter, search history |
| [cart.md](cart.md) | Multi-restaurant cart, optimistic add |
| [checkout-payment.md](checkout-payment.md) | Voucher, MoMo deeplink, poll payment |
| [order-tracking.md](order-tracking.md) | Poll status, ETA countdown, confirm received |
| [address.md](address.md) | CRUD địa chỉ, map picker, Places search |

### Realtime & Push

| Doc | Nội dung |
|-----|----------|
| [chat.md](chat.md) | Socket.IO, optimistic message, polling fallback |
| [notification.md](notification.md) | FCM, Room cache, deep link trong app |

### Tương tác & Vendor

| Doc | Nội dung |
|-----|----------|
| [rating-review.md](rating-review.md) | Đánh giá sau đơn CONFIRMED |
| [restaurant-admin.md](restaurant-admin.md) | Dashboard NH, quản lý đơn/món, Admin |

### DevOps & Release

| Doc | Nội dung |
|-----|----------|
| [cicd-release.md](cicd-release.md) | GitHub Actions, release-please, signed APK, setup portable |
| [../cookbook/android-cicd-template.md](../cookbook/android-cicd-template.md) | Checklist 1 trang copy sang project mới |

---

## Cách đọc source theo 4 lớp

```
Screen (Composable)
  → viewModel.onEvent(...)
ViewModel (State / Event / UiEffect)
  → repository / useCase
RepositoryImpl
  → API / Room / Socket / DataStore
```

**Quy tắc:** Luôn bắt đầu từ Screen, không nhảy thẳng vào Repository.

---

## Tài liệu liên quan

- [Pattern catalog chi tiết](patterns.md)
- [Cookbook — snippet copy](../cookbook/)
- [ADR — quyết định thiết kế](../decisions/)
- [Troubleshooting](../troubleshooting.md)
- [Glossary](../glossary.md)
