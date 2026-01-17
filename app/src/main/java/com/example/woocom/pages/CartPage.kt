package com.example.woocom.pages

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.woocom.AppNavigation
import com.example.woocom.GlobalNavigation
import com.example.woocom.components.CartItemView
import com.example.woocom.components.parsePrice
import com.example.woocom.model.ProductModel
import com.example.woocom.model.UserModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await

// Updated CartPage.kt
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartPage(modifier: Modifier = Modifier) {
    var userModel by remember { mutableStateOf<UserModel?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var cartProducts by remember { mutableStateOf<Map<String, ProductModel>>(emptyMap()) }
    var totalPrice by remember { mutableStateOf(0.0) }
    var refreshTrigger by remember { mutableStateOf(0) } // Add refresh trigger

    // Function to refresh cart data
    fun refreshCartData() {
        isLoading = true
        refreshTrigger += 1
    }

    LaunchedEffect(key1 = refreshTrigger) { // Update key to refreshTrigger
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
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Your Cart", color = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        containerColor = Color.Transparent
    ) { paddingValues ->
        com.example.woocom.components.PremiumBackground {
            Box(
                modifier = modifier
                    .padding(paddingValues)
                    .fillMaxSize()
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
                                quantity = userModel!!.cartItems[productId]!!,
                                onCartUpdated = { refreshCartData() } // Pass refresh callback
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Order Summary
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(4.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = com.example.woocom.ui.theme.CardSurface
                        ),
                        border = androidx.compose.foundation.BorderStroke(0.5.dp, com.example.woocom.ui.theme.NeonBorder),
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
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Subtotal",
                                    fontSize = 16.sp,
                                    color = Color.White
                                )
                                Text(
                                    text = "₹${String.format("%.2f", totalPrice)}",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.White
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Shipping",
                                    fontSize = 16.sp,
                                    color = Color.White
                                )
                                Text(
                                    text = "₹49",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.White
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
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "₹${String.format("%.2f", totalPrice + 49)}",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = com.example.woocom.ui.theme.GreenPrimary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Checkout Button
                    Button(
                        onClick = {
                            val finalAmount = totalPrice + 49
                            GlobalNavigation.navController.navigate("checkout/$finalAmount")
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
            color = Color.White
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

// Updated CartItemView.kt
@Composable
fun CartItemView(
    modifier: Modifier = Modifier,
    productId: String,
    quantity: Long,
    onCartUpdated: () -> Unit // Add callback parameter
) {
    var product by remember { mutableStateOf<ProductModel?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    val context = LocalContext.current

    // Fetch product details
    LaunchedEffect(key1 = productId) {
        Firebase.firestore
            .collection("data")
            .document("stock")
            .collection("products")
            .whereEqualTo("id", productId)
            .get().addOnCompleteListener { task ->
                isLoading = false
                if (task.isSuccessful) {
                    val result = task.result.toObjects(ProductModel::class.java)
                    if (result.isNotEmpty()) {
                        product = result.first()
                    }
                }
            }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(
            containerColor = com.example.woocom.ui.theme.CardSurface
        ),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, com.example.woocom.ui.theme.NeonBorder),
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = GreenPrimary)
                }
            } else if (product == null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Product not found")
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Product Image
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.LightGray)
                    ) {
                        if (product!!.images.isNotEmpty()) {
                            AsyncImage(
                                model = product!!.images.first(),
                                contentDescription = "Product image",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    // Product Details
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp)
                    ) {
                        Text(
                            text = product!!.title,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            color = Color.White
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "₹${product!!.price}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = com.example.woocom.ui.theme.GreenPrimary
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        Text(
                            text = "₹${product!!.actualPrice}",
                            fontSize = 14.sp,
                            color = Color.Gray,
                            textDecoration = TextDecoration.LineThrough
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Calculate item total - Using parsePrice helper to handle commas
                        val priceValue = parsePrice(product!!.price)
                        val itemTotal = priceValue * quantity
                        Text(
                            text = "Total: ₹$itemTotal",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White
                        )
                    }

                    // Quantity Controls
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(modifier = Modifier.height(32.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = {
                                    updateCartQuantity(productId, quantity - 1) {
                                        onCartUpdated() // Call refresh callback
                                    }
                                    AppUtil.showToast(context, "Quantity updated")
                                },
                                enabled = quantity > 1,
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(
                                        if (quantity > 1) GreenPrimary.copy(alpha = 0.2f) else Color.Gray.copy(alpha = 0.1f),
                                        RoundedCornerShape(8.dp)
                                    )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowDown,
                                    contentDescription = "Decrease quantity",
                                    tint = if (quantity > 1) Color.Black else Color.Gray
                                )
                            }

                            Text(
                                text = "$quantity",
                                modifier = Modifier.padding(horizontal = 8.dp),
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )

                            IconButton(
                                onClick = {
                                    updateCartQuantity(productId, quantity + 1) {
                                        onCartUpdated() // Call refresh callback
                                    }
                                    AppUtil.showToast(context, "Quantity updated")
                                },
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(GreenPrimary.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowUp,
                                    contentDescription = "Increase quantity",
                                    tint = Color.Black
                                )
                            }
                        }
                    }
                }

                // Delete Button at Top Right Corner
                IconButton(
                    onClick = {
                        removeFromCart(productId) {
                            onCartUpdated() // Call refresh callback
                        }
                        AppUtil.showToast(context, "Item removed from cart")
                    },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(36.dp)
                        .background(Color.Red.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Remove from cart",
                        tint = Color.Gray
                    )
                }
            }
        }
    }
}

// Helper function to safely parse prices with commas
fun parsePrice(priceString: String): Double {
    return try {
        val cleanPrice = priceString.replace(",", "").trim()
        cleanPrice.toDouble()
    } catch (e: NumberFormatException) {
        0.0
    }
}

// Updated utility functions with callbacks
fun updateCartQuantity(productId: String, quantity: Long, onSuccess: () -> Unit) {
    val userDoc = Firebase.firestore.collection("user")
        .document(FirebaseAuth.getInstance().currentUser?.uid!!)

    if (quantity <= 0) {
        removeFromCart(productId, onSuccess)
    } else {
        val updatedCart = mapOf("cartItems.$productId" to quantity)
        userDoc.update(updatedCart)
            .addOnSuccessListener {
                onSuccess()
            }
    }
}

private fun removeFromCart(productId: String, onSuccess: () -> Unit) {
    val userDoc = Firebase.firestore.collection("user")
        .document(FirebaseAuth.getInstance().currentUser?.uid!!)

    val updates = mapOf("cartItems.$productId" to com.google.firebase.firestore.FieldValue.delete())
    userDoc.update(updates)
        .addOnSuccessListener {
            onSuccess()
        }
}

// Updated AppUtil.kt with complete removeFromCart function
object AppUtil {
    fun showToast(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }

    fun addToCart(productId: String, context: Context){
        val userDoc = Firebase.firestore.collection("user")
            .document(FirebaseAuth.getInstance().currentUser?.uid!!)
        userDoc.get().addOnCompleteListener {
            if(it.isSuccessful){
                val currentCart = it.result.get("cartItems") as? Map<String,Long> ?: emptyMap()
                //Check if current quantity is initialized or not, if not, initialize with 0
                val currentQuantity = currentCart[productId]?:0
                val updatedQuantity = currentQuantity + 1
                //Updating the quantity
                val updatedCart = mapOf("cartItems.$productId" to updatedQuantity)
                //Push this updated quantity to Firebase
                userDoc.update(updatedCart)
                    .addOnCompleteListener {
                        if(it.isSuccessful){
                            showToast(context, "Item added to cart")
                        }else{
                            showToast(context, "Failed to add item to cart")
                        }
                    }
            }
        }
    }

    fun removeFromCart(productId: String, context: Context) {
        val userDoc = Firebase.firestore.collection("user")
            .document(FirebaseAuth.getInstance().currentUser?.uid!!)

        val updates = mapOf("cartItems.$productId" to com.google.firebase.firestore.FieldValue.delete())
        userDoc.update(updates)
            .addOnCompleteListener {
                if(it.isSuccessful){
                    showToast(context, "Item removed from cart")
                }else{
                    showToast(context, "Failed to remove item from cart")
                }
            }
    }
}