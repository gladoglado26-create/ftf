package com.socialplatform.middleware

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.socialplatform.config.AppConfig
import com.socialplatform.security.UserPrincipal
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import java.util.UUID

fun Application.configureSecurity(config: AppConfig) {
    install(Authentication) {
        jwt("auth-jwt") {
            realm = "social-platform"
            verifier(
                JWT.require(Algorithm.HMAC256(config.supabase.jwtSecret))
                    .withAudience(config.security.jwtAudience)
                    .build()
            )
            validate { credential ->
                val subject = credential.payload.subject ?: return@validate null
                val role = credential.payload.getClaim("role").asString() ?: "user"
                UserPrincipal(UUID.fromString(subject), credential.payload.getClaim("email").asString(), role)
            }
        }
    }
    install(StatusPages) {
        exception<Throwable> { call, cause ->
            val status = when (cause) {
                is IllegalArgumentException -> HttpStatusCode.BadRequest
                is IllegalAccessException -> HttpStatusCode.Forbidden
                else -> HttpStatusCode.InternalServerError
            }
            call.respond(status, mapOf("error" to (cause.message ?: "Unexpected error")))
        }
        status(HttpStatusCode.Unauthorized) { call, _ -> call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Unauthorized")) }
    }
}
