package com.example.mrs_tango.core.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DeviceRateModel(
    val id: String? = null,
    @SerialName("device_id")
    val deviceId: String,
    @SerialName("rate_name")
    val rateName: String = "Reguler",
    @SerialName("price_per_hour")
    val pricePerHour: Int,
    @SerialName("effective_from")
    val effectiveFrom: String? = null, // Format Postgres TIME: "08:00:00"
    @SerialName("effective_until")
    val effectiveUntil: String? = null, // Format Postgres TIME: "18:00:00"
    @SerialName("is_active")
    val isActive: Boolean = true
) {
    fun isApplicableAt(targetMinutes: Int): Boolean {
        if (!isActive) return false
        val start = effectiveFrom.toMinutesOfDay() ?: return true
        val end = effectiveUntil.toMinutesOfDay() ?: return true

        return when {
            start == end -> true // Berlaku 24 jam
            start < end -> targetMinutes in start until end // Shift normal (08:00 - 18:00)
            else -> targetMinutes >= start || targetMinutes < end // Lintas tengah malam (18:00 - 02:00)
        }
    }

    // Format tampilan ringkas untuk UI: "08:00 - 18:00"
    val timeRangeFormatted: String
        get() {
            val start = effectiveFrom?.take(5)
            val end = effectiveUntil?.take(5)
            return if (start != null && end != null) "$start - $end" else "24 Jam"
        }

    private fun String?.toMinutesOfDay(): Int? {
        if (this.isNullOrBlank()) return null
        val parts = this.split(":")
        val hour = parts.getOrNull(0)?.toIntOrNull() ?: return null
        val minute = parts.getOrNull(1)?.toIntOrNull() ?: 0
        return hour * 60 + minute
    }
}