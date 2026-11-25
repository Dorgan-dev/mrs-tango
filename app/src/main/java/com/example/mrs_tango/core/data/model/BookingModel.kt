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
    @SerialName("usage_date")
    val usageDate: String, // Format: "YYYY-MM-DD"
    @SerialName("start_time")
    val startTime: String, // Format: "HH:mm:ss"
    @SerialName("time_tipe")
    val timeType: String? = "hourly", // misal: "hourly" atau "open_bill"
    @SerialName("end_time")
    val endTime: String? = null, // Format: "HH:mm:ss"
    @SerialName("duration_minutes")
    val durationMinutes: Int? = null,
    @SerialName("estimated_cost")
    val estimatedCost: Int? = null,
    @SerialName("dp_amount")
    val dpAmount: Int? = null,
    @SerialName("dp_proof_url")
    val dpProofUrl: String? = null,
    val status: String? = "pending", // pending, confirmed, canceled, completed
    @SerialName("cancle_reason")
    val cancelReason: String? = null,
    @SerialName("canceled_by")
    val canceledBy: String? = null,
    @SerialName("created_at")
    val createdAt: String? = null,
    @SerialName("updated_at")
    val updatedAt: String? = null
)