package com.example.mrs_tango.core.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserModel(
    val id: String? = null,
    val name: String,
    val email: String,
    val password: String,
    val password_set: Int? = 0,
    val gender: String,
    val phone: String? = null,
    val avatar: String? = null,
    @SerialName("birth_date")
    val birthDate: String,
    val role: String = "customer",
    val is_active: Int = 1,
    @SerialName("created_at")
    val createdAt: String? = null
)


