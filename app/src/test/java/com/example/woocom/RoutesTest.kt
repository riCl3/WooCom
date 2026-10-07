package com.example.woocom

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RoutesTest {
    @Test
    fun search_encodesSpacesAndSlashes() {
        assertEquals("search/50%25%20off", Routes.search("50% off"))
        assertEquals("search/a%2Fb", Routes.search("a/b"))
    }

    @Test
    fun search_trimsTheQuery() {
        assertEquals("search/shoes", Routes.search("  shoes "))
    }

    @Test
    fun productDetails_encodesIdsWithSpecialCharacters() {
        assertEquals("product-details/abc%24def", Routes.productDetails("abc\$def"))
    }

    @Test
    fun categoryProducts_keepsOrdinaryIdsReadable() {
        assertEquals("category-products/phones", Routes.categoryProducts("phones"))
    }

    @Test
    fun constants_matchTheComposableRoutes() {
        assertEquals("home", Routes.HOME)
        assertEquals("checkout", Routes.CHECKOUT)
        assertTrue(Routes.PRODUCT_DETAILS.startsWith("product-details/"))
        assertTrue(Routes.CATEGORY_PRODUCTS.startsWith("category-products/"))
    }

    @Test
    fun homeIsTheRootTab() {
        assertFalse(Routes.HOME.startsWith("/"))
    }
}
