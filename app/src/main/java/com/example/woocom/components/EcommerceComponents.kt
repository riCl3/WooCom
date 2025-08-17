package com.example.woocom.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.woocom.GlobalNavigation
import com.example.woocom.model.ProductModel
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObject
import kotlin.random.Random

// Deals of the Day Component
@Composable
fun DealsOfTheDayView(modifier: Modifier = Modifier) {
    var productList by remember { mutableStateOf(listOf<ProductModel>()) }

    LaunchedEffect(Unit) {
        Firebase.firestore.collection("data")
            .document("stock")
            .collection("products")
            .limit(10)
            .get().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val resultList = task.result.documents.mapNotNull { doc ->
                        doc.toObject(ProductModel::class.java)
                    }
                    productList = resultList
                }
            }
    }

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(productList) { item ->
            DealProductItem(product = item)
        }
    }
}

@Composable
fun DealProductItem(product: ProductModel) {
    // Calculate discount percentage
    val discount = try {
        val actual = product.actualPrice.toDouble()
        val current = product.price.toDouble()
        if (actual > current) {
            ((actual - current) / actual * 100).toInt()
        } else 0
    } catch (e: Exception) {
        Random.nextInt(10, 50) // Random discount for demo
    }

    Card(
        modifier = Modifier
            .width(160.dp)
            .height(220.dp)
            .clickable {
                GlobalNavigation.navController.navigate("product-details/${product.id}")
            },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.elevatedCardElevation(6.dp)
    ) {
        Column(
            modifier = Modifier.padding(8.dp)
        ) {
            AsyncImage(
                model = if (product.images.isNotEmpty()) product.images[0] else "",
                contentDescription = "Product Image",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = product.title,
                style = TextStyle(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                ),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row {
                Text(
                    text = "₹${product.price}",
                    style = TextStyle(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFF4D00)
                    )
                )
                Spacer(modifier = Modifier.width(4.dp))
                if (product.actualPrice.isNotEmpty() && product.actualPrice != product.price) {
                    Text(
                        text = "₹${product.actualPrice}",
                        style = TextStyle(
                            fontSize = 12.sp,
                            color = Color.Gray,
                            textDecoration = TextDecoration.LineThrough
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            if (discount > 0) {
                Text(
                    text = "${discount}% OFF",
                    style = TextStyle(
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    ),
                    modifier = Modifier
                        .background(
                            Color(0xFFFF4D00),
                            shape = RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
    }
}

// Featured Products Component
@Composable
fun FeaturedProductsView(modifier: Modifier = Modifier) {
    var productList by remember { mutableStateOf(listOf<ProductModel>()) }

    LaunchedEffect(Unit) {
        Firebase.firestore.collection("data")
            .document("stock")
            .collection("products")
            .limit(10)
            .get().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val resultList = task.result.documents.mapNotNull { doc ->
                        doc.toObject(ProductModel::class.java)
                    }
                    productList = resultList
                }
            }
    }

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(productList) { item ->
            FeaturedProductItem(product = item)
        }
    }
}

@Composable
fun FeaturedProductItem(product: ProductModel) {
    val randomRating = remember { Random.nextDouble(3.5, 5.0) }

    Card(
        modifier = Modifier
            .width(140.dp)
            .height(180.dp)
            .clickable {
                GlobalNavigation.navController.navigate("product-details/${product.id}")
            },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.elevatedCardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(8.dp)
        ) {
            AsyncImage(
                model = if (product.images.isNotEmpty()) product.images[0] else "",
                contentDescription = "Product Image",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = product.title,
                style = TextStyle(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                ),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "₹${product.price}",
                style = TextStyle(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            )

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "⭐",
                    fontSize = 10.sp
                )
                Text(
                    text = String.format("%.1f", randomRating),
                    style = TextStyle(
                        fontSize = 10.sp,
                        color = Color.Gray
                    )
                )
            }
        }
    }
}

// Recently Viewed Component
@Composable
fun RecentlyViewedView(modifier: Modifier = Modifier) {
    var productList by remember { mutableStateOf(listOf<ProductModel>()) }

    LaunchedEffect(Unit) {
        Firebase.firestore.collection("data")
            .document("stock")
            .collection("products")
            .limit(8)
            .get().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val resultList = task.result.documents.mapNotNull { doc ->
                        doc.toObject(ProductModel::class.java)
                    }
                    productList = resultList
                }
            }
    }

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(productList) { item ->
            RecentlyViewedItem(product = item)
        }
    }
}

@Composable
fun RecentlyViewedItem(product: ProductModel) {
    Card(
        modifier = Modifier
            .width(100.dp)
            .height(140.dp)
            .clickable {
                GlobalNavigation.navController.navigate("product-details/${product.id}")
            },
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.elevatedCardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier.padding(6.dp)
        ) {
            AsyncImage(
                model = if (product.images.isNotEmpty()) product.images[0] else "",
                contentDescription = "Product Image",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(70.dp)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = product.title,
                style = TextStyle(
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium
                ),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = "₹${product.price}",
                style = TextStyle(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            )
        }
    }
}

// Recommended Products Component
@Composable
fun RecommendedView(modifier: Modifier = Modifier) {
    var productList by remember { mutableStateOf(listOf<ProductModel>()) }

    LaunchedEffect(Unit) {
        Firebase.firestore.collection("data")
            .document("stock")
            .collection("products")
            .limit(10)
            .get().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val resultList = task.result.documents.mapNotNull { doc ->
                        doc.toObject(ProductModel::class.java)
                    }
                    productList = resultList
                }
            }
    }

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(productList) { item ->
            RecommendedProductItem(product = item)
        }
    }
}

@Composable
fun RecommendedProductItem(product: ProductModel) {
    val randomRating = remember { Random.nextDouble(4.0, 5.0) }

    Card(
        modifier = Modifier
            .width(150.dp)
            .height(190.dp)
            .clickable {
                GlobalNavigation.navController.navigate("product-details/${product.id}")
            },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.elevatedCardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(8.dp)
        ) {
            AsyncImage(
                model = if (product.images.isNotEmpty()) product.images[0] else "",
                contentDescription = "Product Image",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = product.title,
                style = TextStyle(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                ),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "₹${product.price}",
                style = TextStyle(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "⭐",
                        fontSize = 10.sp
                    )
                    Text(
                        text = String.format("%.1f", randomRating),
                        style = TextStyle(
                            fontSize = 10.sp,
                            color = Color.Gray
                        )
                    )
                }

                Text(
                    text = "Recommended",
                    style = TextStyle(
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    ),
                    modifier = Modifier
                        .background(
                            Color.Green,
                            shape = RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 4.dp, vertical = 1.dp)
                )
            }
        }
    }
}