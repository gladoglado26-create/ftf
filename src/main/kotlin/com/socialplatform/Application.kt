package com.socialplatform

import com.socialplatform.api.configureRouting
import com.socialplatform.config.AppConfig
import com.socialplatform.config.configureKoin
import com.socialplatform.database.DatabaseFactory
import com.socialplatform.middleware.configureHTTP
import com.socialplatform.middleware.configureMonitoring
import com.socialplatform.middleware.configureSecurity
import com.socialplatform.realtime.configureRealtime
import com.socialplatform.workers.startWorkers
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import kotlinx.serialization.json.Json

fun main(args: Array<String>): Unit = EngineMain.main(args)

fun Application.module() {
    val config = AppConfig.fromEnv()
    configureKoin(config)
    DatabaseFactory.init(config.database)
    install(ContentNegotiation) {
        json(Json { ignoreUnknownKeys = true; explicitNulls = false; prettyPrint = false })
    }
    configureMonitoring()
    configureHTTP(config)
    configureSecurity(config)
    configureRealtime(config)
    configureRouting()
    startWorkers()
}
