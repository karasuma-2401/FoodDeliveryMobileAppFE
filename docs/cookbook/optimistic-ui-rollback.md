# Recipe: Optimistic UI + Rollback

## Khi nào dùng

Action user cần phản hồi ngay: add cart, send message, toggle like.

## Pattern

```
1. Snapshot state hiện tại
2. Apply optimistic change local
3. Call API / Socket
4. Success → reconcile với server response
5. Failure → rollback từ snapshot
```

## Cart example (DFood)

```kotlin
val snapshot = captureOptimisticSnapshot(entity)
cartDao.addToCartAtomic(entity)
return addToCart(...)
    .onFailure { rollbackOptimisticAdd(snapshot) }

data class OptimisticCartSnapshot(
    val hadExistingItem: Boolean,
    val quantityBefore: Int?
)
```

## Chat example (DFood)

```kotlin
// Optimistic insert
messageDao.insertMessage(MessageEntity(id=tempId, isSending=true, ...))
socket.emit("text-chat", json)

// Server ack — dedup
messageDao.deleteOptimisticDuplicates(conversationId, senderId, serverMessageId)
messageDao.insertMessage(serverMessage)
```

## Template tối giản

```kotlin
suspend fun optimisticUpdate(
    applyLocal: suspend () -> Unit,
    rollback: suspend () -> Unit,
    remote: suspend () -> Result<Unit>
): Result<Unit> {
    applyLocal()
    return remote().onFailure { rollback() }
}
```

## Lưu ý

- Snapshot phải đủ để undo (không chỉ boolean)
- Server message id khác temp id → cần dedup logic
- Timeout → mark failed, cho phép retry

## Tham chiếu

- [../decisions/003-optimistic-ui.md](../decisions/003-optimistic-ui.md)
- [../learning/cart.md](../learning/cart.md)
- [../learning/chat.md](../learning/chat.md)
