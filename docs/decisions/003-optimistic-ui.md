# ADR-003: Optimistic UI cho Cart và Chat

## Status

Accepted

## Context

User expect phản hồi tức thì khi:
- Thêm món vào giỏ
- Gửi tin nhắn chat

Chờ API/socket round-trip tạo cảm giác app chậm.

## Decision

**Optimistic UI** với rollback / dedup:

### Cart
1. `cartDao.addToCartAtomic(entity)` ngay
2. Gọi `cartApi.addToCart`
3. Fail → `rollbackOptimisticAdd(snapshot)` khôi phục quantity cũ

### Chat
1. Insert `MessageEntity` với `isSending=true`, temp UUID
2. Emit socket `text-chat`
3. Server ack → `deleteOptimisticDuplicates` + insert message có server id
4. Timeout 15s → `isFailed=true`
5. Socket `exception` → `markOptimisticSendingAsFailed()`

## Alternatives considered

| Option | Cons |
|--------|------|
| Chờ server rồi mới hiện UI | UX chậm |
| Optimistic không rollback | Data sai khi fail |

## Consequences

- **Positive:** UX mượt, giống app thương mại điện tử / messenger
- **Negative:** Logic phức tạp; cần test edge cases (duplicate, timeout, reorder)
- **Pattern reusable:** Like, follow, wishlist

## Related

- [../cookbook/optimistic-ui-rollback.md](../cookbook/optimistic-ui-rollback.md)
- [../learning/cart.md](../learning/cart.md)
- [../learning/chat.md](../learning/chat.md)
