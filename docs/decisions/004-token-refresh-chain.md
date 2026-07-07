# ADR-004: Token Refresh Chain tại ValidateSession

## Status

Accepted

## Context

Access token JWT hết hạn trong khi refresh token còn valid. User không nên bị đá về login mỗi lần token expire.

Socket chat bind JWT — token đổi phải reconnect.

## Decision

Centralize tại `ValidateSessionUseCase.fetchMeWithRefresh()`:

```
getMe() → fail Unauthorized
  → refreshToken()
  → updateTokens()
  → chatSocketManager.reconnectWithCurrentToken()
  → retry getMe()
  → fail final → clearAuthData() + socket.disconnect()
```

Gọi từ `MainViewModel` cold start và có thể mở rộng cho API interceptor.

`LoginUseCase` cũng gọi `reconnectWithCurrentToken()` sau login mới.

## Alternatives considered

| Option | Cons |
|--------|------|
| Refresh trong mỗi ViewModel | Duplicate, inconsistent |
| Không reconnect socket | Chat auth fail silent |

## Consequences

- **Positive:** Session liền mạch; chat hoạt động sau refresh
- **Negative:** Refresh logic chưa tách OkHttp Authenticator — một số API 401 có thể chưa auto-retry
- **Files:** `ValidateSessionUseCase.kt`, `LoginUseCase.kt`, `ChatSocketManager.kt`

## Related

- [../learning/auth-session.md](../learning/auth-session.md)
