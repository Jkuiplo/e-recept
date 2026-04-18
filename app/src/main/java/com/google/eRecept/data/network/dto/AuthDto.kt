package com.google.eRecept.data.network.dto

import com.google.gson.annotations.SerializedName

// --- LOGIN ---
data class LoginRequest(
    val email: String,
    val password: String,
)

data class LoginResponse(
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("token_type") val tokenType: String,
    @SerializedName("doctor_id") val doctorId: String,
    @SerializedName("full_name") val fullName: String,
    @SerializedName("specialization") val specialization: String,
)

// --- FORGOT PASSWORD ---
data class ForgotPasswordRequest(
    val email: String,
)

data class BaseMessageResponse(
    val detail: String,
)

// --- RESET PASSWORD ---
data class ResetPasswordRequest(
    val token: String,
    @SerializedName("new_password") val newPassword: String,
)
