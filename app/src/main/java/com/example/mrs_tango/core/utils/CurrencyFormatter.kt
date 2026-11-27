package com.example.mrs_tango.core.utils

import java.text.NumberFormat
import java.util.Locale

fun Int.toRupiah(): String {
    val format = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
    format.maximumFractionDigits = 0
    return format.format(this)
}