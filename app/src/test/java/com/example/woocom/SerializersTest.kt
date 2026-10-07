package com.example.woocom

import com.example.woocom.model.AnyMapAsJsonObject
import com.example.woocom.model.IsoDateTimeAsEpochMillis
import com.example.woocom.model.OrderModel
import com.example.woocom.model.ProductModel
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

/**
 * Covers the two Postgres ⇄ Firestore shape bridges and the row mappings the Supabase
 * repositories rely on — everything that would otherwise only fail against a live backend.
 */
class SerializersTest {
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun isoDateTime_readsUtcOffsetTimestamp() {
        val decoded =
            json.decodeFromString(
                IsoDateTimeAsEpochMillis,
                "\"2026-10-07T09:15:00+00:00\"",
            )

        assertEquals(Instant.parse("2026-10-07T09:15:00Z").toEpochMilli(), decoded)
    }

    @Test
    fun isoDateTime_readsFractionalSeconds() {
        val decoded =
            json.decodeFromString(
                IsoDateTimeAsEpochMillis,
                "\"2026-10-07T09:15:00.123456+00:00\"",
            )

        assertEquals(Instant.parse("2026-10-07T09:15:00.123456Z").toEpochMilli(), decoded)
    }

    @Test
    fun isoDateTime_unparseableValueFallsBackToEpochZero() {
        val decoded = json.decodeFromString(IsoDateTimeAsEpochMillis, "\"not-a-timestamp\"")

        assertEquals(0L, decoded)
    }

    @Test
    fun isoDateTime_writesAnInstantThatReadsBack() {
        val epoch = Instant.parse("2026-10-07T09:15:00Z").toEpochMilli()

        val encoded = json.encodeToString(IsoDateTimeAsEpochMillis, epoch)
        val roundTrip = json.decodeFromString(IsoDateTimeAsEpochMillis, encoded)

        assertEquals(epoch, roundTrip)
    }

    @Test
    fun productModel_decodesASnakeCasePostgresRow() {
        val product =
            json.decodeFromString<ProductModel>(
                """
                {
                  "id": "p-aurora-x",
                  "title": "Aurora X 5G",
                  "description": "6.7\" AMOLED",
                  "category": "phones",
                  "price": "34999",
                  "actual_price": "39999",
                  "images": ["https://example.com/1.jpg"],
                  "other_details": {"RAM": "16GB", "Refresh rate": 120, "OLED": true},
                  "not_in_the_model": "ignored"
                }
                """.trimIndent(),
            )

        assertEquals("p-aurora-x", product.id)
        assertEquals("39999", product.actualPrice)
        assertEquals(listOf("https://example.com/1.jpg"), product.images)
        assertEquals("16GB", product.otherDetails["RAM"])
        assertEquals(120L, product.otherDetails["Refresh rate"])
        assertEquals(true, product.otherDetails["OLED"])
    }

    @Test
    fun orderModel_decodesAPostgresRowWithTimestampAndIds() {
        val order =
            json.decodeFromString<OrderModel>(
                """
                {
                  "id": "0f6b1f0e-0000-4000-8000-000000000001",
                  "user_id": "user-1",
                  "payment_id": "pay_123",
                  "razorpay_order_id": "order_ABC",
                  "amount": 75.0,
                  "item_count": 2,
                  "status": "paid",
                  "created_at": "2026-10-07T09:15:00.123456+00:00",
                  "failure_reason": "",
                  "items": {"p-aurora-x": 2}
                }
                """.trimIndent(),
            )

        assertEquals("0f6b1f0e-0000-4000-8000-000000000001", order.orderId)
        assertEquals("user-1", order.userId)
        assertEquals("order_ABC", order.razorpayOrderId)
        assertEquals(OrderModel.STATUS_PAID, order.status)
        assertEquals(mapOf("p-aurora-x" to 2L), order.items)
        assertEquals(Instant.parse("2026-10-07T09:15:00.123456Z").toEpochMilli(), order.createdAt)
    }

    @Test
    fun anyMap_widensJsonScalarsAndRoundTrips() {
        val decoded =
            json.decodeFromString(
                AnyMapAsJsonObject,
                """{"name": "Aurora", "count": 3, "ratio": 1.5, "active": true}""",
            )

        assertEquals("Aurora", decoded["name"])
        assertEquals(3L, decoded["count"])
        assertEquals(1.5, decoded["ratio"] as Double, 0.0)
        assertEquals(true, decoded["active"])

        val roundTrip = json.decodeFromString(AnyMapAsJsonObject, json.encodeToString(AnyMapAsJsonObject, decoded))
        assertEquals(decoded, roundTrip)
    }

    @Test
    fun anyMap_handlesNestedObjects() {
        val decoded =
            json.decodeFromString(
                AnyMapAsJsonObject,
                """{"display": {"size": 6.7, "type": "AMOLED"}, "bullets": ["a", "b"]}""",
            )

        @Suppress("UNCHECKED_CAST")
        val display = decoded["display"] as Map<String, Any>
        assertEquals(6.7, display["size"] as Double, 0.0)
        assertEquals("AMOLED", display["type"])
        assertEquals(listOf("a", "b"), decoded["bullets"])
        assertTrue(decoded.containsKey("display"))
    }
}
