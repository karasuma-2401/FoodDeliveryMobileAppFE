# Recipe: Room Flow → Compose UI

## Khi nào dùng

UI cần tự cập nhật khi database thay đổi (chat messages, cart items, notifications).

## DAO

```kotlin
@Query("SELECT * FROM messages WHERE conversationId = :id ORDER BY createdAt ASC")
fun getMessages(conversationId: String): Flow<List<MessageEntity>>
```

## Repository

```kotlin
override fun getMessages(conversationId: String): Flow<List<MessageEntity>> =
    messageDao.getMessages(conversationId)
```

## ViewModel

```kotlin
private fun observeMessages(conversationId: String) {
    messageObserverJob?.cancel()
    messageObserverJob = viewModelScope.launch {
        chatRepository.getMessages(conversationId).collectLatest { messages ->
            _state.update { it.copy(messages = messages) }
        }
    }
}
```

## Compose Screen

```kotlin
val state by viewModel.state.collectAsStateWithLifecycle()
LazyColumn {
    items(state.messages) { msg -> ChatBubble(msg) }
}
```

## Lưu ý

- `collectLatest` — query mới cancel collect cũ (đổi conversation)
- Cancel job trong `onCleared()`
- Map Entity → UI model trong ViewModel nếu cần, không map trong Composable

## Tham chiếu

- [../learning/chat.md](../learning/chat.md)
- [../learning/cart.md](../learning/cart.md)
