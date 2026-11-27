package com.example.mrs_tango.core.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BookingModel(
    val id: String? = null,
    @SerialName("device_id")
    val deviceId: String,
    @SerialName("customer_id")
    val customerId: String,
    @SerialName("verified_by")
    val verifiedBy: String? = null,
    @SerialName("usage_date")
    val usageDate: String,
    @SerialName("start_time")
    val startTime: String,
    @SerialName("time_tipe")
    val timeType: String? = "hourly",
    @SerialName("end_time")
    val endTime: String? = null,
    @SerialName("duration_minutes")
    val durationMinutes: Int? = null,
    @SerialName("estimated_cost")
    val estimatedCost: Int? = null,
    @SerialName("dp_amount")
    val dpAmount: Int? = null,
    @SerialName("dp_proof_url")
    val dpProofUrl: String? = null,
    val status: String? = "pending",
    @SerialName("cancle_reason")
    val cancelReason: String? = null,
    @SerialName("canceled_by")
    val canceledBy: String? = null,
    @SerialName("verified_at")
    val verifiedAt: String? = null,
    @SerialName("expires_at")
    val expiresAt: String? = null,
    @SerialName("created_at")
    val createdAt: String? = null,
    @SerialName("updated_at")
    val updatedAt: String? = null
)