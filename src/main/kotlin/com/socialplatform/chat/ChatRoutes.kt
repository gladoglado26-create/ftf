package com.socialplatform.chat

import com.socialplatform.api.*
import com.socialplatform.security.userId
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import java.util.UUID

fun Route.chatRoutes() {
    val service by inject<ChatService>()
    authenticate("auth-jwt") {
        route("/chat") {
            get("/conversations") { call.respond(ApiResponse(service.conversations(call.userId()))) }
            post("/conversations") { call.respond(ApiResponse(IdResponse(service.create(call.userId(), call.receive())))) }
            get("/conversations/{id}/messages") { call.respond(ApiResponse(service.messages(UUID.fromString(call.parameters["id"]), call.request.queryParameters["before"]))) }
            post("/conversations/{id}/messages") { call.respond(ApiResponse(IdResponse(service.send(call.userId(), UUID.fromString(call.parameters["id"]), call.receive())))) }
            post("/conversations/{id}/typing") { service.typing(call.userId(), UUID.fromString(call.parameters["id"]), true); call.respond(ApiResponse(MessageResponse("typing"))) }
            patch("/messages/{id}") { service.edit(call.userId(), UUID.fromString(call.parameters["id"]), call.receive()); call.respond(ApiResponse(MessageResponse("edited"))) }
            delete("/messages/{id}") { service.delete(call.userId(), UUID.fromString(call.parameters["id"])); call.respond(ApiResponse(MessageResponse("deleted"))) }
            post("/messages/{id}/react") { service.react(call.userId(), UUID.fromString(call.parameters["id"]), call.receive()); call.respond(ApiResponse(MessageResponse("reacted"))) }
        }
    }
}
