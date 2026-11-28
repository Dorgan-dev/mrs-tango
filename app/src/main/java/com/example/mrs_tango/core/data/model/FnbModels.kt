package com.example.mrs_tango.core.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FnbCategoryModel(
    val id: String,
    val name: String,
    @SerialName("is_active")
    val isActive: Boolean = true
)

@Serializable
data class FnbItemModel(
    val id: String,
    @SerialName("category_id")
    val categoryId: String,
    val name: String,
    val price: Int,
    val stock: Int = 0,
    @SerialName("is_available")
    val isAvailable: Boolean = true
)