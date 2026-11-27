package com.example.mrs_tango.core.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.text.NumberFormat
import java.util.Locale

@Serializable
data class DeviceModel(
    val id: String? = null,
    val name: String? = null,
    val description: String? = null, // ini kolom baru
    @SerialName("consol_type")
    val consoleType: String? = null,
    @SerialName("console_sn")
    val consoleSn: String? = null,
    @SerialName("tv_name")
    val tvName: String? = null,
    @SerialName("price")
    val price: Int? =  null,
    @SerialName("tv_sn")
    val tvSn: String? = null,
    @SerialName("tv_ip_address")
    val tvIpAddress: String? = null,
    @SerialName("tv_mac_address")
    val tvMacAddress: String? = null,
    val status: String? = "available",
    @SerialName("updated_at")
    val updatedAt: String? = null,
    @SerialName("created_at")
    val createdAt: String? = null
) : java.io.Serializable {

    // Properti tanpa backing field aman dan diabaikan oleh kotlinx.serialization
    val displayName: String
        get() = if (!consoleType.isNullOrBlank()) "$name ($consoleType)" else (name ?: "Unit")

    val hourlyPrice: Int
        get() = when {
            consoleType?.contains("PS5", ignoreCase = true) == true -> 15000
            consoleType?.contains("PS4", ignoreCase = true) == true -> 10000
            consoleType?.contains("Xbox", ignoreCase = true) == true -> 14000
            consoleType?.contains("Switch", ignoreCase = true) == true -> 12000
            else -> 10000
        }

    val formattedPrice: String
        get() {
            val formatter = NumberFormat.getCurrencyInstance(Locale("in", "ID")).apply {
                maximumFractionDigits = 0
            }
            return "${formatter.format(hourlyPrice)} / Jam"
        }
}