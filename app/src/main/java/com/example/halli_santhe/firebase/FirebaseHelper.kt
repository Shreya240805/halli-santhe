package com.example.halli_santhe.firebase

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import com.example.halli_santhe.model.Product
import com.example.halli_santhe.utils.Constants

object FirebaseHelper {

    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    fun getAllProducts(
        onSuccess: (List<Product>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        db.collection(Constants.COLLECTION_PRODUCTS)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onError(error)
                    return@addSnapshotListener
                }
                val products = snapshot?.toObjects(Product::class.java) ?: emptyList()
                onSuccess(products)
            }
    }

    fun addProduct(
        product: Product,
        onSuccess: (String) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val ref = db.collection(Constants.COLLECTION_PRODUCTS).document()
        val productWithId = product.copy(id = ref.id)
        ref.set(productWithId)
            .addOnSuccessListener { onSuccess(ref.id) }
            .addOnFailureListener { onError(it) }
    }

    fun uploadImage(
        imageBytes: ByteArray,
        productId: String,
        onSuccess: (String) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val ref = storage.reference.child("product_images/$productId.jpg")
        ref.putBytes(imageBytes)
            .addOnSuccessListener {
                ref.downloadUrl
                    .addOnSuccessListener { uri -> onSuccess(uri.toString()) }
                    .addOnFailureListener { onError(it) }
            }
            .addOnFailureListener { onError(it) }
    }

    fun seedSampleData() {
        val sampleProducts = listOf(
            Product(
                name = "Channapatna Spinning Top",
                description = "Hand-crafted wooden spinning top made with natural lac dyes.",
                price = 150.0,
                artisanName = "Ramaiah",
                artisanPhone = "+919876543210",
                category = "Channapatna Toys",
                stockStatus = "in_stock",
                location = "Channapatna, Karnataka"
            ),
            Product(
                name = "Silk Saree — Mysore",
                description = "Pure silk saree woven on handloom with traditional Mysore motifs.",
                price = 4500.0,
                artisanName = "Saraswathi Devi",
                artisanPhone = "+919876543211",
                category = "Handloom",
                stockStatus = "in_stock",
                location = "Mysore, Karnataka"
            ),
            Product(
                name = "Terracotta Pot Set",
                description = "Set of 3 hand-thrown terracotta pots for plants or home décor.",
                price = 350.0,
                artisanName = "Muniswamy",
                artisanPhone = "+919876543212",
                category = "Pottery",
                stockStatus = "out_of_stock",
                location = "Bidadi, Karnataka"
            ),
            Product(
                name = "Dokra Brass Elephant",
                description = "Lost-wax cast brass elephant figurine. Each piece is unique.",
                price = 899.0,
                artisanName = "Lakshmi Bai",
                artisanPhone = "+919876543213",
                category = "Other",
                stockStatus = "in_stock",
                location = "Bangalore Rural, Karnataka"
            )
        )
        sampleProducts.forEach { product ->
            addProduct(product, onSuccess = {}, onError = {})
        }
    }
}