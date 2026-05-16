package com.socialplatform.calls

import com.socialplatform.api.CallAnswerRequest
import com.socialplatform.api.CallInitiateRequest
import com.socialplatform.api.IceCandidateRequest
import com.socialplatform.cache.RedisCache
import com.socialplatform.database.Calls
import com.socialplatform.events.EventBus
import com.socialplatform.services.dbQuery
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.time.Instant
import java.util.UUID
import kotlin.time.Duration.Companion.hours

class CallService(private val cache: RedisCache, private val events: EventBus) {
    suspend fun initiate(user: UUID, req: CallInitiateRequest) = dbQuery { val id = UUID.randomUUID(); Calls.insert { it[Calls.id] = id; it[callerId] = user; it[calleeId] = UUID.fromString(req.calleeId); it[type] = req.type; it[status] = "ringing"; it[sdpOffer] = req.sdpOffer; it[createdAt] = Instant.now() }; cache.set("call:$id", req.sdpOffer, 2.hours); events.publish("call.started", user.toString(), Json.encodeToString(mapOf("callId" to id.toString()))); id.toString() }
    suspend fun answer(user: UUID, req: CallAnswerRequest) = dbQuery { Calls.update({ Calls.id eq UUID.fromString(req.callId) }) { it[sdpAnswer] = req.sdpAnswer; it[status] = "active" }; events.publish("call.answered", user.toString(), Json.encodeToString(mapOf("callId" to req.callId))) }
    suspend fun ice(user: UUID, req: IceCandidateRequest) = dbQuery { val id = UUID.fromString(req.callId); val row = Calls.select { Calls.id eq id }.single(); val list = Json.decodeFromString<List<String>>(row[Calls.iceCandidates]).toMutableList(); list.add(req.candidate); Calls.update({ Calls.id eq id }) { it[iceCandidates] = Json.encodeToString(list) }; events.publish("call.ice", user.toString(), Json.encodeToString(mapOf("callId" to id.toString()))) }
    suspend fun end(user: UUID, id: UUID) = dbQuery { Calls.update({ Calls.id eq id }) { it[status] = "ended"; it[endedAt] = Instant.now() }; cache.del("call:$id"); events.publish("call.ended", user.toString(), Json.encodeToString(mapOf("callId" to id.toString()))) }
}
