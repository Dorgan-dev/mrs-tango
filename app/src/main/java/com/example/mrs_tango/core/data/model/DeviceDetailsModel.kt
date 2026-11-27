package com.example.mrs_tango.core.data.model

import com.example.mrs_tango.core.utils.toRupiah
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.Calendar

@Serializable
data class DeviceDetailsModel(
    val id: String,
    val name: String,
    val description: String? = null, //ini kolom baru
    @SerialName("consol_type")
    val consoleType: String? = null,
    @SerialName("console_sn")
    val consoleSn: String? = null,
    @SerialName("tv_name")
    val tvName: String? = null,
    @SerialName("tv_sn")
    val tvSn: String? = null,
    @SerialName("tv_ip_address")
    val tvIpAddress: String? = null,
    @SerialName("tv_mac_address")
    val tvMacAddress: String? = null,
    val status: String? = "available",
    @SerialName("image_url")
    val legacyImageUrl: String? = null,
    @SerialName("price_per_hour")
    val legacyPricePerHour: Int? = null,
    @SerialName("created_at")
    val createdAt: String? = null,
    @SerialName("updated_at")
    val updatedAt: String? = null,
    @SerialName("device_images")
    val images: List<DeviceImageModel> = emptyList(),
    @SerialName("device_rates")
    val rates: List<DeviceRateModel> = emptyList()
) : java.io.Serializable {

    val primaryImageUrl: String?
        get() = images.firstOrNull { it.isPrimary }?.imageUrl
            ?: images.firstOrNull()?.imageUrl
            ?: legacyImageUrl

    val displayName: String
        get() = if (!consoleType.isNullOrBlank()) "$name - $consoleType" else name

    // Daftar seluruh tarif yang aktif milik perangkat ini
    val activeRates: List<DeviceRateModel>
        get() = rates.filter { it.isActive }

    // Tarif yang berlaku pada jam sekarang
    val activeRate: DeviceRateModel?
        get() {
            val now = Calendar.getInstance()
            return getRateAt(now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE))
        }

    // Harga aktif jam sekarang, fallback ke kolom lama jika device_rates kosong
    val activePricePerHour: Int
        get() = activeRate?.pricePerHour
            ?: legacyPricePerHour
            ?: 0

    // Format harga utama untuk kartu Home & header Detail
    val formattedPrice: String
        get() {
            val currentRate = activeRate
            return when {
                currentRate != null -> "${currentRate.pricePerHour.toRupiah()} / jam (${currentRate.rateName})"
                activePricePerHour > 0 -> "${activePricePerHour.toRupiah()} / jam"
                else -> "Tarif belum diatur"
            }
        }

    // Ringkasan seluruh jadwal shift (Siang / Malam) untuk ditampilkan di DeviceDetailFragment
    val formattedRateSchedule: String
        get() = activeRates.joinToString("\n") { rate ->
            "• ${rate.rateName} (${rate.timeRangeFormatted}): ${rate.pricePerHour.toRupiah()}/jam"
        }

    // Cari tarif pada jam tertentu (berguna saat Customer memilih jam booking)
    fun getRateAt(hour: Int, minute: Int = 0): DeviceRateModel? {
        val targetMinutes = (hour % 24) * 60 + minute

        return activeRates.firstOrNull {
            it.effectiveFrom != null && it.effectiveUntil != null && it.isApplicableAt(targetMinutes)
        }
            ?: activeRates.firstOrNull { it.effectiveFrom == null && it.effectiveUntil == null }
            ?: activeRates.firstOrNull()
    }

    // Hitung total biaya jika durasi main melintasi 2 shift (misal mulai 17:00 main 3 jam)
    fun calculateTotalCost(startHour: Int, startMinute: Int, durationHours: Int): Int {
        var total = 0
        for (i in 0 until durationHours) {
            val checkHour = (startHour + i) % 24
            total += getRateAt(checkHour, startMinute)?.pricePerHour
                ?: legacyPricePerHour
                        ?: 0
        }
        return total
    }
}