package com.example.woocom.pages

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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

@Composable
fun CheckoutPage() {
    val auth = FirebaseAuth.getInstance()
    val userId = auth.currentUser?.uid
    val db = FirebaseFirestore.getInstance()

    var user by remember { mutableStateOf<UserModel?>(null) }
    var userDocId by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    // Load user + cart
    LaunchedEffect(userId) {
        if (userId != null) {
            val snapshot = db.collection("user")
                .whereEqualTo("userId", userId)
                .get()
                .await()
            if (!snapshot.isEmpty) {
                val doc = snapshot.documents.first()
                user = doc.toObject(UserModel::class.java)
                userDocId = doc.id
            }
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
                        db.collection("user")
                            .document(userDocId!!)
                            .update("cartItems", emptyMap<String, Long>())
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
                        text = "Proceed to Checkout",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
