package com.example.woocom

import org.junit.Assert.assertEquals
import org.junit.Test

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
    fun formatPrice_rendersIndianLocale() {
        assertEquals("₹1,234.00", AppUtil.formatPrice(1234.0))
    }
}