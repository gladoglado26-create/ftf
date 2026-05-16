package com.socialplatform.posts

import com.socialplatform.api.*
import com.socialplatform.security.userId
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import java.util.UUID

fun Route.postRoutes() {
    val service by inject<PostService>()
    authenticate("auth-jwt") {
        route("/posts") {
            get("/feed") { call.respond(ApiResponse(service.feed(call.request.queryParameters["page"]?.toInt() ?: 1))) }
            get("/reels") { call.respond(ApiResponse(service.reels())) }
            post { call.respond(ApiResponse(IdResponse(service.create(call.userId(), call.receive())))) }
            get("/{id}") { call.respond(ApiResponse(service.get(UUID.fromString(call.parameters["id"])))) }
            patch("/{id}") { service.patch(UUID.fromString(call.parameters["id"]), call.userId(), call.receive()); call.respond(ApiResponse(MessageResponse("updated"))) }
            delete("/{id}") { service.delete(UUID.fromString(call.parameters["id"]), call.userId()); call.respond(ApiResponse(MessageResponse("deleted"))) }
            post("/{id}/like") { service.like(call.userId(), UUID.fromString(call.parameters["id"])); call.respond(ApiResponse(MessageResponse("liked"))) }
            delete("/{id}/like") { service.unlike(call.userId(), UUID.fromString(call.parameters["id"])); call.respond(ApiResponse(MessageResponse("unliked"))) }
            post("/{id}/save") { service.save(call.userId(), UUID.fromString(call.parameters["id"])); call.respond(ApiResponse(MessageResponse("saved"))) }
            delete("/{id}/save") { service.unsave(call.userId(), UUID.fromString(call.parameters["id"])); call.respond(ApiResponse(MessageResponse("unsaved"))) }
            get("/{id}/comments") { call.respond(ApiResponse(service.comments(UUID.fromString(call.parameters["id"])))) }
            post("/{id}/comments") { call.respond(ApiResponse(IdResponse(service.addComment(call.userId(), UUID.fromString(call.parameters["id"]), call.receive())))) }
        }
        delete("/comments/{id}") { service.deleteComment(call.userId(), UUID.fromString(call.parameters["id"])); call.respond(ApiResponse(MessageResponse("deleted"))) }
        route("/stories") {
            get { call.respond(ApiResponse(service.stories())) }
            post { val body = call.receive<Map<String,String>>(); call.respond(ApiResponse(IdResponse(service.createStory(call.userId(), body["mediaUrl"] ?: error("mediaUrl required"))))) }
            post("/{id}/view") { service.viewStory(call.userId(), UUID.fromString(call.parameters["id"])); call.respond(ApiResponse(MessageResponse("viewed"))) }
        }
    }
}
