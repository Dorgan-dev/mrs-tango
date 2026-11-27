package com.example.mrs_tango.core.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RefundModel(
    val id: String? = null,
    @SerialName("booking_id")
    val bookingId: String,
    @SerialName("processed_by")
    val processedBy: String? = null,
    @SerialName("refund_amount")
    val refundAmount: Int,
    val reason: String,
    @SerialName("refund_method")
    val refundMethod: String = "transfer",
    @SerialName("processed_at")
    val processedAt: String? = null
)