package com.socialplatform.events

import com.rabbitmq.client.CancelCallback
import com.rabbitmq.client.ConnectionFactory
import com.rabbitmq.client.DeliverCallback
import com.socialplatform.config.AppConfig
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.Instant
import java.util.UUID

@Serializable data class DomainEvent(val id: String = UUID.randomUUID().toString(), val name: String, val actorId: String? = null, val payload: String = "{}", val occurredAt: String = Instant.now().toString())

class EventBus(config: AppConfig.Rabbit) {
    private val exchange = config.exchange
    private val connection = runCatching { ConnectionFactory().apply { setUri(config.uri) }.newConnection("social-platform-api") }.getOrNull()
    fun publish(name: String, actorId: String? = null, payload: String = "{}") {
        val body = Json.encodeToString(DomainEvent(name = name, actorId = actorId, payload = payload)).toByteArray()
        runCatching {
            connection?.createChannel()?.use { ch ->
                ch.exchangeDeclare(exchange, "topic", true)
                ch.basicPublish(exchange, name, null, body)
            }
        }
    }
    fun consume(queue: String, bindingKeys: List<String>, handler: (DomainEvent) -> Unit) {
        val conn = connection ?: return
        val ch = conn.createChannel()
        ch.exchangeDeclare(exchange, "topic", true)
        ch.queueDeclare(queue, true, false, false, null)
        bindingKeys.forEach { ch.queueBind(queue, exchange, it) }
        val deliver = DeliverCallback { _, delivery -> handler(Json.decodeFromString(DomainEvent.serializer(), delivery.body.decodeToString())) }
        val cancel = CancelCallback { }
        ch.basicConsume(queue, true, deliver, cancel)
    }
}
