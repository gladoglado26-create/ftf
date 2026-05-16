# Social Platform Backend

**Author:** Manus AI  
**Stack:** Kotlin, Ktor, Koin, Exposed, PostgreSQL, Redis, RabbitMQ, Supabase Auth/Storage, WebSockets, WebRTC signaling, Firebase Cloud Messaging, Docker.

## Overview

This repository contains an end-to-end backend implementation for a hybrid social, messaging, realtime, and calling platform inspired by the interaction patterns of Instagram, Telegram, and Discord. The service is implemented with **Ktor** for HTTP and WebSocket APIs, **Koin** for dependency injection, **Exposed** for SQL persistence, **PostgreSQL** for relational data, **Redis** for ephemeral realtime state, and **RabbitMQ** for event-driven bot and notification workflows. Supabase is used as the external identity and object-storage provider, while Firebase Cloud Messaging and Jamendo-compatible music search hooks are prepared as optional integrations.

The backend has been generated as a production-oriented scaffold rather than a throwaway mock. It includes application configuration, authentication middleware, database schema, domain services, HTTP routes, realtime WebSocket handling, WebRTC signaling endpoints, upload support, notification token registration, event publishing, bot-rule execution, background workers, tests, and Docker-based local infrastructure.

> The build has been verified locally with `gradle compileKotlin` and `gradle test`; both tasks completed successfully in the sandbox environment.

## Verification Status

| Area | Result | Notes |
|---|---:|---|
| Kotlin main-source compilation | Passed | `gradle compileKotlin --no-daemon --console=plain` completed with `BUILD SUCCESSFUL`. |
| Test suite | Passed | `gradle test --no-daemon --console=plain` completed with `BUILD SUCCESSFUL`. |
| Environment template | Present | `.env.example` includes all runtime variables used by `AppConfig`. |
| Container deployment files | Present | `Dockerfile`, `docker-compose.yml`, and `scripts/healthcheck.sh` are included. |
| Database schema definitions | Present | Core tables are defined in `src/main/kotlin/com/socialplatform/database/Tables.kt`. |

## Architecture

The application is organized into feature modules under `src/main/kotlin/com/socialplatform`. The `Application.kt` entry point installs Ktor plugins, dependency injection, security, error handling, serialization, rate limiting, database connectivity, and feature routes. Each domain package separates transport-level routing from service-level business logic where appropriate.

| Package | Responsibility |
|---|---|
| `api` | Common API response envelopes and shared DTOs. |
| `config` | Environment-backed application configuration. |
| `database` | PostgreSQL connection setup, Exposed transaction helpers, and table definitions. |
| `security` | Supabase JWT validation, authenticated user extraction, and Ktor auth configuration. |
| `users` | Profile management, follow graph, blocking, discovery, nearby search, and presence. |
| `posts` | Posts, feed, reels-style listing, likes, saves, comments, stories, and story expiration. |
| `chat` | Conversations, message sending, edits, deletes, reactions, and typing signals. |
| `realtime` | WebSocket hub for presence, typing, message, notification, and call signaling frames. |
| `calls` | WebRTC signaling lifecycle for initiating, answering, ICE candidates, and ending calls. |
| `upload` | Supabase Storage upload integration and music search endpoint. |
| `notifications` | Notification listing, read state, device token registration, and optional FCM dispatch. |
| `events` | RabbitMQ event publishing and consumption primitives. |
| `bots` | Event-driven bot rule evaluation and execution logging. |
| `workers` | Background workers for story expiration, analytics hooks, and moderation hooks. |

## Prerequisites

You need a JDK-compatible environment and Docker if you want to run the full local stack. The project was verified using Gradle 8.9 and Kotlin 1.9.24. Docker Compose is the recommended way to start PostgreSQL, Redis, RabbitMQ, and the API together.

| Dependency | Purpose |
|---|---|
| JDK 17 or newer | Compile and run the Kotlin/Ktor backend. |
| Gradle 8.9 or wrapper-equivalent | Build, test, and install the application distribution. |
| Docker and Docker Compose | Run the API with PostgreSQL, Redis, and RabbitMQ locally. |
| Supabase project | Production authentication and object storage. |
| Firebase server key | Optional push notification delivery. |
| Jamendo client ID | Optional music search integration. |

## Environment Setup

Copy the included example file and fill in production secrets before running the service outside local development.

```bash
cp .env.example .env
```

The following variables are read by the backend at startup.

| Variable | Required | Description |
|---|---:|---|
| `APP_ENV` | No | Runtime environment label such as `dev`, `staging`, or `prod`. |
| `PUBLIC_URL` | No | Public API base URL used when creating externally visible links. |
| `PORT` | No | Ktor HTTP port; defaults to `8080` through the deployment configuration. |
| `DATABASE_URL` | Yes | JDBC URL for PostgreSQL. |
| `DATABASE_USER` | Yes | PostgreSQL username. |
| `DATABASE_PASSWORD` | Yes | PostgreSQL password. |
| `DATABASE_POOL_SIZE` | No | Hikari pool size. |
| `REDIS_URL` | Yes | Redis connection URL. |
| `RABBITMQ_URI` | Yes | RabbitMQ AMQP URI. |
| `RABBITMQ_EXCHANGE` | No | RabbitMQ exchange for application events. |
| `SUPABASE_URL` | Yes | Supabase project URL. |
| `SUPABASE_ANON_KEY` | Yes | Supabase anonymous key. |
| `SUPABASE_SERVICE_ROLE_KEY` | Yes | Supabase service-role key for server-side storage operations. |
| `SUPABASE_JWT_SECRET` | Yes | JWT secret used to verify Supabase-issued access tokens. |
| `SUPABASE_STORAGE_BUCKET` | Yes | Storage bucket name for uploaded media. |
| `JWT_AUDIENCE` | No | JWT audience expected by auth middleware. |
| `JWT_ISSUER` | No | JWT issuer expected by auth middleware. |
| `RATE_LIMIT_PER_MINUTE` | No | Ktor rate-limit threshold. |
| `FCM_SERVER_KEY` | No | Firebase Cloud Messaging server key. |
| `JAMENDO_CLIENT_ID` | No | Jamendo API client ID for music search. |

## Local Development

Run infrastructure dependencies with Docker Compose and then start the API from your IDE or Gradle.

```bash
docker compose up -d postgres redis rabbitmq
./gradlew run
```

If you do not use a Gradle wrapper, run the same tasks with your installed Gradle distribution.

```bash
gradle run
```

The health endpoint is available at:

```bash
curl http://localhost:8080/health
```

## Full Docker Run

The repository includes a multi-stage `Dockerfile` and a full `docker-compose.yml` file. To run the complete local stack, create `.env` first and then execute:

```bash
docker compose up --build
```

The API will listen on `http://localhost:8080`, RabbitMQ Management will be available at `http://localhost:15672`, PostgreSQL on `localhost:5432`, and Redis on `localhost:6379`.

## API Surface

All authenticated endpoints expect a Supabase-compatible bearer token.

```http
Authorization: Bearer <supabase-access-token>
```

The standard response envelope follows this shape:

```json
{
  "data": {},
  "error": null
}
```

| Area | Method and path | Description |
|---|---|---|
| Health | `GET /health` | Returns service status for health checks. |
| Users | `GET /users/me` | Fetch the authenticated user profile. |
| Users | `PUT /users/me` | Update profile fields such as username, display name, bio, avatar, or location. |
| Users | `GET /users/{id}` | Fetch a public user profile. |
| Users | `GET /users/search?q=...` | Search users by username or display name. |
| Users | `GET /users/nearby?lat=...&lng=...` | Find users near a coordinate. |
| Social | `POST /users/{id}/follow` | Follow a user. |
| Social | `DELETE /users/{id}/follow` | Unfollow a user. |
| Social | `POST /users/{id}/block` | Block a user. |
| Social | `DELETE /users/{id}/block` | Unblock a user. |
| Presence | `POST /users/presence` | Update authenticated user presence. |
| Posts | `GET /posts/feed` | Fetch feed posts. |
| Posts | `POST /posts` | Create a new post. |
| Posts | `GET /posts/{id}` | Fetch a post. |
| Posts | `DELETE /posts/{id}` | Soft-delete a post. |
| Posts | `POST /posts/{id}/like` | Like a post. |
| Posts | `DELETE /posts/{id}/like` | Remove a like. |
| Posts | `POST /posts/{id}/save` | Save a post. |
| Posts | `DELETE /posts/{id}/save` | Remove a save. |
| Comments | `POST /posts/{id}/comments` | Add a comment. |
| Comments | `GET /posts/{id}/comments` | List comments for a post. |
| Reels | `GET /reels` | Fetch reels-style media feed. |
| Stories | `GET /stories` | List active stories. |
| Stories | `POST /stories` | Create a story. |
| Stories | `POST /stories/{id}/view` | Mark a story as viewed. |
| Chat | `GET /chat/conversations` | List conversations for the authenticated user. |
| Chat | `POST /chat/conversations` | Create one-to-one or group conversations. |
| Chat | `GET /chat/conversations/{id}/messages` | List messages in a conversation. |
| Chat | `POST /chat/conversations/{id}/messages` | Send a message. |
| Chat | `POST /chat/conversations/{id}/typing` | Emit typing status. |
| Chat | `PATCH /chat/messages/{id}` | Edit a sent message. |
| Chat | `DELETE /chat/messages/{id}` | Soft-delete a sent message. |
| Chat | `POST /chat/messages/{id}/react` | React to a message. |
| Realtime | `GET /ws` | WebSocket endpoint for presence, messages, typing, notifications, and calls. |
| Calls | `POST /calls/initiate` | Start WebRTC call signaling. |
| Calls | `POST /calls/answer` | Submit SDP answer. |
| Calls | `POST /calls/ice-candidate` | Add ICE candidate. |
| Calls | `POST /calls/end` | End a call. |
| Uploads | `POST /upload/avatar` | Upload avatar media to Supabase Storage. |
| Uploads | `POST /upload/media` | Upload post, story, or chat media. |
| Music | `GET /music/search?q=...` | Search music tracks through the configured provider. |
| Notifications | `GET /notifications` | List notifications. |
| Notifications | `POST /notifications/read-all` | Mark all notifications as read. |
| Notifications | `POST /notifications/token` | Register a device token for push notifications. |

## WebSocket Protocol

The WebSocket server accepts JSON text frames. Each frame includes a `type` field and additional fields depending on the event. The connection is authenticated by passing the access token as a query parameter or by adapting the client to include authorization during the opening handshake.

| Frame type | Purpose | Typical payload fields |
|---|---|---|
| `presence` | Publish online or away status. | `status`, `userId` |
| `typing` | Notify conversation participants that a user is typing. | `conversationId`, `isTyping` |
| `message` | Broadcast newly created chat messages. | `conversationId`, `messageId`, `senderId` |
| `notification` | Send realtime notification payloads. | `userId`, `payload` |
| `call.offer` | Forward call offer signaling data. | `callId`, `calleeId`, `sdp` |
| `call.answer` | Forward answer signaling data. | `callId`, `callerId`, `sdp` |
| `call.ice` | Forward ICE candidates. | `callId`, `targetUserId`, `candidate` |

## Database Model

The database model is intentionally compact, but it covers the core product surface. The schema includes users, follows, blocks, posts, stories, likes, saves, comments, conversations, conversation members, messages, reactions, calls, notifications, device tokens, bot rules, bot logs, and uploads.

| Table | Purpose |
|---|---|
| `users` | Profile, identity mirror, counters, location, and role fields. |
| `follows` and `blocks` | Social graph and safety controls. |
| `posts`, `likes`, `saves`, `comments` | Feed and interaction primitives. |
| `stories` | Ephemeral media with expiration and viewer state. |
| `conversations`, `conversation_members`, `messages`, `message_reactions` | Chat and group messaging. |
| `calls` | WebRTC signaling sessions, SDP data, ICE candidates, and status. |
| `notifications`, `device_tokens` | Notification persistence and push token registry. |
| `bot_rules`, `bot_logs` | Event-driven bot automation and audit logging. |
| `uploads` | Uploaded media metadata. |

## Bot Engine and Workers

Application events are published through RabbitMQ so bot rules and background jobs can be decoupled from the request path. The included bot engine reads events, evaluates enabled rules, executes configured actions, records outcomes, and provides a foundation for anti-spam, moderation, and automation workflows.

Workers are prepared for recurring maintenance concerns such as story expiration, analytics aggregation hooks, and moderation hooks. In a production deployment, these workers can run inside the same API process for small deployments or be split into a dedicated worker process by reusing the same service classes and RabbitMQ event bus.

## Build and Test Commands

Use these commands during development and CI.

```bash
gradle compileKotlin --no-daemon --console=plain
gradle test --no-daemon --console=plain
gradle installDist --no-daemon --console=plain
```

The generated application distribution is created under:

```text
build/install/social-platform-backend/
```

## Production Notes

Before production release, replace all local defaults in `.env`, configure a real Supabase project, restrict CORS origins, enable TLS at your ingress layer, configure secure RabbitMQ and Redis credentials, rotate JWT and service-role secrets, and point storage uploads to a private bucket policy appropriate for your client application. The generated backend is a verified implementation foundation; final hardening should follow your hosting provider, compliance, observability, and mobile/web client requirements.

## References

[1]: https://ktor.io/docs/server-create-a-new-project.html "Ktor server documentation"  
[2]: https://insert-koin.io/docs/reference/koin-ktor/ktor/ "Koin for Ktor documentation"  
[3]: https://www.jetbrains.com/help/exposed/home.html "JetBrains Exposed documentation"  
[4]: https://supabase.com/docs/guides/auth "Supabase Auth documentation"  
[5]: https://supabase.com/docs/guides/storage "Supabase Storage documentation"  
[6]: https://www.rabbitmq.com/tutorials "RabbitMQ tutorials"  
[7]: https://redis.io/docs/latest/ "Redis documentation"
