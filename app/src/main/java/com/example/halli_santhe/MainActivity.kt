package com.example.halli_santhe

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.LinearLayout
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.halli_santhe.R
import com.example.halli_santhe.adapter.ProductAdapter
import com.example.halli_santhe.firebase.FirebaseHelper
import com.example.halli_santhe.ui.DetailActivity
import com.example.halli_santhe.ui.UploadActivity
import com.example.halli_santhe.utils.Constants
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.textfield.TextInputEditText

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ProductAdapter
    private lateinit var emptyStateLayout: LinearLayout
    private lateinit var loadingProgressBar: ProgressBar
    private lateinit var searchEditText: TextInputEditText
    private lateinit var categoryChipGroup: ChipGroup
    private lateinit var fabAddProduct: ExtendedFloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initViews()
        setupRecyclerView()
        setupCategoryChips()
        setupSearch()
        setupFab()
        loadProducts()
    }

    private fun initViews() {
        recyclerView = findViewById(R.id.productRecyclerView)
        emptyStateLayout = findViewById(R.id.emptyStateLayout)
        loadingProgressBar = findViewById(R.id.loadingProgressBar)
        searchEditText = findViewById(R.id.searchEditText)
        categoryChipGroup = findViewById(R.id.categoryChipGroup)
        fabAddProduct = findViewById(R.id.fabAddProduct)
    }

    private fun setupRecyclerView() {
        adapter = ProductAdapter { product ->
            val intent = Intent(this, DetailActivity::class.java)
            intent.putExtra(Constants.KEY_PRODUCT, product)
            startActivity(intent)
        }
        recyclerView.apply {
            layoutManager = GridLayoutManager(this@MainActivity, 2)
            adapter = this@MainActivity.adapter
            setHasFixedSize(false)
        }
    }

    private fun setupCategoryChips() {
        Constants.CATEGORIES.forEach { category ->
            val chip = Chip(this).apply {
                text = category
                isCheckable = true
                setChipBackgroundColorResource(R.color.warm_white)
                setTextColor(resources.getColor(R.color.dark_brown, null))
                chipStrokeWidth = 1f
                setChipStrokeColorResource(R.color.saffron)
                setCheckedIconVisible(false)
            }
            categoryChipGroup.addView(chip)
        }

        (categoryChipGroup.getChildAt(0) as? Chip)?.isChecked = true

        categoryChipGroup.setOnCheckedStateChangeListener { group, checkedIds ->
            if (checkedIds.isNotEmpty()) {
                val selectedChip = group.findViewById<Chip>(checkedIds[0])
                val selectedCategory = selectedChip?.text?.toString() ?: "All"
                adapter.filterByCategory(selectedCategory)
                showEmptyStateIfNeeded()
            }
        }
    }

    private fun setupSearch() {
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                adapter.filter(s?.toString() ?: "")
                showEmptyStateIfNeeded()
            }
        })
    }

    private fun setupFab() {
        fabAddProduct.setOnClickListener {
            startActivity(Intent(this, UploadActivity::class.java))
        }
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy > 0) fabAddProduct.shrink() else fabAddProduct.extend()
            }
        })
    }

    private fun loadProducts() {
        showLoading(true)
        FirebaseHelper.getAllProducts(
            onSuccess = { products ->
                showLoading(false)
                adapter.updateList(products)
                showEmptyStateIfNeeded()
                if (products.isEmpty()) FirebaseHelper.seedSampleData()
            },
            onError = {
                showLoading(false)
                showEmptyState(true)
            }
        )
    }

    private fun showLoading(show: Boolean) {
        loadingProgressBar.visibility = if (show) View.VISIBLE else View.GONE
        recyclerView.visibility = if (show) View.GONE else View.VISIBLE
    }

    private fun showEmptyState(show: Boolean) {
        emptyStateLayout.visibility = if (show) View.VISIBLE else View.GONE
        recyclerView.visibility = if (show) View.GONE else View.VISIBLE
    }

    private fun showEmptyStateIfNeeded() {
        showEmptyState(adapter.itemCount == 0)
    }
}