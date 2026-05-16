package com.socialplatform.api

import com.socialplatform.auth.authRoutes
import com.socialplatform.calls.callRoutes
import com.socialplatform.chat.chatRoutes
import com.socialplatform.notifications.notificationRoutes
import com.socialplatform.posts.postRoutes
import com.socialplatform.realtime.realtimeRoutes
import com.socialplatform.upload.uploadRoutes
import com.socialplatform.users.userRoutes
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.ratelimit.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    routing {
        rateLimit(RateLimitName("api")) {
            get("/health") { call.respond(mapOf("status" to "ok", "service" to "social-platform-backend")) }
            route("/api/v1") {
                authRoutes(); userRoutes(); postRoutes(); chatRoutes(); callRoutes(); uploadRoutes(); notificationRoutes(); realtimeRoutes()
            }
        }
    }
}
