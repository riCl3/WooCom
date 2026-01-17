package com.example.woocom.pages

import android.app.Activity
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.woocom.GlobalNavigation
import com.example.woocom.components.CartItemView
import com.example.woocom.model.UserModel
import com.example.woocom.ui.theme.GreenPrimary
import com.example.woocom.ui.theme.DarkText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.razorpay.Checkout
import kotlinx.coroutines.tasks.await
import org.json.JSONObject

@Composable
fun CheckoutPage(totalAmount: Any) {
    val auth = FirebaseAuth.getInstance()
    val userId = auth.currentUser?.uid
    val db = FirebaseFirestore.getInstance()

    var user by remember { mutableStateOf<UserModel?>(null) }
    var userDocId by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    // Load user + cart
    LaunchedEffect(userId) {
        if (userId != null) {
            try {
                val snapshot = db.collection("user")
                    .whereEqualTo("userId", userId)
                    .get()
                    .await()
                if (!snapshot.isEmpty) {
                    val doc = snapshot.documents.first()
                    user = doc.toObject(UserModel::class.java)
                    userDocId = doc.id
                }
            } catch (e: Exception) {
                // Handle permission error or other exceptions
                Log.e("CheckoutPage", "Error loading user data", e)
            } finally {
                isLoading = false
            }
        } else {
             isLoading = false
        }
    }

    if (isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else if (user == null || userDocId == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("User not found")
        }
    } else {
        Column(Modifier.fillMaxSize().padding(16.dp)) {
            Text("Hello, ${user!!.name}", style = MaterialTheme.typography.titleLarge)

            Spacer(Modifier.height(16.dp))

            // Cart Items
            val cartItems = user!!.cartItems
            if (cartItems.isEmpty()) {
                Text("Your cart is empty")
            } else {
                cartItems.forEach { (productId, quantity) ->
                    CartItemView(productId = productId, quantity = quantity)
                }

                Spacer(Modifier.height(24.dp))

                // Place Order Button
                Button(
                    onClick = {
                        startPayment(totalAmount as Float)
                        // IMPORTANT: Cart clearing is now handled in MainActivity.onPaymentSuccess
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GreenPrimary,
                        contentColor = DarkText
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Place Order",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
fun razorPayApiKey() : String{
    return "rzp_test_R6KVQIP9lwJh8q"
}

fun startPayment(amount :  Float){
    val checkout = Checkout()
    checkout.setKeyID(razorPayApiKey())

    val options = JSONObject()
    options.put("name", "WooCom")
    options.put("description", "Payment")
    options.put("amount", amount*100)
    options.put("currency", "INR")

    // Using GlobalNavigation to get context, assuming it is an Activity context or we cast it
    // ideally should be passed from MainActivity or handled via callback
    checkout.open(GlobalNavigation.navController.context as Activity, options)

}
