package com.socialplatform.notifications

import com.socialplatform.api.*
import com.socialplatform.security.userId
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Route.notificationRoutes() {
    val service by inject<NotificationService>()
    authenticate("auth-jwt") {
        route("/notifications") {
            get { call.respond(ApiResponse(service.list(call.userId()))) }
            post("/read-all") { service.readAll(call.userId()); call.respond(ApiResponse(MessageResponse("read"))) }
            post("/token") { service.token(call.userId(), call.receive()); call.respond(ApiResponse(MessageResponse("registered"))) }
        }
    }
}
