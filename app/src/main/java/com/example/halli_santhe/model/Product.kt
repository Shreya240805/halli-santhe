package com.example.halli_santhe.model

import com.google.firebase.firestore.DocumentId
import java.io.Serializable

data class Product(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val price: Double = 0.0,
    val quantity: Int = 0,
    val imageUrl: String = "",
    val artisanName: String = "",
    val artisanPhone: String = "",
    val location: String = "",
    val category: String = "",
    val stockStatus: String = "in_stock",
    val timestamp: Long = 0L
) : Serializable
