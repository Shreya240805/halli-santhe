package com.example.halli_santhe.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.card.MaterialCardView
import com.google.android.material.chip.Chip
import com.example.halli_santhe.R
import com.example.halli_santhe.model.Product

class ProductAdapter(
    private var products: MutableList<Product> = mutableListOf(),
    private val onProductClick: (Product) -> Unit
) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    private var allProducts: MutableList<Product> = mutableListOf()

    inner class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val card: MaterialCardView = itemView.findViewById(R.id.productCard)
        val image: ImageView = itemView.findViewById(R.id.productImage)
        val imagePlaceholder: TextView = itemView.findViewById(R.id.productImagePlaceholder)
        val name: TextView = itemView.findViewById(R.id.productName)
        val artisan: TextView = itemView.findViewById(R.id.artisanName)
        val price: TextView = itemView.findViewById(R.id.productPrice)
        val category: TextView = itemView.findViewById(R.id.productCategory)
        val stockBadge: Chip = itemView.findViewById(R.id.stockBadge)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_product, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = products[position]

        holder.name.text = product.name
        holder.artisan.text = "by ${product.artisanName} · ${product.location}"
        holder.price.text = "₹${product.price.toInt()}"
        holder.category.text = product.category

        if (product.stockStatus == "in_stock") {

            holder.stockBadge.text = "✓ ${product.quantity} left"

            holder.stockBadge.setChipBackgroundColorResource(
                R.color.in_stock_green
            )

        } else {

            holder.stockBadge.text = "✗ Out of Stock"

            holder.stockBadge.setChipBackgroundColorResource(
                R.color.out_of_stock_red
            )
        }

        if (product.imageUrl.isNotEmpty()) {
            holder.imagePlaceholder.visibility = View.GONE
            Glide.with(holder.itemView.context)
                .load(product.imageUrl)
                .placeholder(R.color.cream)
                .centerCrop()
                .into(holder.image)
        } else {
            holder.imagePlaceholder.visibility = View.VISIBLE
            holder.imagePlaceholder.text = getCategoryEmoji(product.category)
        }

        holder.card.setOnClickListener { onProductClick(product) }
    }

    override fun getItemCount() = products.size

    fun updateList(newProducts: List<Product>) {
        allProducts = newProducts.toMutableList()
        val diffResult = DiffUtil.calculateDiff(ProductDiffCallback(products, newProducts))
        products.clear()
        products.addAll(newProducts)
        diffResult.dispatchUpdatesTo(this)
    }

    fun filter(query: String) {
        products = if (query.isEmpty()) allProducts.toMutableList()
        else allProducts.filter {
            it.name.contains(query, true) ||
                    it.artisanName.contains(query, true) ||
                    it.category.contains(query, true) ||
                    it.location.contains(query, true)
        }.toMutableList()
        notifyDataSetChanged()
    }

    fun filterByCategory(category: String) {
        products = if (category == "All") allProducts.toMutableList()
        else allProducts.filter { it.category == category }.toMutableList()
        notifyDataSetChanged()
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

    class ProductDiffCallback(
        private val oldList: List<Product>,
        private val newList: List<Product>
    ) : DiffUtil.Callback() {
        override fun getOldListSize() = oldList.size
        override fun getNewListSize() = newList.size
        override fun areItemsTheSame(o: Int, n: Int) = oldList[o].id == newList[n].id
        override fun areContentsTheSame(o: Int, n: Int) = oldList[o] == newList[n]
    }
}