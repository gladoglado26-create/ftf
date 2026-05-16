package com.socialplatform.api

import kotlinx.serialization.Serializable

@Serializable data class ApiResponse<T>(val data: T? = null, val error: String? = null)
@Serializable data class IdResponse(val id: String)
@Serializable data class MessageResponse(val message: String)
@Serializable data class Page<T>(val items: List<T>, val page: Int, val hasMore: Boolean)
@Serializable data class AuthRequest(val email: String, val password: String, val username: String? = null)
@Serializable data class OtpRequest(val email: String, val token: String)
@Serializable data class ResetPasswordRequest(val email: String)
@Serializable data class PasswordPatchRequest(val password: String)
@Serializable data class ProfilePatchRequest(val username: String? = null, val displayName: String? = null, val bio: String? = null, val avatarUrl: String? = null, val lat: Double? = null, val lng: Double? = null)
@Serializable data class PostCreateRequest(val caption: String? = null, val mediaUrls: List<String> = emptyList(), val hashtags: List<String> = emptyList(), val location: String? = null)
@Serializable data class CommentCreateRequest(val body: String, val parentId: String? = null)
@Serializable data class ConversationCreateRequest(val memberIds: List<String>, val title: String? = null, val isGroup: Boolean = false)
@Serializable data class MessageCreateRequest(val body: String? = null, val mediaUrls: List<String> = emptyList(), val replyToId: String? = null)
@Serializable data class ReactionRequest(val reaction: String)
@Serializable data class CallInitiateRequest(val calleeId: String, val type: String, val sdpOffer: String)
@Serializable data class CallAnswerRequest(val callId: String, val sdpAnswer: String)
@Serializable data class IceCandidateRequest(val callId: String, val candidate: String)
@Serializable data class UploadResult(val url: String, val mime: String, val kind: String)
@Serializable data class PushTokenRequest(val token: String, val platform: String)
@Serializable data class WsEnvelope(val channel: String, val event: String, val payload: String)
