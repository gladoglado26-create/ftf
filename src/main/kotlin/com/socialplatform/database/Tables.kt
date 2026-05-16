package com.socialplatform.database

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestamp

object Users : Table("users") {
    val id = uuid("id")
    val username = varchar("username", 50).uniqueIndex()
    val displayName = varchar("display_name", 100).nullable()
    val bio = text("bio").nullable()
    val avatarUrl = text("avatar_url").nullable()
    val role = varchar("role", 30).default("user")
    val verified = bool("verified").default(false)
    val lat = double("lat").nullable()
    val lng = double("lng").nullable()
    val followersCount = integer("followers_count").default(0)
    val followingCount = integer("following_count").default(0)
    val createdAt = timestamp("created_at")
    override val primaryKey = PrimaryKey(id)
}
object Follows : Table("follows") { val followerId = uuid("follower_id"); val followeeId = uuid("followee_id"); val createdAt = timestamp("created_at"); override val primaryKey = PrimaryKey(followerId, followeeId) }
object Blocks : Table("blocks") { val blockerId = uuid("blocker_id"); val blockedId = uuid("blocked_id"); val createdAt = timestamp("created_at"); override val primaryKey = PrimaryKey(blockerId, blockedId) }
object Posts : Table("posts") { val id = uuid("id"); val userId = uuid("user_id"); val caption = text("caption").nullable(); val mediaUrls = text("media_urls").default("[]"); val hashtags = text("hashtags").default("[]"); val location = text("location").nullable(); val archived = bool("archived").default(false); val deletedAt = timestamp("deleted_at").nullable(); val createdAt = timestamp("created_at"); override val primaryKey = PrimaryKey(id) }
object Stories : Table("stories") { val id = uuid("id"); val userId = uuid("user_id"); val mediaUrl = text("media_url"); val viewedBy = text("viewed_by").default("[]"); val expiresAt = timestamp("expires_at"); val createdAt = timestamp("created_at"); override val primaryKey = PrimaryKey(id) }
object Likes : Table("likes") { val userId = uuid("user_id"); val postId = uuid("post_id"); val createdAt = timestamp("created_at"); override val primaryKey = PrimaryKey(userId, postId) }
object Saves : Table("saves") { val userId = uuid("user_id"); val postId = uuid("post_id"); val createdAt = timestamp("created_at"); override val primaryKey = PrimaryKey(userId, postId) }
object Comments : Table("comments") { val id = uuid("id"); val postId = uuid("post_id"); val userId = uuid("user_id"); val parentId = uuid("parent_id").nullable(); val body = text("body"); val deletedAt = timestamp("deleted_at").nullable(); val createdAt = timestamp("created_at"); override val primaryKey = PrimaryKey(id) }
object Conversations : Table("conversations") { val id = uuid("id"); val title = varchar("title", 120).nullable(); val isGroup = bool("is_group").default(false); val createdBy = uuid("created_by"); val createdAt = timestamp("created_at"); override val primaryKey = PrimaryKey(id) }
object ConversationMembers : Table("conversation_members") { val conversationId = uuid("conversation_id"); val userId = uuid("user_id"); val role = varchar("role", 30).default("member"); val joinedAt = timestamp("joined_at"); override val primaryKey = PrimaryKey(conversationId, userId) }
object Messages : Table("messages") { val id = uuid("id"); val conversationId = uuid("conversation_id"); val senderId = uuid("sender_id"); val replyToId = uuid("reply_to_id").nullable(); val body = text("body").nullable(); val mediaUrls = text("media_urls").default("[]"); val editedAt = timestamp("edited_at").nullable(); val deletedAt = timestamp("deleted_at").nullable(); val createdAt = timestamp("created_at"); override val primaryKey = PrimaryKey(id) }
object MessageReactions : Table("message_reactions") { val messageId = uuid("message_id"); val userId = uuid("user_id"); val reaction = varchar("reaction", 32); val createdAt = timestamp("created_at"); override val primaryKey = PrimaryKey(messageId, userId, reaction) }
object Calls : Table("calls") { val id = uuid("id"); val callerId = uuid("caller_id"); val calleeId = uuid("callee_id"); val type = varchar("type", 10); val status = varchar("status", 20); val sdpOffer = text("sdp_offer").nullable(); val sdpAnswer = text("sdp_answer").nullable(); val iceCandidates = text("ice_candidates").default("[]"); val createdAt = timestamp("created_at"); val endedAt = timestamp("ended_at").nullable(); override val primaryKey = PrimaryKey(id) }
object Notifications : Table("notifications") { val id = uuid("id"); val userId = uuid("user_id"); val type = varchar("type", 80); val payload = text("payload"); val read = bool("read").default(false); val createdAt = timestamp("created_at"); override val primaryKey = PrimaryKey(id) }
object DeviceTokens : Table("device_tokens") { val userId = uuid("user_id"); val token = text("token"); val platform = varchar("platform", 20); val createdAt = timestamp("created_at"); override val primaryKey = PrimaryKey(userId, token) }
object BotRules : Table("bot_rules") { val id = uuid("id"); val ownerId = uuid("owner_id"); val event = varchar("event", 80); val enabled = bool("enabled").default(true); val conditions = text("conditions").default("{}"); val actions = text("actions").default("{}"); val createdAt = timestamp("created_at"); override val primaryKey = PrimaryKey(id) }
object BotLogs : Table("bot_logs") { val id = uuid("id"); val ruleId = uuid("rule_id").nullable(); val event = varchar("event", 80); val outcome = varchar("outcome", 80); val detail = text("detail").nullable(); val createdAt = timestamp("created_at"); override val primaryKey = PrimaryKey(id) }
object Uploads : Table("uploads") { val id = uuid("id"); val userId = uuid("user_id"); val url = text("url"); val mime = varchar("mime", 120); val kind = varchar("kind", 30); val createdAt = timestamp("created_at"); override val primaryKey = PrimaryKey(id) }
