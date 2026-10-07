package com.example.woocom

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import com.example.woocom.data.PaymentSession
import com.example.woocom.data.ServiceLocator
import com.example.woocom.ui.theme.WooComTheme
import com.razorpay.PaymentResultListener
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity(), PaymentResultListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WooComTheme {
                AppNavigation()
            }
        }
    }

    /**
     * The Razorpay SDK calls back into the Activity that opened checkout, so this is
     * where the pending order created by CheckoutPage is settled.
     *
     * Order of operations matters: the order is only flipped to `paid` before the cart
     * is cleared, so a failure to write the order leaves the cart intact and the order
     * recoverable rather than silently losing a purchase.
     */
    override fun onPaymentSuccess(razorpayPaymentId: String?) {
        val orderId = PaymentSession.pendingOrderId
        lifecycleScope.launch {
            if (orderId != null) {
                runCatching {
                    ServiceLocator.userRepository.markOrderPaid(orderId, orEmpty(razorpayPaymentId))
                }.onFailure { failure ->
                    AppUtil.showToast(
                        this@MainActivity,
                        "Payment received but could not be recorded: ${failure.localizedMessage}"
                    )
                }
            }
            runCatching { ServiceLocator.userRepository.clearCart() }
            PaymentSession.pendingOrderId = null
            AppUtil.showToast(this@MainActivity, "Payment Successful")
        }
    }

    override fun onPaymentError(errorCode: Int, response: String?) {
        val orderId = PaymentSession.pendingOrderId
        val reason = "code=$errorCode ${orEmpty(response)}".trim()
        lifecycleScope.launch {
            if (orderId != null) {
                runCatching { ServiceLocator.userRepository.markOrderFailed(orderId, reason) }
            }
            PaymentSession.pendingOrderId = null
            AppUtil.showToast(this@MainActivity, "Payment Failed")
        }
    }

    private fun orEmpty(value: String?): String = value.orEmpty()
}
