package com.example.mrs_tango.core.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TransactionItemModel(
    val id: String,
    @SerialName("transaction_id")
    val transactionId: String,
    @SerialName("fnb_item_id")
    val fnbItemId: String? = null,
    @SerialName("item_name")
    val itemName: String,
    val quantity: Int = 1,
    @SerialName("unit_price")
    val unitPrice: Int,
    val subtotal: Int
)

@Serializable
data class TransactionSessionSummary(
    @SerialName("customer_id")
    val customerId: String? = null,
    @SerialName("started_at")
    val startedAt: String? = null,
    @SerialName("ended_at")
    val endedAt: String? = null,
    @SerialName("duration_minutes")
    val durationMinutes: Int? = null,
    @SerialName("devices")
    val device: SessionDeviceInfo? = null
)

@Serializable
data class TransactionWithDetails(
    val id: String,
    @SerialName("session_id")
    val sessionId: String,
    @SerialName("invoice_number")
    val invoiceNumber: String,
    @SerialName("gaming_total")
    val gamingTotal: Int = 0,
    @SerialName("fnb_total")
    val fnbTotal: Int = 0,
    @SerialName("grand_total")
    val grandTotal: Int = 0,
    @SerialName("dp_paid")
    val dpPaid: Int = 0,
    @SerialName("remaining_amount")
    val remainingAmount: Int = 0,
    @SerialName("amount_paid")
    val amountPaid: Int = 0,
    @SerialName("change_amount")
    val changeAmount: Int = 0,
    @SerialName("payment_method")
    val paymentMethod: String = "cash",
    val status: String = "paid",
    @SerialName("paid_at")
    val paidAt: String,
    @SerialName("play_sessions")
    val session: TransactionSessionSummary? = null,
    @SerialName("transaction_items")
    val items: List<TransactionItemModel> = emptyList()
)