package com.socialplatform.upload

import com.socialplatform.config.AppConfig
import com.socialplatform.database.Uploads
import com.socialplatform.events.EventBus
import com.socialplatform.services.dbQuery
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.insert
import java.time.Instant
import java.util.UUID

class UploadService(private val config: AppConfig, private val events: EventBus) {
    private val http = HttpClient(CIO)
    private val allowed = setOf("image/jpeg", "image/png", "image/webp", "video/mp4", "video/webm")

    suspend fun upload(user: UUID, bytes: ByteArray, name: String, mime: String, kind: String): String {
        require(mime in allowed) { "Unsupported MIME type" }
        val safeName = name.replace(Regex("[^A-Za-z0-9._-]"), "_")
        val path = "$kind/$user/${UUID.randomUUID()}-$safeName"
        runCatching {
            http.post("${config.supabase.url}/storage/v1/object/${config.supabase.storageBucket}/$path") {
                header("apikey", config.supabase.serviceRoleKey)
                bearerAuth(config.supabase.serviceRoleKey)
                contentType(ContentType.parse(mime))
                setBody(bytes)
            }
        }
        val publicUrl = "${config.supabase.url}/storage/v1/object/public/${config.supabase.storageBucket}/$path"
        dbQuery { Uploads.insert { it[id] = UUID.randomUUID(); it[userId] = user; it[url] = publicUrl; it[Uploads.mime] = mime; it[Uploads.kind] = kind; it[createdAt] = Instant.now() } }
        events.publish("media.uploaded", user.toString(), Json.encodeToString(mapOf("url" to publicUrl)))
        return publicUrl
    }
}
