package com.socialplatform.security

import io.ktor.server.application.*
import io.ktor.server.auth.*
import java.util.UUID

fun ApplicationCall.userPrincipal(): UserPrincipal = principal<UserPrincipal>() ?: error("Missing authenticated principal")
fun ApplicationCall.userId(): UUID = userPrincipal().id
fun ApplicationCall.requireRole(vararg roles: String) {
    val role = userPrincipal().role
    if (role !in roles) throw IllegalAccessException("Requires one of roles: ${roles.joinToString()}")
}
