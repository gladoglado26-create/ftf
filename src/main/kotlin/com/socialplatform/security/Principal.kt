package com.socialplatform.security

import io.ktor.server.auth.*
import java.util.UUID

data class UserPrincipal(val id: UUID, val email: String?, val role: String = "user") : Principal
