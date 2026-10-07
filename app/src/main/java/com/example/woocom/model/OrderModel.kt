package com.example.woocom.model

/**
 * A completed (or in-flight) purchase.
 *
 * Written to the `orders` collection after a successful payment so the Orders screen is
 * backed by real data instead of a hard-coded empty state.
 */
data class OrderModel(
    val orderId: String = "",
    val userId: String = "",
    val paymentId: String = "",
    val amount: Double = 0.0,
    val itemCount: Int = 0,
    val items: Map<String, Long> = emptyMap(),
    val status: String = STATUS_PENDING,
    val createdAt: Long = 0L,
    val failureReason: String = ""
) {
    companion object {
        const val STATUS_PENDING = "pending"
        const val STATUS_PAID = "paid"
        const val STATUS_FAILED = "failed"
    }
}
