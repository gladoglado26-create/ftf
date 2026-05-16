package com.socialplatform.realtime

import com.socialplatform.config.AppConfig
import com.socialplatform.security.UserPrincipal
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.koin.ktor.ext.inject
import java.time.Duration

fun Application.configureRealtime(config: AppConfig) {
    install(WebSockets) { pingPeriod = Duration.ofSeconds(20); timeout = Duration.ofSeconds(45); maxFrameSize = Long.MAX_VALUE; masking = false }
}

fun Route.realtimeRoutes() {
    val hub by inject<WebSocketHub>()
    authenticate("auth-jwt") {
        webSocket("/ws") {
            val principal = call.principal<UserPrincipal>() ?: return@webSocket close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "Unauthorized"))
            val userId = principal.id.toString()
            hub.register(userId, this)
            try {
                for (frame in incoming) if (frame is Frame.Text) {
                    send(Frame.Text(Json.encodeToString(mapOf("event" to "ack", "payload" to frame.readText()))))
                }
            } finally { hub.unregister(userId, this) }
        }
    }
}
