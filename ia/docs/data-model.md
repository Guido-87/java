# Data and State

The current application does not define relational entities or a relational database schema. Conversation history is stored as JSON in Redis.

- Redis key format: `chat:{userId}`.
- `userId` is generated and stored in the HTTP session when a session has no user ID.
- The value is an ordered list of message maps containing `role` and `content` fields.
- The Redis entry expires after one hour when saved.
- The application also uses the HTTP session cookie to associate requests with a conversation.

Update this document only when the storage or conversation-state contract changes. Confirm details against `RedisService` and `ChatService` before documenting them.
