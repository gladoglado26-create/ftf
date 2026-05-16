package com.socialplatform.notifications

import com.socialplatform.api.PushTokenRequest
import com.socialplatform.config.AppConfig
import com.socialplatform.database.DeviceTokens
import com.socialplatform.database.Notifications
import com.socialplatform.events.EventBus
import com.socialplatform.realtime.WebSocketHub
import com.socialplatform.services.dbQuery
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.time.Instant
import java.util.UUID

class NotificationService(private val config: AppConfig, private val events: EventBus, private val hub: WebSocketHub) {
    suspend fun list(user:UUID)= dbQuery { Notifications.select { Notifications.userId eq user }.orderBy(Notifications.createdAt, SortOrder.DESC).limit(50).map { mapOf("id" to it[Notifications.id].toString(), "type" to it[Notifications.type], "payload" to it[Notifications.payload], "read" to it[Notifications.read].toString(), "createdAt" to it[Notifications.createdAt].toString()) } }
    suspend fun readAll(user:UUID)= dbQuery { Notifications.update({ Notifications.userId eq user }) { it[read]=true } }
    suspend fun token(user:UUID, req:PushTokenRequest)= dbQuery { DeviceTokens.insertIgnore { it[userId]=user; it[token]=req.token; it[platform]=req.platform; it[createdAt]=Instant.now() } }
    suspend fun create(user:UUID, type:String, payload:String) { val id = UUID.randomUUID(); dbQuery { Notifications.insert { it[Notifications.id]=id; it[userId]=user; it[Notifications.type]=type; it[Notifications.payload]=payload; it[createdAt]=Instant.now() } }; hub.sendToUser(user.toString(), "notification.created", payload); events.publish("notification.created", user.toString(), payload) }
}
