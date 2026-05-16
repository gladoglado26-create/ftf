package com.socialplatform.auth

import com.socialplatform.api.*
import com.socialplatform.cache.RedisCache
import com.socialplatform.config.AppConfig
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class AuthService(private val config: AppConfig, private val cache: RedisCache) {
    private val http = HttpClient(CIO) { install(ContentNegotiation) { json() } }
    private fun authUrl(path: String) = "${config.supabase.url}/auth/v1$path"
    private fun HttpRequestBuilder.supabaseHeaders(service: Boolean = false) {
        header("apikey", if (service) config.supabase.serviceRoleKey else config.supabase.anonKey)
        if (service) bearerAuth(config.supabase.serviceRoleKey)
    }
    suspend fun signup(req: AuthRequest): JsonObject = http.post(authUrl("/signup")) { supabaseHeaders(); contentType(ContentType.Application.Json); setBody(buildJsonObject { put("email", req.email); put("password", req.password) }) }.body()
    suspend fun login(req: AuthRequest): JsonObject = http.post(authUrl("/token?grant_type=password")) { supabaseHeaders(); contentType(ContentType.Application.Json); setBody(buildJsonObject { put("email", req.email); put("password", req.password) }) }.body()
    suspend fun verifyOtp(req: OtpRequest): JsonObject = http.post(authUrl("/verify")) { supabaseHeaders(); contentType(ContentType.Application.Json); setBody(buildJsonObject { put("email", req.email); put("token", req.token); put("type", "email") }) }.body()
    suspend fun resendOtp(req: ResetPasswordRequest): JsonObject = http.post(authUrl("/resend")) { supabaseHeaders(); contentType(ContentType.Application.Json); setBody(buildJsonObject { put("email", req.email); put("type", "signup") }) }.body()
    suspend fun resetPassword(req: ResetPasswordRequest): JsonObject = http.post(authUrl("/recover")) { supabaseHeaders(); contentType(ContentType.Application.Json); setBody(buildJsonObject { put("email", req.email) }) }.body()
    suspend fun logout(jwt: String): Boolean { cache.del("session:$jwt"); return http.post(authUrl("/logout")) { supabaseHeaders(); bearerAuth(jwt) }.status.isSuccess() }
    suspend fun patchPassword(jwt: String, req: PasswordPatchRequest): JsonObject = http.put(authUrl("/user")) { supabaseHeaders(); bearerAuth(jwt); contentType(ContentType.Application.Json); setBody(buildJsonObject { put("password", req.password) }) }.body()
}
