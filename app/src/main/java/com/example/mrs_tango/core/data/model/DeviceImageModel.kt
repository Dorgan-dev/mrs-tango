package com.example.mrs_tango.core.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DeviceImageModel(
    val id: String? = null,
    @SerialName("device_id")
    val deviceId: String,
    @SerialName("image_url")
    val imageUrl: String,
    @SerialName("storage_path")
    val storagePath: String? = null,
    @SerialName("is_primary")
    val isPrimary: Boolean = false,
    @SerialName("created_at")
    val createdAt: String? = null
) : java.io.Serializable