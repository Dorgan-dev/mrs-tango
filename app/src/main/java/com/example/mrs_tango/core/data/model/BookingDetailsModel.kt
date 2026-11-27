package com.example.mrs_tango.core.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BookingDeviceSummary(
    val name: String? = "Unit PS",
    @SerialName("consol_type")
    val psType: String? = null
)

@Serializable
data class BookingDetailsModel(
    val id: String,
    @SerialName("device_id")
    val deviceId: String? = null,
    @SerialName("customer_id")
    val customerId: String? = null,
    @SerialName("usage_date")
    val bookingDate: String? = null,
    @SerialName("start_time")
    val startTime: String? = null,
    @SerialName("time_tipe")
    val timeType: String? = null,
    @SerialName("end_time")
    val endTime: String? = null,
    @SerialName("duration_minutes")
    val durationMinutes: Int? = null,
    @SerialName("estimated_cost")
    val estimatedCost: Int? = null,
    @SerialName("dp_amount")
    val dpAmount: Int? = null,
    @SerialName("dp_proof_file")
    val dpProofFile: String? = null,
    @SerialName("dp_proof_url")
    val dpProofUrl: String? = null,
    val status: String? = "pending",
    @SerialName("cancle_reason")
    val cancelReason: String? = null,
    @SerialName("canceled_by")
    val cancelledBy: String? = null,
    @SerialName("verified_at")
    val verifiedAt: String? = null,
    @SerialName("expires_at")
    val expiresAt: String? = null,
    @SerialName("created_at")
    val createdAt: String? = null,
    @SerialName("devices")
    val device: BookingDeviceSummary? = null,
    @SerialName("refunds")
    val refunds: List<RefundModel> = emptyList()
) {
    // Mengambil data refund terbaru dari daftar refund booking.
    val latestRefund: RefundModel?
        get() = refunds.firstOrNull()

    // Mengambil URL bukti DP dengan prioritas dp_proof_file dan fallback ke dp_proof_url.
    val effectiveDpProof: String?
        get() = dpProofFile?.takeIf(String::isNotBlank)
            ?: dpProofUrl?.takeIf(String::isNotBlank)

    // Membentuk informasi jam bermain berdasarkan tipe dan durasi booking.
    val formattedTimeRange: String
        get() {
            val start = startTime?.take(5) ?: "--:--"
            val end = endTime?.take(5)

            return if (timeType == "per_hour" && end != null) {
                "$start - $end (${(durationMinutes ?: 60) / 60} Jam)"
            } else {
                "Mulai $start (Open Bill)"
            }
        }
}