package com.socialplatform.users

import com.socialplatform.api.ProfilePatchRequest
import com.socialplatform.cache.RedisCache
import com.socialplatform.database.*
import com.socialplatform.events.EventBus
import com.socialplatform.services.dbQuery
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.time.Instant
import java.util.UUID
import kotlin.math.*
import kotlin.time.Duration.Companion.minutes

class UserService(private val cache: RedisCache, private val events: EventBus) {
    private fun rowToMap(r: ResultRow) = mapOf(
        "id" to r[Users.id].toString(), "username" to r[Users.username], "displayName" to r[Users.displayName],
        "bio" to r[Users.bio], "avatarUrl" to r[Users.avatarUrl], "role" to r[Users.role],
        "verified" to r[Users.verified].toString(), "followersCount" to r[Users.followersCount].toString(),
        "followingCount" to r[Users.followingCount].toString()
    )

    suspend fun me(id: UUID) = get(id)

    suspend fun get(id: UUID): Map<String, String?> = dbQuery {
        val key = "profile:$id"
        cache.get(key)?.let { return@dbQuery Json.decodeFromString<Map<String, String?>>(it) }
        val row = Users.select { Users.id eq id }.singleOrNull() ?: error("User not found")
        rowToMap(row).also { cache.set(key, Json.encodeToString(it), 5.minutes) }
    }

    suspend fun upsertMe(id: UUID, req: ProfilePatchRequest): Map<String, String?> {
        dbQuery {
            val exists = Users.select { Users.id eq id }.count() > 0
            if (!exists) Users.insert {
                it[Users.id] = id; it[username] = req.username ?: "user_${id.toString().take(8)}"; it[createdAt] = Instant.now()
                it[displayName] = req.displayName; it[bio] = req.bio; it[avatarUrl] = req.avatarUrl; it[lat] = req.lat; it[lng] = req.lng
            } else Users.update({ Users.id eq id }) {
                req.username?.let { v -> it[username] = v }; req.displayName?.let { v -> it[displayName] = v }; req.bio?.let { v -> it[bio] = v }
                req.avatarUrl?.let { v -> it[avatarUrl] = v }; req.lat?.let { v -> it[lat] = v }; req.lng?.let { v -> it[lng] = v }
            }
            cache.del("profile:$id")
            events.publish("user.updated", id.toString())
        }
        return get(id)
    }

    suspend fun search(q: String) = dbQuery { Users.select { Users.username like "%$q%" }.limit(25).map { rowToMap(it) } }
    suspend fun nearby(lat: Double, lng: Double, radiusKm: Double) = dbQuery {
        Users.selectAll().mapNotNull { r ->
            val a = r[Users.lat]; val b = r[Users.lng]
            if (a == null || b == null) null else {
                val d = haversine(lat, lng, a, b)
                if (d <= radiusKm) rowToMap(r) + ("distanceKm" to "%.2f".format(d)) else null
            }
        }
    }
    private fun haversine(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val radius = 6371.0; val dLat = Math.toRadians(lat2 - lat1); val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2).pow(2) + cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLon / 2).pow(2)
        return 2 * radius * asin(sqrt(a))
    }
    suspend fun follow(actor: UUID, target: UUID) = dbQuery {
        Follows.insertIgnore { it[followerId] = actor; it[followeeId] = target; it[createdAt] = Instant.now() }
        Users.update({ Users.id eq target }) { with(SqlExpressionBuilder) { it.update(followersCount, followersCount + 1) } }
        Users.update({ Users.id eq actor }) { with(SqlExpressionBuilder) { it.update(followingCount, followingCount + 1) } }
        events.publish("user.followed", actor.toString(), Json.encodeToString(mapOf("targetId" to target.toString())))
    }
    suspend fun unfollow(actor: UUID, target: UUID) = dbQuery { Follows.deleteWhere { (followerId eq actor) and (followeeId eq target) }; events.publish("user.unfollowed", actor.toString()) }
    suspend fun followers(id: UUID) = dbQuery { Follows.select { Follows.followeeId eq id }.map { it[Follows.followerId].toString() } }
    suspend fun block(actor: UUID, target: UUID) = dbQuery { Blocks.insertIgnore { it[blockerId] = actor; it[blockedId] = target; it[createdAt] = Instant.now() }; events.publish("user.blocked", actor.toString()) }
    suspend fun unblock(actor: UUID, target: UUID) = dbQuery { Blocks.deleteWhere { (blockerId eq actor) and (blockedId eq target) } }
    suspend fun blocked(actor: UUID) = dbQuery { Blocks.select { Blocks.blockerId eq actor }.map { it[Blocks.blockedId].toString() } }
    fun setOnline(id: UUID) = cache.set("presence:$id", "online", 2.minutes)
}
