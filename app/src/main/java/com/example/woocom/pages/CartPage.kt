package com.example.woocom.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.woocom.AppUtil
import com.example.woocom.Routes
import com.example.woocom.components.CartItemView
import com.example.woocom.components.PremiumBackground
import com.example.woocom.model.ProductModel
import com.example.woocom.model.UserModel
import com.example.woocom.ui.theme.CardSurface
import com.example.woocom.ui.theme.DarkText
import com.example.woocom.ui.theme.GreenPrimary
import com.example.woocom.ui.theme.NeonBorder
import com.example.woocom.ui.theme.PrimaryText
import com.example.woocom.ui.theme.SecondaryText
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartPage(navController: NavHostController, onGoHome: () -> Unit = {}) {
    var userModel by remember { mutableStateOf<UserModel?>(null) }
    var cartProducts by remember { mutableStateOf<Map<String, ProductModel>>(emptyMap()) }
    var isLoading by remember { mutableStateOf(true) }
    var totalPrice by remember { mutableStateOf(0.0) }
    var refreshTrigger by remember { mutableIntStateOf(0) }

    LaunchedEffect(refreshTrigger) {
        try {
            val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return@LaunchedEffect
            val userSnapshot = Firebase.firestore.collection("user")
                .document(uid)
                .get()
                .await()
            val user = userSnapshot.toObject(UserModel::class.java)

            if (user != null) {
                userModel = user

                val productMap = mutableMapOf<String, ProductModel>()
                var total = 0.0

                for (productId in user.cartItems.keys) {
                    val quantity = user.cartItems[productId] ?: 0L
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
                        total += AppUtil.parsePrice(product.price) * quantity
                    }
                }

                totalPrice = total
                cartProducts = productMap
            }

            isLoading = false
        } catch (e: Exception) {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.ShoppingCart,
                            contentDescription = null,
                            tint = GreenPrimary
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Your Cart", color = PrimaryText)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = PrimaryText
                )
            )
        },
        containerColor = Color.Transparent
    ) { paddingValues ->
        PremiumBackground {
            Box(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
            ) {
                when {
                    isLoading -> CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = GreenPrimary
                    )

                    userModel == null || userModel!!.cartItems.isEmpty() -> {
                        EmptyCartView(
                            onBrowseClick = onGoHome,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }

                    else -> CartContent(
                        cartItems = userModel!!.cartItems,
                        cartProducts = cartProducts,
                        totalPrice = totalPrice,
                        onCartUpdated = { refreshTrigger++ },
                        onCheckout = {
                            navController.navigate(Routes.checkout(totalPrice + SHIPPING_COST))
                        }
                    )
                }
            }
        }
    }
}

private const val SHIPPING_COST = 49.0

@Composable
private fun CartContent(
    cartItems: Map<String, Long>,
    cartProducts: Map<String, ProductModel>,
    totalPrice: Double,
    onCartUpdated: () -> Unit,
    onCheckout: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            items(cartItems.keys.toList(), key = { it }) { productId ->
                CartItemView(
                    productId = productId,
                    quantity = cartItems[productId] ?: 0L,
                    product = cartProducts[productId],
                    onCartUpdated = onCartUpdated
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        OrderSummaryCard(totalPrice = totalPrice, onCheckout = onCheckout)
    }
}

@Composable
private fun OrderSummaryCard(totalPrice: Double, onCheckout: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, NeonBorder),
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
                color = PrimaryText
            )

            Spacer(modifier = Modifier.height(12.dp))

            SummaryRow(label = "Subtotal", value = AppUtil.formatPrice(totalPrice))
            Spacer(modifier = Modifier.height(8.dp))
            SummaryRow(label = "Shipping", value = AppUtil.formatPrice(SHIPPING_COST))

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = SecondaryText.copy(alpha = 0.2f))
            Spacer(modifier = Modifier.height(8.dp))

            SummaryRow(
                label = "Total",
                value = AppUtil.formatPrice(totalPrice + SHIPPING_COST),
                emphasize = true
            )
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    Button(
        onClick = onCheckout,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = GreenPrimary,
            contentColor = DarkText
        ),
        shape = RoundedCornerShape(14.dp)
    ) {
        Text(
            text = "Proceed to Checkout",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun SummaryRow(label: String, value: String, emphasize: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = if (emphasize) 18.sp else 16.sp,
            fontWeight = if (emphasize) FontWeight.Bold else FontWeight.Normal,
            color = PrimaryText
        )
        Text(
            text = value,
            fontSize = if (emphasize) 18.sp else 16.sp,
            fontWeight = if (emphasize) FontWeight.Bold else FontWeight.Medium,
            color = if (emphasize) GreenPrimary else PrimaryText
        )
    }
}

@Composable
private fun EmptyCartView(onBrowseClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.ShoppingCart,
            contentDescription = null,
            modifier = Modifier.size(100.dp),
            tint = SecondaryText
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Your cart is empty",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = PrimaryText
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Add items to your cart to continue shopping",
            fontSize = 14.sp,
            color = SecondaryText
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onBrowseClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = GreenPrimary,
                contentColor = DarkText
            ),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text(
                text = "Browse Products",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}