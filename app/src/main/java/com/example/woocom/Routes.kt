package com.example.woocom

/**
 * Central registry of navigation destinations.
 *
 * Keeps routes as named constants so screens and components never hard-code
 * magic strings. Use the builder helpers when a destination carries arguments.
 */
object Routes {
    const val SPLASH = "splash"
    const val AUTH = "auth"
    const val LOGIN = "login"
    const val SIGNUP = "signup"
    const val HOME = "home"
    const val ORDERS = "orders"
    const val ADDRESSES = "addresses"
    const val SETTINGS = "settings"

    const val CATEGORY_PRODUCTS = "category-products/{categoryId}"
    const val PRODUCT_DETAILS = "product-details/{productId}"
    const val CHECKOUT = "checkout/{totalAmount}"
    const val SEARCH = "search/{query}"

    fun categoryProducts(categoryId: String) = "category-products/$categoryId"
    fun productDetails(productId: String) = "product-details/$productId"
    fun checkout(totalAmount: Double) = "checkout/$totalAmount"
    fun search(query: String) = "search/$query"
}