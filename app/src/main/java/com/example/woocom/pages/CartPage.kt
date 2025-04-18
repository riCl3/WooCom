package com.example.woocom.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.woocom.components.CartItemView
import com.example.woocom.components.parsePrice
import com.example.woocom.model.ProductModel
import com.example.woocom.model.UserModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartPage(modifier: Modifier = Modifier) {
    var userModel by remember { mutableStateOf<UserModel?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var cartProducts by remember { mutableStateOf<Map<String, ProductModel>>(emptyMap()) }
    var totalPrice by remember { mutableStateOf(0.0) }

    LaunchedEffect(key1 = Unit) {
        try {
            val userDocRef = Firebase.firestore.collection("user")
                .document(FirebaseAuth.getInstance().currentUser?.uid!!)

            val userSnapshot = userDocRef.get().await()
            val user = userSnapshot.toObject(UserModel::class.java)

            if (user != null) {
                userModel = user

                // Fetch product details for each cart item
                val productMap = mutableMapOf<String, ProductModel>()
                var total = 0.0

                for (productId in user.cartItems.keys) {
                    val quantity = user.cartItems[productId] ?: 0
                    val productSnapshot = Firebase.firestore
                        .collection("data")
                        .document("stock")
                        .collection("products")
                        .whereEqualTo("id", productId)
                        .get()
                        .await()

                    val products = productSnapshot.toObjects(ProductModel::class.java)
                    if (products.isNotEmpty()) {
                        val product = products.first()
                        productMap[productId] = product
                        // Use parsePrice helper to safely convert price strings with commas
                        total += parsePrice(product.price) * quantity
                    }
                }

                cartProducts = productMap
                totalPrice = total
            }

            isLoading = false
        } catch (e: Exception) {
            isLoading = false
            // Handle error
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.ShoppingCart,
                            contentDescription = "Cart",
                            tint = DarkText
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Your Cart", color = DarkText)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = modifier
                .padding(paddingValues)
                .fillMaxSize()
                .background(Color(0xFFF5F5F5))
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = GreenPrimary
                )
            } else if (userModel == null || userModel!!.cartItems.isEmpty()) {
                EmptyCartView(modifier = Modifier.align(Alignment.Center))
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    // Cart Items
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        items(userModel!!.cartItems.keys.toList()) { productId ->
                            CartItemView(
                                productId = productId,
                                quantity = userModel!!.cartItems[productId]!!
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Order Summary
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(4.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "Order Summary",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Subtotal",
                                    fontSize = 16.sp
                                )
                                Text(
                                    text = "₹${String.format("%.2f", totalPrice)}",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Shipping",
                                    fontSize = 16.sp
                                )
                                Text(
                                    text = "₹49",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Divider()
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Total",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "₹${String.format("%.2f", totalPrice + 49)}",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DarkText
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Checkout Button
                    Button(
                        onClick = { /* TODO: Implement checkout */ },
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
}

@Composable
fun EmptyCartView(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.ShoppingCart,
            contentDescription = "Empty Cart",
            modifier = Modifier.size(100.dp),
            tint = Color.LightGray
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Your cart is empty",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = DarkText
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Add items to your cart to continue shopping",
            fontSize = 16.sp,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { /* TODO: Navigate to home/products */ },
            colors = ButtonDefaults.buttonColors(
                containerColor = GreenPrimary,
                contentColor = DarkText
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = "Browse Products",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}