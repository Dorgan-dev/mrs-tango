package com.example.mrs_tango.DummyData

import java.io.Serializable

data class ConsoleModel(
    val id: String,
    val name: String,
    val price: String,
    val imagePhoto: Int
) : Serializable