package com.socialplatform.realtime

import com.socialplatform.cache.RedisCache
import com.socialplatform.config.AppConfig
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.concurrent.ConcurrentHashMap
import kotlin.time.Duration.Companion.minutes

class WebSocketHub(private val config: AppConfig, private val cache: RedisCache) {
    private val sessions = ConcurrentHashMap<String, MutableSet<DefaultWebSocketServerSession>>()

    suspend fun register(userId: String, session: DefaultWebSocketServerSession) {
        sessions.computeIfAbsent(userId) { ConcurrentHashMap.newKeySet() }.add(session)
        cache.set("presence:$userId", "online", 2.minutes)
    }

    fun unregister(userId: String, session: DefaultWebSocketServerSession) {
        sessions[userId]?.remove(session)
        if (sessions[userId].isNullOrEmpty()) cache.del("presence:$userId")
    }

    suspend fun sendToUser(userId: String, event: String, payload: String) {
        val text = Json.encodeToString(mapOf("event" to event, "payload" to payload))
        sessions[userId]?.forEach { runCatching { it.send(Frame.Text(text)) } }
    }

    fun broadcastConversation(conversationId: String, event: String, payload: String) {
        cache.publish("conversation:$conversationId", Json.encodeToString(mapOf("event" to event, "payload" to payload)))
    }
}
