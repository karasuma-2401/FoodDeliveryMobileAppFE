# ADR-002: Room làm Cache, không phải Source of Truth (hầu hết features)

## Status

Accepted

## Context

Notification và chat cần hiển thị nhanh, hoạt động khi API chậm/fail. Cart cần optimistic local trong thời gian ngắn.

## Decision

| Data | Source of truth | Room role |
|------|-----------------|-----------|
| Notifications | Server | Cache + offline read fallback |
| Chat messages | Server (+ socket) | Cache + optimistic send buffer |
| Conversations list | Server | Cache + merge preview local |
| Cart | **Server** (sau mỗi API success) | Mirror + optimistic window |

Sync pattern:
- `syncNotifications()` — pull 50 mới nhất vào Room
- `syncConversations()` / `syncConversationDetail()` — upsert Room
- `applyServerCart()` — **replace** toàn bộ cart local từ response

## Alternatives considered

| Option | Rejected because |
|--------|------------------|
| Room là primary cho cart | Conflict multi-device, khó reconcile |
| Không cache notification | UX kém offline, list trống khi API fail |

## Consequences

- **Positive:** Offline-first read cho notification; chat UI reactive qua Flow
- **Negative:** Cần migration khi schema đổi (`AppDatabase` version 8)
- **Risk:** Stale cache nếu không sync — mitigate bằng ON_RESUME refresh

## Related

- [../learning/notification.md](../learning/notification.md)
- [../decisions/005-server-as-cart-source-of-truth.md](005-server-as-cart-source-of-truth.md)
