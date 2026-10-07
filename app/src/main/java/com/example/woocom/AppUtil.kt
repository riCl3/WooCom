package com.example.woocom

import android.content.Context
import android.widget.Toast
import java.util.Locale

/**
 * Small, side-effect helpers shared across the UI layer.
 *
 * Data access lives in `com.example.woocom.data.*` — this object deliberately holds no
 * Firebase references so it can be unit-tested on the JVM.
 */
object AppUtil {

    fun showToast(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }

    /** Safely parses a currency string (e.g. "1,299.00") into a Double. */
    fun parsePrice(price: String): Double {
        return try {
            price.replace(",", "").trim().toDouble()
        } catch (e: NumberFormatException) {
            0.0
        }
    }

    /**
     * Line total for one cart row.
     *
     * Product prices are stored as display strings in the backend (`price` is the
     * selling price, `actualPrice` is the struck-through MRP), so every total in the
     * app — cart subtotal, checkout amount, order amount — must go through here or the
     * numbers drift apart.
     */
    fun lineTotal(sellingPrice: String, quantity: Long): Double =
        parsePrice(sellingPrice) * quantity

    fun formatPrice(amount: Double, locale: Locale = Locale.getDefault()): String =
        String.format(locale, "₹%,.2f", amount)
}
