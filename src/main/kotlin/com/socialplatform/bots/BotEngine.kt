package com.socialplatform.bots

import com.socialplatform.cache.RedisCache
import com.socialplatform.database.BotLogs
import com.socialplatform.database.BotRules
import com.socialplatform.events.DomainEvent
import com.socialplatform.events.EventBus
import com.socialplatform.notifications.NotificationService
import com.socialplatform.services.dbQuery
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jetbrains.exposed.sql.*
import java.time.Instant
import java.util.UUID

class BotEngine(private val events: EventBus, private val cache: RedisCache, private val notifications: NotificationService) {
    fun start(scope: CoroutineScope) {
        scope.launch(Dispatchers.IO) {
            events.consume("bot-engine", listOf("message.sent", "comment.created", "post.created", "user.followed", "story.posted")) { event ->
                scope.launch { handle(event) }
            }
        }
    }
    suspend fun handle(event: DomainEvent) {
        val key = "bot-rate:${event.actorId}:${event.name}"
        if (cache.incr(key) > 50) { log(null, event.name, "rate_limited", "Anti-spam triggered"); return }
        val rules = dbQuery { BotRules.select { (BotRules.event eq event.name.uppercase().replace('.', '_')) and (BotRules.enabled eq true) }.toList() }
        rules.forEach { rule -> log(rule[BotRules.id], event.name, "executed", "Rule evaluated with event ${event.id}") }
    }
    private suspend fun log(ruleId: UUID?, event:String, outcome:String, detail:String) = dbQuery { BotLogs.insert { it[id]=UUID.randomUUID(); it[BotLogs.ruleId]=ruleId; it[BotLogs.event]=event; it[BotLogs.outcome]=outcome; it[BotLogs.detail]=detail; it[createdAt]=Instant.now() } }
}
