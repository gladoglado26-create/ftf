package com.socialplatform.auth

import com.socialplatform.api.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Route.authRoutes() {
    val service by inject<AuthService>()
    route("/auth") {
        post("/signup") { call.respond(ApiResponse(service.signup(call.receive()))) }
        post("/login") { call.respond(ApiResponse(service.login(call.receive()))) }
        post("/verify-otp") { call.respond(ApiResponse(service.verifyOtp(call.receive()))) }
        post("/resend-otp") { call.respond(ApiResponse(service.resendOtp(call.receive()))) }
        post("/reset-password") { call.respond(ApiResponse(service.resetPassword(call.receive()))) }
        authenticate("auth-jwt") {
            post("/logout") { call.respond(ApiResponse(MessageResponse(if (service.logout(call.bearer())) "logged out" else "logout requested"))) }
            patch("/password") { call.respond(ApiResponse(service.patchPassword(call.bearer(), call.receive()))) }
        }
    }
}
private fun ApplicationCall.bearer(): String = request.headers["Authorization"]?.removePrefix("Bearer ") ?: error("Missing bearer token")
