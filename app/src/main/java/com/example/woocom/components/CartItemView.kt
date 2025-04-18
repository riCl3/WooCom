package com.example.woocom.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
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
import com.example.woocom.AppUtil
import com.example.woocom.model.ProductModel
import com.example.woocom.pages.GreenPrimary
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore

@Composable
fun CartItemView(modifier: Modifier = Modifier, productId: String, quantity: Long) {
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
            containerColor = Color.White
        ),
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
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "₹${product!!.price}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
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
                            fontWeight = FontWeight.Medium
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
                                    updateCartQuantity(productId, quantity - 1)
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
                                fontWeight = FontWeight.Bold
                            )

                            IconButton(
                                onClick = {
                                    updateCartQuantity(productId, quantity + 1)
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
                        removeFromCart(productId)
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

private fun updateCartQuantity(productId: String, quantity: Long) {
    val userDoc = Firebase.firestore.collection("user")
        .document(FirebaseAuth.getInstance().currentUser?.uid!!)

    if (quantity <= 0) {
        removeFromCart(productId)
    } else {
        val updatedCart = mapOf("cartItems.$productId" to quantity)
        userDoc.update(updatedCart)
    }
}

private fun removeFromCart(productId: String) {
    val userDoc = Firebase.firestore.collection("user")
        .document(FirebaseAuth.getInstance().currentUser?.uid!!)

    val updates = mapOf("cartItems.$productId" to com.google.firebase.firestore.FieldValue.delete())
    userDoc.update(updates)
}
