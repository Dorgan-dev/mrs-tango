package com.example.mrs_tango.core.data.model

import kotlinx.serialization.Serializable

@Serializable
data class SettingModel(
    val id: String? = null,
    val key: String,
    val value: String? = null,
    val label: String? = null
)