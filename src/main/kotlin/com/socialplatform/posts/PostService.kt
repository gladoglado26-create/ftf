package com.socialplatform.posts

import com.socialplatform.api.CommentCreateRequest
import com.socialplatform.api.PostCreateRequest
import com.socialplatform.cache.RedisCache
import com.socialplatform.database.*
import com.socialplatform.events.EventBus
import com.socialplatform.services.dbQuery
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.lessEq
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID
import kotlin.time.Duration.Companion.minutes

class PostService(private val cache: RedisCache, private val events: EventBus) {
    private fun postRow(r: ResultRow) = mapOf("id" to r[Posts.id].toString(), "userId" to r[Posts.userId].toString(), "caption" to r[Posts.caption], "mediaUrls" to r[Posts.mediaUrls], "hashtags" to r[Posts.hashtags], "location" to r[Posts.location], "createdAt" to r[Posts.createdAt].toString())
    suspend fun feed(page: Int) = dbQuery { val key = "feed:$page"; cache.get(key)?.let { return@dbQuery Json.decodeFromString<List<Map<String, String?>>>(it) }; Posts.select { Posts.deletedAt.isNull() and (Posts.archived eq false) }.orderBy(Posts.createdAt, SortOrder.DESC).limit(25, ((page - 1) * 25).toLong()).map { postRow(it) }.also { cache.set(key, Json.encodeToString(it), 2.minutes) } }
    suspend fun reels() = dbQuery { Posts.select { (Posts.deletedAt.isNull()) and (Posts.mediaUrls neq "[]") }.orderBy(Posts.createdAt, SortOrder.DESC).limit(25).map { postRow(it) } }
    suspend fun create(user: UUID, req: PostCreateRequest) = dbQuery { val id = UUID.randomUUID(); Posts.insert { it[Posts.id] = id; it[userId] = user; it[caption] = req.caption; it[mediaUrls] = Json.encodeToString(req.mediaUrls); it[hashtags] = Json.encodeToString(req.hashtags); it[location] = req.location; it[createdAt] = Instant.now() }; cache.del("feed:1"); events.publish("post.created", user.toString(), Json.encodeToString(mapOf("postId" to id.toString()))); id.toString() }
    suspend fun get(id: UUID) = dbQuery { Posts.select { Posts.id eq id }.singleOrNull()?.let { postRow(it) } ?: error("Post not found") }
    suspend fun patch(id: UUID, user: UUID, req: PostCreateRequest) = dbQuery { Posts.update({ (Posts.id eq id) and (Posts.userId eq user) }) { it[caption] = req.caption; it[mediaUrls] = Json.encodeToString(req.mediaUrls); it[hashtags] = Json.encodeToString(req.hashtags); it[location] = req.location }; cache.del("feed:1") }
    suspend fun delete(id: UUID, user: UUID) = dbQuery { Posts.update({ (Posts.id eq id) and (Posts.userId eq user) }) { it[deletedAt] = Instant.now() }; cache.del("feed:1") }
    suspend fun stories() = dbQuery { Stories.select { Stories.expiresAt greater Instant.now() }.orderBy(Stories.createdAt, SortOrder.DESC).map { mapOf("id" to it[Stories.id].toString(), "userId" to it[Stories.userId].toString(), "mediaUrl" to it[Stories.mediaUrl], "expiresAt" to it[Stories.expiresAt].toString()) } }
    suspend fun createStory(user: UUID, mediaUrl: String) = dbQuery { val id = UUID.randomUUID(); Stories.insert { it[Stories.id] = id; it[userId] = user; it[Stories.mediaUrl] = mediaUrl; it[expiresAt] = Instant.now().plus(24, ChronoUnit.HOURS); it[createdAt] = Instant.now() }; events.publish("story.posted", user.toString(), Json.encodeToString(mapOf("storyId" to id.toString()))); id.toString() }
    suspend fun viewStory(user: UUID, id: UUID) = dbQuery { val row = Stories.select { Stories.id eq id }.singleOrNull() ?: error("Story not found"); val viewers = Json.decodeFromString<List<String>>(row[Stories.viewedBy]).toMutableSet(); viewers.add(user.toString()); Stories.update({ Stories.id eq id }) { it[viewedBy] = Json.encodeToString(viewers.toList()) } }
    suspend fun like(user: UUID, post: UUID) = dbQuery { Likes.insertIgnore { it[userId] = user; it[postId] = post; it[createdAt] = Instant.now() }; events.publish("post.liked", user.toString(), Json.encodeToString(mapOf("postId" to post.toString()))) }
    suspend fun unlike(user: UUID, post: UUID) = dbQuery { Likes.deleteWhere { (userId eq user) and (postId eq post) } }
    suspend fun save(user: UUID, post: UUID) = dbQuery { Saves.insertIgnore { it[userId] = user; it[postId] = post; it[createdAt] = Instant.now() } }
    suspend fun unsave(user: UUID, post: UUID) = dbQuery { Saves.deleteWhere { (userId eq user) and (postId eq post) } }
    suspend fun comments(post: UUID) = dbQuery { Comments.select { Comments.postId eq post }.orderBy(Comments.createdAt).map { mapOf("id" to it[Comments.id].toString(), "userId" to it[Comments.userId].toString(), "parentId" to it[Comments.parentId]?.toString(), "body" to it[Comments.body], "createdAt" to it[Comments.createdAt].toString()) } }
    suspend fun addComment(user: UUID, post: UUID, req: CommentCreateRequest) = dbQuery { val id = UUID.randomUUID(); Comments.insert { it[Comments.id] = id; it[postId] = post; it[userId] = user; it[parentId] = req.parentId?.let(UUID::fromString); it[body] = req.body; it[createdAt] = Instant.now() }; events.publish("comment.created", user.toString(), Json.encodeToString(mapOf("commentId" to id.toString(), "postId" to post.toString()))); id.toString() }
    suspend fun deleteComment(user: UUID, id: UUID) = dbQuery { Comments.update({ (Comments.id eq id) and (Comments.userId eq user) }) { it[deletedAt] = Instant.now() } }
    suspend fun expireStories() = dbQuery { Stories.deleteWhere { Stories.expiresAt lessEq Instant.now() } }
}
