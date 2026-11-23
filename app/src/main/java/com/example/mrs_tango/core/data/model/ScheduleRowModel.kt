package com.example.mrs_tango.core.data.model

data class ScheduleRowModel(
    val time: String,                 // Contoh: "01:00"
    val unitStatuses: List<String>    // Contoh: listOf("Digunakan", "-", "Dibooking")
)