package com.example.woocom.data

/**
 * In-memory hand-off between [com.example.woocom.pages.CheckoutPage], which creates a
 * `pending` order, and [com.example.woocom.MainActivity], which owns the Razorpay
 * `PaymentResultListener` callbacks and has no other way to learn which order the
 * payment belongs to.
 *
 * Process death mid-payment simply leaves the order in `pending`, which is the correct
 * conservative state for an unconfirmed payment.
 */
object PaymentSession {
    @Volatile
    var pendingOrderId: String? = null
}
