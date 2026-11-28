package com.example.mrs_tango.core.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LogDeviceSummary(val name: String? = "Unit PS")

@Serializable
data class LogActorSummary(val name: String? = "Sistem")

@Serializable
data class DeviceLogModel(
    val id: String? = null,
    @SerialName("device_id")
    val deviceId: String,
    @SerialName("actor_id")
    val actorId: String? = null,
    @SerialName("from_status")
    val fromStatus: String,
    @SerialName("to_status")
    val toStatus: String,
    val note: String? = null,
    @SerialName("changed_at")
    val changedAt: String? = null,
    @SerialName("devices")
    val device: LogDeviceSummary? = null,
    @SerialName("users")
    val actor: LogActorSummary? = null
)