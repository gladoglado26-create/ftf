package com.socialplatform.middleware

import com.socialplatform.config.AppConfig
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.ratelimit.*
import kotlin.time.Duration.Companion.minutes

fun Application.configureHTTP(config: AppConfig) {
    install(CORS) {
        anyHost()
        allowHeader(HttpHeaders.Authorization)
        allowHeader(HttpHeaders.ContentType)
        allowMethod(HttpMethod.Get); allowMethod(HttpMethod.Post); allowMethod(HttpMethod.Patch); allowMethod(HttpMethod.Delete)
    }
    install(RateLimit) {
        register(RateLimitName("api")) { rateLimiter(limit = config.security.rateLimitPerMinute, refillPeriod = 1.minutes) }
    }
}
