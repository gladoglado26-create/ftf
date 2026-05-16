package com.socialplatform.calls

import com.socialplatform.api.*
import com.socialplatform.security.userId
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import java.util.UUID

fun Route.callRoutes() {
    val service by inject<CallService>()
    authenticate("auth-jwt") {
        route("/calls") {
            post("/initiate") { call.respond(ApiResponse(IdResponse(service.initiate(call.userId(), call.receive())))) }
            post("/answer") { service.answer(call.userId(), call.receive()); call.respond(ApiResponse(MessageResponse("answered"))) }
            post("/ice-candidate") { service.ice(call.userId(), call.receive()); call.respond(ApiResponse(MessageResponse("candidate accepted"))) }
            post("/end") { val body=call.receive<Map<String,String>>(); service.end(call.userId(), UUID.fromString(body["callId"])); call.respond(ApiResponse(MessageResponse("ended"))) }
        }
    }
}
