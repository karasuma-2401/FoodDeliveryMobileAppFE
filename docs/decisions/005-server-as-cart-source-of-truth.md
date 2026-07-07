# ADR-005: Server là Source of Truth cho Cart

## Status

Accepted

## Context

Multi-restaurant cart với logic phức tạp (line total, size, note, voucher) — khó reconcile nếu client tự tính.

## Decision

Sau **mỗi** cart API success (`getCart`, `addToCart`, `updateCartItem`, `deleteCartItem`, `clearCartByRestaurant`):

```kotlin
applyServerCart(cartResponse) {
    cartDao.clearCart()
    insert all items from response
    update restaurantGroups StateFlow
}
```

Optimistic chỉ là **window ngắn** trước khi API trả về — không phải permanent local truth.

Checkout gửi `clearCartAfterOrder = false` — server xóa selective theo restaurant.

## Alternatives considered

| Option | Cons |
|--------|------|
| Local cart primary, sync background | Conflict khi 2 device |
| Optimistic không sync lại server | Sai quantity, sai lineTotal |

## Consequences

- **Positive:** Cart luôn khớp BE; đơn giản debug
- **Negative:** Mỗi thao tác = 1 round-trip; cần optimistic để che latency
- **Files:** `CartRepositoryImpl.kt`

## Related

- [../learning/cart.md](../learning/cart.md)
- [../docs/Cart.md](../Cart.md) (BE spec)
