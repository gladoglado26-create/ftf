package com.socialplatform.middleware

import io.ktor.server.application.*
import io.ktor.server.plugins.callid.*
import io.ktor.server.plugins.callloging.*
import org.slf4j.event.Level
import java.util.UUID

fun Application.configureMonitoring() {
    install(CallId) { generate { UUID.randomUUID().toString() }; verify { it.isNotBlank() } }
    install(CallLogging) { level = Level.INFO; callIdMdc("trace_id") }
}
