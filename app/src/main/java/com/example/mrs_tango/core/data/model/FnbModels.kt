package com.example.mrs_tango.core.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FnbCategoryModel(
    val id: String? = null,
    val name: String,
    @SerialName("is_active")
    val isActive: Boolean = true
)

@Serializable
data class FnbItemModel(
    val id: String? = null,
    @SerialName("category_id")
    val categoryId: String,
    val name: String,
    val price: Int,
    val stock: Int = 0,
    @SerialName("is_available")
    val isAvailable: Boolean = true
)

@Serializable
data class FnbCategorySummary(
    val name: String = "Tanpa Kategori"
)

@Serializable
data class FnbItemWithCategory(
    val id: String,
    @SerialName("category_id")
    val categoryId: String,
    val name: String,
    val price: Int,
    val stock: Int = 0,
    @SerialName("is_available")
    val isAvailable: Boolean = true,
    @SerialName("fnb_categories")
    val category: FnbCategorySummary? = null
)