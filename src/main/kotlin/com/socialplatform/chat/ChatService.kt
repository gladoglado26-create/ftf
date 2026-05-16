package com.socialplatform.chat

import com.socialplatform.api.ConversationCreateRequest
import com.socialplatform.api.MessageCreateRequest
import com.socialplatform.cache.RedisCache
import com.socialplatform.database.*
import com.socialplatform.events.EventBus
import com.socialplatform.realtime.WebSocketHub
import com.socialplatform.services.dbQuery
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.time.Instant
import java.util.UUID
import kotlin.time.Duration.Companion.minutes

class ChatService(private val cache: RedisCache, private val events: EventBus, private val hub: WebSocketHub) {
    suspend fun conversations(user: UUID) = dbQuery { ConversationMembers.select { ConversationMembers.userId eq user }.map { it[ConversationMembers.conversationId].toString() } }
    suspend fun create(user: UUID, req: ConversationCreateRequest) = dbQuery {
        val id = UUID.randomUUID(); Conversations.insert { it[Conversations.id] = id; it[title] = req.title; it[isGroup] = req.isGroup; it[createdBy] = user; it[createdAt] = Instant.now() }
        (req.memberIds.map(UUID::fromString) + user).distinct().forEach { uid -> ConversationMembers.insertIgnore { it[conversationId] = id; it[userId] = uid; it[joinedAt] = Instant.now() } }
        events.publish("conversation.created", user.toString(), Json.encodeToString(mapOf("conversationId" to id.toString()))); id.toString()
    }
    suspend fun messages(cid: UUID, before: String?) = dbQuery { Messages.select { Messages.conversationId eq cid }.orderBy(Messages.createdAt, SortOrder.DESC).limit(50).map { msg(it) } }
    private fun msg(r: ResultRow) = mapOf("id" to r[Messages.id].toString(), "conversationId" to r[Messages.conversationId].toString(), "senderId" to r[Messages.senderId].toString(), "body" to r[Messages.body], "mediaUrls" to r[Messages.mediaUrls], "createdAt" to r[Messages.createdAt].toString(), "editedAt" to r[Messages.editedAt]?.toString())
    suspend fun send(user: UUID, cid: UUID, req: MessageCreateRequest) = dbQuery {
        val id = UUID.randomUUID(); Messages.insert { it[Messages.id] = id; it[conversationId] = cid; it[senderId] = user; it[replyToId] = req.replyToId?.let(UUID::fromString); it[body] = req.body; it[mediaUrls] = Json.encodeToString(req.mediaUrls); it[createdAt] = Instant.now() }
        cache.del("conversation:$cid"); events.publish("message.sent", user.toString(), Json.encodeToString(mapOf("messageId" to id.toString(), "conversationId" to cid.toString()))); id.toString()
    }.also { hub.broadcastConversation(cid.toString(), "message.sent", it) }
    suspend fun edit(user: UUID, id: UUID, body: String) = dbQuery { Messages.update({ (Messages.id eq id) and (Messages.senderId eq user) }) { it[Messages.body] = body; it[editedAt] = Instant.now() } }
    suspend fun delete(user: UUID, id: UUID) = dbQuery { Messages.update({ (Messages.id eq id) and (Messages.senderId eq user) }) { it[deletedAt] = Instant.now() } }
    suspend fun react(user: UUID, id: UUID, emoji: String) = dbQuery { MessageReactions.insertIgnore { it[messageId] = id; it[userId] = user; it[MessageReactions.reaction] = emoji; it[createdAt] = Instant.now() } }
    fun typing(user: UUID, cid: UUID, active: Boolean) { cache.set("typing:$cid:$user", active.toString(), 1.minutes); hub.broadcastConversation(cid.toString(), "typing", Json.encodeToString(mapOf("userId" to user.toString(), "active" to active.toString()))) }
}
