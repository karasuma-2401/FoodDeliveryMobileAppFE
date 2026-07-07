# ADR-001: Socket + Polling Hybrid cho Chat

## Status

Accepted

## Context

Chat cần realtime qua Socket.IO. Tuy nhiên trên mobile:
- Socket có thể disconnect (background, mạng yếu, token refresh)
- User có thể miss event `text-chat` khi chưa join room kịp

## Decision

Dùng **hybrid**:
1. **Primary:** Socket.IO — `join-room`, emit/listen `text-chat`
2. **Fallback:** REST poll `syncConversationDetail` mỗi **8 giây** khi đang ở màn chat (`ChatViewModel`)

## Alternatives considered

| Option | Pros | Cons |
|--------|------|------|
| Chỉ Socket | Ít API call | Miss message khi disconnect |
| Chỉ Poll | Đơn giản | Tốn pin/băng thông, không instant |
| **Hybrid** | Reliable + UX tốt | Phức tạp hơn, duplicate handling cần idempotent Room insert |

## Consequences

- **Positive:** Tin nhắn vẫn sync khi socket flappy
- **Negative:** Thêm API load; cần dedup optimistic vs server message (`handleNewMessage`)
- **Files:** `ChatViewModel.kt`, `ChatRepositoryImpl.kt`, `ChatSocketManager.kt`

## Related

- [../learning/chat.md](../learning/chat.md)
- [../cookbook/polling-with-cancel.md](../cookbook/polling-with-cancel.md)
