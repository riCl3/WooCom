package com.example.woocom

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.woocom.ui.theme.WooComTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.razorpay.PaymentResultListener

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

    override fun onPaymentSuccess(razorpayPaymentId: String?) {
        AppUtil.showToast(this, "Payment Successful")

        // Clear the cart after a successful order.
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()
        db.collection("user")
            .whereEqualTo("userId", uid)
            .get()
            .addOnSuccessListener { snapshot ->
                if (!snapshot.isEmpty) {
                    db.collection("user").document(snapshot.documents.first().id)
                        .update("cartItems", emptyMap<String, Any>())
                }
            }
    }

    override fun onPaymentError(errorCode: Int, response: String?) {
        AppUtil.showToast(this, "Payment Failed")
    }
}