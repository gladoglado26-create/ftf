package com.socialplatform.upload

import com.socialplatform.api.*
import com.socialplatform.config.AppConfig
import com.socialplatform.security.userId
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Route.uploadRoutes() {
    val service by inject<UploadService>()
    val config by inject<AppConfig>()
    val http = HttpClient(CIO) { install(ContentNegotiation) { json() } }
    authenticate("auth-jwt") {
        route("/upload") {
            post("/avatar") { call.respond(ApiResponse(UploadResult(uploadOne(call, service, "avatar"), "image/*", "avatar"))) }
            post("/media") { call.respond(ApiResponse(UploadResult(uploadOne(call, service, "media"), "mixed", "media"))) }
        }
        get("/music/search") {
            val q = call.request.queryParameters["q"] ?: ""
            val data: String = http.get("https://api.jamendo.com/v3.0/tracks/") { parameter("client_id", config.jamendo.clientId); parameter("format", "json"); parameter("limit", 10); parameter("search", q) }.body()
            call.respond(ApiResponse(data))
        }
    }
}
private suspend fun uploadOne(call: ApplicationCall, service: UploadService, kind:String): String {
    val multipart = call.receiveMultipart(); var result: String? = null
    multipart.forEachPart { part ->
        if (part is PartData.FileItem) {
            val bytes = part.streamProvider().readBytes()
            result = service.upload(call.userId(), bytes, part.originalFileName ?: "upload.bin", part.contentType?.toString() ?: "application/octet-stream", kind)
        }
        part.dispose()
    }
    return result ?: error("file field required")
}
