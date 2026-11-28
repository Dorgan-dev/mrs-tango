package com.example.mrs_tango.core.data.repository

import com.example.mrs_tango.core.data.remote.SupabaseClient
import com.example.mrs_tango.core.utils.Result
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Serializable
data class DashboardDeviceStatus(
    val id: String,
    val status: String? = "available"
)

@Serializable
data class DashboardTransactionItem(
    val id: String,
    @SerialName("invoice_number")
    val invoiceNumber: String,
    @SerialName("gaming_total")
    val gamingTotal: Int = 0,
    @SerialName("fnb_total")
    val fnbTotal: Int = 0,
    @SerialName("grand_total")
    val grandTotal: Int = 0,
    @SerialName("payment_method")
    val paymentMethod: String = "cash",
    val status: String = "paid",
    @SerialName("paid_at")
    val paidAt: String
)

data class OwnerDashboardSummary(
    val todayRevenue: Int,
    val totalPaidRevenue: Int,
    val totalTransactionsCount: Int,
    val totalDevices: Int,
    val availableDevices: Int,
    val inUseDevices: Int,
    val maintenanceDevices: Int,
    val recentTransactions: List<DashboardTransactionItem>
)

class OwnerDashboardRepository {

    suspend fun getDashboardSummary(): Result<OwnerDashboardSummary> {
        return withContext(Dispatchers.IO) {
            try {
                val devices = SupabaseClient.client.from("devices")
                    .select(columns = Columns.raw("id, status"))
                    .decodeList<DashboardDeviceStatus>()

                val paidTransactions = SupabaseClient.client.from("transactions")
                    .select(
                        columns = Columns.raw(
                            "id, invoice_number, gaming_total, fnb_total, grand_total, payment_method, status, paid_at"
                        )
                    ) {
                        filter { eq("status", "paid") }
                        order(column = "paid_at", order = Order.DESCENDING)
                    }
                    .decodeList<DashboardTransactionItem>()

                val todayPrefix = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
                val todayRevenue = paidTransactions
                    .filter { it.paidAt.startsWith(todayPrefix) }
                    .sumOf { it.grandTotal }

                val summary = OwnerDashboardSummary(
                    todayRevenue = todayRevenue,
                    totalPaidRevenue = paidTransactions.sumOf { it.grandTotal },
                    totalTransactionsCount = paidTransactions.size,
                    totalDevices = devices.size,
                    availableDevices = devices.count { it.status == "available" },
                    inUseDevices = devices.count { it.status == "in_use" || it.status == "booked" },
                    maintenanceDevices = devices.count { it.status == "maintenance" },
                    recentTransactions = paidTransactions.take(5)
                )

                Result.Success(summary)
            } catch (e: Exception) {
                Result.Error(e.message ?: "Gagal memuat ringkasan dashboard")
            }
        }
    }
}