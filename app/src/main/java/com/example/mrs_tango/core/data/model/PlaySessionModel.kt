package com.example.mrs_tango.core.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SessionDeviceInfo(
    val name: String? = "Unit PS",
    @SerialName("consol_type")
    val consolType: String? = null
)

@Serializable
data class PlaySessionModel(
    val id: String,
    @SerialName("device_id")
    val deviceId: String,
    @SerialName("customer_id")
    val customerId: String? = null,
    @SerialName("booking_id")
    val bookingId: String? = null,
    @SerialName("session_type")
    val sessionType: String = "per_hour", // "per_hour" atau "free_play"
    @SerialName("started_at")
    val startedAt: String,
    @SerialName("planned_end_at")
    val plannedEndAt: String? = null,
    @SerialName("ended_at")
    val endedAt: String? = null,
    @SerialName("duration_minutes")
    val durationMinutes: Int? = null,
    @SerialName("gaming_cost")
    val gamingCost: Int = 0,
    val status: String = "active",
    @SerialName("devices")
    val device: SessionDeviceInfo? = null
)