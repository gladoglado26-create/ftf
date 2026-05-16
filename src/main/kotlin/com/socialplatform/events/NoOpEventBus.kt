package com.socialplatform.events

import com.socialplatform.config.AppConfig

class NoOpEventBus : EventBus(config = AppConfig.Rabbit(uri = "", exchange = "")) {
    override fun publish(name: String, actorId: String?, payload: String) {}
    override fun consume(queue: String, bindingKeys: List<String>, handler: (DomainEvent) -> Unit) {}
}
