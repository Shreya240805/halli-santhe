package com.example.halli_santhe.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.example.halli_santhe.R
import com.example.halli_santhe.model.Product
import com.example.halli_santhe.utils.Constants

class DetailActivity : AppCompatActivity() {

    private lateinit var product: Product

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        @Suppress("DEPRECATION")
        product = intent.getSerializableExtra(Constants.KEY_PRODUCT) as? Product
            ?: run { finish(); return }

        setupToolbar()
        bindProductData()
        setupButtons()
    }

    private fun setupToolbar() {
        val toolbar = findViewById<Toolbar>(R.id.detailToolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = ""
        toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
    }

    private fun bindProductData() {
        val productImage = findViewById<ImageView>(R.id.detailProductImage)
        val imagePlaceholder = findViewById<TextView>(R.id.detailImagePlaceholder)

        if (product.imageUrl.isNotEmpty()) {
            imagePlaceholder.visibility = View.GONE
            Glide.with(this).load(product.imageUrl).centerCrop().into(productImage)
        } else {
            imagePlaceholder.text = getCategoryEmoji(product.category)
        }

        findViewById<TextView>(R.id.detailProductName).text = product.name
        findViewById<TextView>(R.id.detailProductPrice).text = "₹${product.price.toInt()}"
        findViewById<TextView>(R.id.detailDescription).text = product.description
        findViewById<TextView>(R.id.detailArtisanName).text = product.artisanName
        findViewById<TextView>(R.id.detailArtisanLocation).text = "📍 ${product.location}"
        findViewById<TextView>(R.id.detailCategory).text = "🏷 ${product.category}"

        val stockChip = findViewById<Chip>(R.id.detailStockChip)
        if (product.stockStatus == "in_stock") {

            stockChip.text = "✓ ${product.quantity} items left"

            stockChip.setChipBackgroundColorResource(
                R.color.in_stock_green
            )

        } else {

            stockChip.text = "✗ Out of Stock"

            stockChip.setChipBackgroundColorResource(
                R.color.out_of_stock_red
            )
        }
    }

    private fun setupButtons() {
        findViewById<ExtendedFloatingActionButton>(R.id.fabCheckStock)
            .setOnClickListener {

                if (product.quantity > 0) {

                    Snackbar.make(
                        findViewById(android.R.id.content),
                        "🛒 Added to cart",
                        Snackbar.LENGTH_SHORT
                    ).show()

                } else {

                    Snackbar.make(
                        findViewById(android.R.id.content),
                        "❌ Product out of stock",
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
            }
        findViewById<MaterialButton>(R.id.btnMessageArtisan)
            .setOnClickListener { openWhatsApp() }
    }

    private fun showCheckStockDialog() {
        val msg = if (product.stockStatus == "in_stock")
            "✅ ${product.name} is IN STOCK.\n\nArtisan: ${product.artisanName}\nLocation: ${product.location}"
        else
            "❌ ${product.name} is OUT OF STOCK.\n\nContact the artisan to check restocking."

        AlertDialog.Builder(this)
            .setTitle("Stock Status")
            .setMessage(msg)
            .setPositiveButton("Message Artisan") { _, _ -> openWhatsApp() }
            .setNegativeButton("Close", null)
            .show()
    }

    private fun openWhatsApp() {
        val phone = product.artisanPhone.replace("+", "").replace(" ", "")
        val message = "Hello! I saw '${product.name}' on Halli Santhe. Is it available for ₹${product.price.toInt()}?"
        try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("https://wa.me/$phone?text=${Uri.encode(message)}")
                setPackage("com.whatsapp")
            }
            startActivity(intent)
        } catch (e: Exception) {
            Snackbar.make(
                findViewById(android.R.id.content),
                "WhatsApp not installed",
                Snackbar.LENGTH_SHORT
            ).show()
        }
    }

    private fun getCategoryEmoji(category: String) = when (category) {
        "Channapatna Toys" -> "🪀"
        "Handloom" -> "🧵"
        "Pottery" -> "🏺"
        "Jewellery" -> "💍"
        "Paintings" -> "🎨"
        "Basketry" -> "🧺"
        "Leather" -> "👜"
        else -> "🎁"
    }
}