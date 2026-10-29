package com.example.mrs_tango

import java.io.Serializable

data class Console(
    val id: String,
    val name: String,
    val price: String,
    val imagePhoto: Int
) : Serializable