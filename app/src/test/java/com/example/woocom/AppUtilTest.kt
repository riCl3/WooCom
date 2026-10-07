package com.example.woocom

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Locale

class AppUtilTest {
    @Test
    fun parsePrice_handlesCommas() {
        assertEquals(1299.00, AppUtil.parsePrice("1,299"), 0.0)
    }

    @Test
    fun parsePrice_handlesWhitespace() {
        assertEquals(99.5, AppUtil.parsePrice(" 99.5 "), 0.0)
    }

    @Test
    fun parsePrice_returnsZeroForInvalidInput() {
        assertEquals(0.0, AppUtil.parsePrice("not-a-price"), 0.0)
    }

    @Test
    fun parsePrice_returnsZeroForBlankInput() {
        assertEquals(0.0, AppUtil.parsePrice(""), 0.0)
    }

    @Test
    fun parsePrice_stripsCurrencySymbols() {
        assertEquals(499.0, AppUtil.parsePrice("₹499.00"), 0.0)
    }

    @Test
    fun parsePrice_returnsZeroForDashOnlyInput() {
        assertEquals(0.0, AppUtil.parsePrice("-"), 0.0)
    }

    @Test
    fun formatPrice_groupsIndianStyle_enIN() {
        assertEquals("₹1,234.00", AppUtil.formatPrice(1234.0, Locale("en", "IN")))
    }

    @Test
    fun formatPrice_groupsEuropeanStyle_deDE() {
        assertEquals("₹1.234,00", AppUtil.formatPrice(1234.0, Locale("de", "DE")))
    }

    @Test
    fun formatPrice_alwaysUsesTheRupeeSymbol() {
        // Grouping legitimately differs per locale; the currency symbol must not.
        val formatted = AppUtil.formatPrice(10.0)
        assertTrue(formatted.startsWith("₹"))
        assertTrue(formatted.contains("10"))
    }

    @Test
    fun lineTotal_multipliesSellingPriceByQuantity() {
        assertEquals(2998.0, AppUtil.lineTotal("1,499", 2), 0.0)
    }

    @Test
    fun lineTotal_isZeroForMissingProduct() {
        assertEquals(0.0, AppUtil.lineTotal("", 5), 0.0)
    }
}
