package com.socialplatform.config

import com.socialplatform.auth.AuthService
import com.socialplatform.bots.BotEngine
import com.socialplatform.cache.RedisCache
import com.socialplatform.calls.CallService
import com.socialplatform.chat.ChatService
import com.socialplatform.events.EventBus
import com.socialplatform.notifications.NotificationService
import com.socialplatform.posts.PostService
import com.socialplatform.realtime.WebSocketHub
import com.socialplatform.upload.UploadService
import com.socialplatform.users.UserService
import io.ktor.server.application.*
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger

fun Application.configureKoin(config: AppConfig) {
    install(Koin) {
        slf4jLogger()
        modules(module {
            single { config }
            single { RedisCache(config.redis) }
            single { EventBus(config.rabbit) }
            single { WebSocketHub(get(), get()) }
            single { AuthService(config, get()) }
            single { UserService(get(), get()) }
            single { PostService(get(), get()) }
            single { ChatService(get(), get(), get()) }
            single { CallService(get(), get()) }
            single { UploadService(config, get()) }
            single { NotificationService(config, get(), get()) }
            single { BotEngine(get(), get(), get()) }
        })
    }
}
