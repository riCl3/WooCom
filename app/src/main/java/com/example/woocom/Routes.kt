package com.example.woocom

/**
 * Central registry of navigation destinations.
 *
 * Keeps routes as named constants so screens and components never hard-code magic
 * strings. Argument values are percent-encoded (RFC 3986) before they are spliced into
 * the pattern — without this a search query such as "50% off" or "men/shoes" produces an
 * unmatched route and crashes at runtime.
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
    const val CHECKOUT = "checkout"
    const val SEARCH = "search/{query}"

    fun categoryProducts(categoryId: String) = "category-products/${encode(categoryId)}"

    fun productDetails(productId: String) = "product-details/${encode(productId)}"

    fun search(query: String) = "search/${encode(query.trim())}"

    /**
     * Percent-encodes everything outside the RFC 3986 unreserved set.
     *
     * Implemented locally (instead of `Uri.encode`) so the rule is unit-testable on the
     * JVM without the Android framework.
     */
    fun encode(value: String): String =
        buildString(value.length) {
            for (char in value) {
                if (char.isUnreserved()) {
                    append(char)
                } else {
                    for (byte in char.toString().toByteArray(Charsets.UTF_8)) {
                        val unsigned = byte.toInt() and 0xFF
                        append('%')
                        append(HEX[unsigned ushr 4])
                        append(HEX[unsigned and 0x0F])
                    }
                }
            }
        }

    private const val HEX = "0123456789ABCDEF"

    private fun Char.isUnreserved(): Boolean =
        this in 'A'..'Z' || this in 'a'..'z' || this in '0'..'9' ||
            this == '-' || this == '.' || this == '_' || this == '~'
}
