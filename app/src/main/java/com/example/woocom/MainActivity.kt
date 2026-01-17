package com.example.woocom

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.woocom.ui.theme.WooComTheme
import com.razorpay.PaymentResultListener

class MainActivity : ComponentActivity(), PaymentResultListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WooComTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    AppNavigation(Modifier.padding(innerPadding))
                }
            }
        }
    }

    override fun onPaymentSuccess(p0: String?) {
        AppUtil.showToast(this, "Payment Successful")
        
        val uid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
        if (uid != null) {
            val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
            db.collection("user")
                .whereEqualTo("userId", uid)
                .get()
                .addOnSuccessListener { snapshot ->
                     if (!snapshot.isEmpty) {
                         val docId = snapshot.documents[0].id
                         db.collection("user").document(docId)
                            .update("cartItems", emptyMap<String, Any>())
                     }
                }
        }
    }

    override fun onPaymentError(p0: Int, p1: String?) {
        AppUtil.showToast(this, "Payment Failed")
    }
}
