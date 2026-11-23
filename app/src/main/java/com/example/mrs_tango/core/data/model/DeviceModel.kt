package com.example.mrs_tango.core.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DeviceModel(
    val id: String? = null,
    val name: String? = null,
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
    @SerialName("updated_at")
    val updatedAt: String? = null,
    @SerialName("created_at")
    val createdAt: String? = null
)