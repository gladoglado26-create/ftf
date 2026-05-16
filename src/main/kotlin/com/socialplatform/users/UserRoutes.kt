package com.socialplatform.users

import com.socialplatform.api.*
import com.socialplatform.security.userId
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import java.util.UUID

fun Route.userRoutes() {
    val service by inject<UserService>()
    authenticate("auth-jwt") {
        route("/users") {
            get("/me") { call.respond(ApiResponse(service.me(call.userId()))) }
            patch("/me") { call.respond(ApiResponse(service.upsertMe(call.userId(), call.receive()))) }
            get("/search") { call.respond(ApiResponse(service.search(call.request.queryParameters["q"] ?: ""))) }
            get("/nearby") { call.respond(ApiResponse(service.nearby(call.parameters.qd("lat"), call.parameters.qd("lng"), call.parameters.qd("radius", 25.0)))) }
            get("/blocked") { call.respond(ApiResponse(service.blocked(call.userId()))) }
            get("/{id}") { call.respond(ApiResponse(service.get(UUID.fromString(call.parameters["id"])))) }
            post("/{id}/follow") { service.follow(call.userId(), UUID.fromString(call.parameters["id"])); call.respond(ApiResponse(MessageResponse("followed"))) }
            delete("/{id}/follow") { service.unfollow(call.userId(), UUID.fromString(call.parameters["id"])); call.respond(ApiResponse(MessageResponse("unfollowed"))) }
            get("/{id}/followers") { call.respond(ApiResponse(service.followers(UUID.fromString(call.parameters["id"])))) }
            post("/{id}/block") { service.block(call.userId(), UUID.fromString(call.parameters["id"])); call.respond(ApiResponse(MessageResponse("blocked"))) }
            delete("/{id}/block") { service.unblock(call.userId(), UUID.fromString(call.parameters["id"])); call.respond(ApiResponse(MessageResponse("unblocked"))) }
        }
    }
}
private fun Parameters.qd(name:String, default:Double?=null)=this[name]?.toDouble() ?: default ?: error("Missing $name")
