package com.example.woocom

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import com.example.woocom.data.PaymentSession
import com.example.woocom.data.ServiceLocator
import com.example.woocom.ui.theme.WooComTheme
import com.razorpay.PaymentData
import com.razorpay.PaymentResultWithDataListener
import kotlinx.coroutines.launch

/**
 * Owns the Razorpay callbacks and settles the pending order created by CheckoutPage.
 *
 * Only [PaymentResultWithDataListener] is implemented on purpose: the Razorpay SDK checks
 * for [com.razorpay.PaymentResultListener] first and short-circuits to it when present,
 * which would drop `PaymentData` — and with it the `razorpay_signature` the Supabase
 * `verify-payment` function needs to re-derive the HMAC server-side.
 */
class MainActivity : ComponentActivity(), PaymentResultWithDataListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WooComTheme {
                AppNavigation()
            }
        }
    }

    override fun onPaymentSuccess(
        razorpayPaymentId: String?,
        paymentData: PaymentData?,
    ) {
        val session = PaymentSession.session ?: return
        val paymentId = paymentData?.paymentId ?: razorpayPaymentId.orEmpty()
        val signature = paymentData?.signature
        lifecycleScope.launch {
            runCatching {
                ServiceLocator.paymentGateway.settlePayment(session, paymentId, signature)
                // Only after the order is marked paid: if this write fails the cart stays
                // intact and the purchase is recoverable rather than silently lost.
                runCatching { ServiceLocator.userRepository.clearCart() }
            }.onFailure { failure ->
                AppUtil.showToast(
                    this@MainActivity,
                    "Payment received but could not be recorded: ${failure.localizedMessage}",
                )
            }
            PaymentSession.session = null
            AppUtil.showToast(this@MainActivity, "Payment Successful")
        }
    }

    override fun onPaymentError(
        errorCode: Int,
        response: String?,
        paymentData: PaymentData?,
    ) {
        val session = PaymentSession.session ?: return
        val reason = "code=$errorCode ${response.orEmpty()}".trim()
        lifecycleScope.launch {
            runCatching { ServiceLocator.paymentGateway.failPayment(session, reason) }
            PaymentSession.session = null
            AppUtil.showToast(this@MainActivity, "Payment Failed")
        }
    }
}
