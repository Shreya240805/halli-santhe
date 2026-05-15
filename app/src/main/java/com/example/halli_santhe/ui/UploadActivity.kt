package com.example.halli_santhe.ui

import android.app.Activity
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.example.halli_santhe.R
import com.example.halli_santhe.firebase.FirebaseHelper
import com.example.halli_santhe.model.Product
import com.example.halli_santhe.utils.Constants
import com.example.halli_santhe.utils.ImageUtils
import java.util.UUID

class UploadActivity : AppCompatActivity() {

    private lateinit var uploadedImageView: ImageView
    private lateinit var imagePlaceholder: LinearLayout
    private lateinit var inputProductName: TextInputEditText
    private lateinit var inputPrice: TextInputEditText
    private lateinit var inputQuantity: TextInputEditText
    private lateinit var inputDescription: TextInputEditText
    private lateinit var inputArtisanName: TextInputEditText
    private lateinit var inputPhone: TextInputEditText
    private lateinit var inputLocation: TextInputEditText
    private lateinit var categoryDropdown: AutoCompleteTextView
    private lateinit var btnSubmit: MaterialButton
    private lateinit var uploadProgressLayout: LinearLayout
    private lateinit var uploadStatusText: TextView

    private var selectedImageBitmap: Bitmap? = null

    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val bitmap = ImageUtils.getBitmapFromUri(this, it)
            if (bitmap != null) {
                selectedImageBitmap = bitmap
                uploadedImageView.setImageBitmap(bitmap)
                uploadedImageView.visibility = View.VISIBLE
                imagePlaceholder.visibility = View.GONE
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upload)

        initViews()
        setupToolbar()
        setupImagePicker()
        setupCategoryDropdown()
        setupSubmitButton()
    }

    private fun initViews() {
        uploadedImageView = findViewById(R.id.uploadedImage)
        imagePlaceholder = findViewById(R.id.imagePlaceholder)
        inputProductName = findViewById(R.id.inputProductName)
        inputPrice = findViewById(R.id.inputPrice)
        inputDescription = findViewById(R.id.inputDescription)
        inputArtisanName = findViewById(R.id.inputArtisanName)
        inputPhone = findViewById(R.id.inputPhone)
        inputLocation = findViewById(R.id.inputLocation)
        categoryDropdown = findViewById(R.id.categoryDropdown)
        btnSubmit = findViewById(R.id.btnSubmit)
        uploadProgressLayout = findViewById(R.id.uploadProgressLayout)
        uploadStatusText = findViewById(R.id.uploadStatusText)
    }

    private fun setupToolbar() {
        val toolbar = findViewById<Toolbar>(R.id.uploadToolbar)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupImagePicker() {
        findViewById<View>(R.id.imagePickerContainer).setOnClickListener {
            imagePickerLauncher.launch("image/*")
        }
    }

    private fun setupCategoryDropdown() {
        val uploadCategories = Constants.CATEGORIES.filter { it != "All" }
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_dropdown_item_1line,
            uploadCategories
        )
        categoryDropdown.setAdapter(adapter)
        categoryDropdown.setText(uploadCategories[0], false)
    }

    private fun setupSubmitButton() {
        btnSubmit.setOnClickListener {
            if (validateForm()) uploadProduct()
        }
    }

    private fun validateForm(): Boolean {
        if (inputProductName.text.isNullOrBlank()) {
            inputProductName.error = "Required"; return false
        }
        if (inputPrice.text.isNullOrBlank()) {
            inputPrice.error = "Required"; return false
        }
        if (inputQuantity.text.isNullOrBlank()) {
            inputQuantity.error = "Required"; return false
        }
        if (inputArtisanName.text.isNullOrBlank()) {
            inputArtisanName.error = "Required"; return false
        }
        return true
    }

    private fun uploadProduct() {
        val productId = UUID.randomUUID().toString()
        val bitmap = selectedImageBitmap

        if (bitmap != null) {
            setLoadingState(true, "Compressing image…")
            val imageBytes = ImageUtils.compressBitmap(bitmap)
            setLoadingState(true, "Uploading image…")
            FirebaseHelper.uploadImage(imageBytes, productId,
                onSuccess = { imageUrl -> saveProductToFirestore(productId, imageUrl) },
                onError = { setLoadingState(false); showError("Image upload failed") }
            )
        } else {
            saveProductToFirestore(productId, "")
        }
    }

    private fun saveProductToFirestore(productId: String, imageUrl: String) {
        setLoadingState(true, "Saving to Halli Santhe…")
        val product = Product(
            id = productId,
            name = inputProductName.text.toString().trim(),
            description = inputDescription.text?.toString()?.trim() ?: "",
            price = inputPrice.text.toString().trim().toDoubleOrNull() ?: 0.0,
            quantity = inputQuantity.text.toString().trim().toIntOrNull() ?: 0,
            imageUrl = imageUrl,
            artisanName = inputArtisanName.text.toString().trim(),
            artisanPhone = "+91${inputPhone.text?.toString()?.trim()}",
            location = inputLocation.text?.toString()?.trim() ?: "",
            category = categoryDropdown.text.toString(),
            stockStatus = "in_stock",
            timestamp = System.currentTimeMillis()
        )
        FirebaseHelper.addProduct(product,
            onSuccess = { setLoadingState(false); showSuccess() },
            onError = { setLoadingState(false); showError("Failed to save") }
        )
    }

    private fun setLoadingState(loading: Boolean, message: String = "") {
        btnSubmit.isEnabled = !loading
        uploadProgressLayout.visibility = if (loading) View.VISIBLE else View.GONE
        if (message.isNotEmpty()) uploadStatusText.text = message
    }

    private fun showSuccess() {
        Snackbar.make(
            findViewById(android.R.id.content),
            "🎉 Listed on Halli Santhe!",
            Snackbar.LENGTH_LONG
        ).show()
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            setResult(Activity.RESULT_OK)
            finish()
        }, 1500)
    }

    private fun showError(message: String) {
        Snackbar.make(
            findViewById(android.R.id.content),
            message,
            Snackbar.LENGTH_LONG
        ).show()
    }
}