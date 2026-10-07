package com.example.woocom.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.woocom.Routes
import com.example.woocom.model.ProductModel
import com.example.woocom.ui.theme.GradientEnd
import com.example.woocom.ui.theme.GradientStart
import com.example.woocom.ui.theme.GreenPrimary
import com.example.woocom.ui.theme.PriceRed
import com.example.woocom.ui.theme.PrimaryText
import com.example.woocom.ui.theme.SecondaryText
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObject

/** App-wide premium dark gradient backdrop. */
@Composable
fun PremiumBackground(content: @Composable BoxScope.() -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(GradientStart, GradientEnd)
                )
            )
    ) {
        content()
    }
}

@Composable
fun DealsOfTheDayView(modifier: Modifier = Modifier, navController: NavHostController) {
    ProductRow(modifier = modifier, limit = 10) { product ->
        DealProductItem(product = product, navController = navController)
    }
}

@Composable
fun DealProductItem(product: ProductModel, navController: NavHostController) {
    val discount = calculateDiscount(product.actualPrice, product.price)

    NeonGlassCard(
        modifier = Modifier
            .width(160.dp)
            .height(230.dp)
            .clickable { navController.navigate(Routes.productDetails(product.id)) },
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            AsyncImage(
                model = product.images.firstOrNull(),
                contentDescription = product.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .clip(RoundedCornerShape(8.dp))
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = product.title,
                style = TextStyle(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = PrimaryText
                ),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "₹${product.price}",
                    style = TextStyle(
                        fontWeight = FontWeight.Bold,
                        color = GreenPrimary
                    )
                )
                Spacer(modifier = Modifier.width(4.dp))
                if (product.actualPrice.isNotBlank() && product.actualPrice != product.price) {
                    Text(
                        text = "₹${product.actualPrice}",
                        style = TextStyle(
                            fontSize = 12.sp,
                            color = SecondaryText,
                            textDecoration = TextDecoration.LineThrough
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            if (discount > 0) {
                Text(
                    text = "$discount% OFF",
                    style = TextStyle(
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    ),
                    modifier = Modifier
                        .background(PriceRed, RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
    }
}

@Composable
fun FeaturedProductsView(modifier: Modifier = Modifier, navController: NavHostController) {
    ProductRow(modifier = modifier, limit = 10) { product ->
        FeaturedProductItem(product = product, navController = navController)
    }
}

@Composable
fun FeaturedProductItem(product: ProductModel, navController: NavHostController) {
    GlassCard(
        modifier = Modifier
            .width(140.dp)
            .height(190.dp)
            .clickable { navController.navigate(Routes.productDetails(product.id)) },
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            AsyncImage(
                model = product.images.firstOrNull(),
                contentDescription = product.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .clip(RoundedCornerShape(8.dp))
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = product.title,
                style = TextStyle(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = PrimaryText
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
                    color = GreenPrimary
                )
            )
        }
    }
}

@Composable
fun RecentlyViewedView(modifier: Modifier = Modifier, navController: NavHostController) {
    ProductRow(modifier = modifier, limit = 8) { product ->
        RecentlyViewedItem(product = product, navController = navController)
    }
}

@Composable
fun RecentlyViewedItem(product: ProductModel, navController: NavHostController) {
    NeonGlassCard(
        modifier = Modifier
            .width(100.dp)
            .height(150.dp)
            .clickable { navController.navigate(Routes.productDetails(product.id)) },
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(6.dp)) {
            AsyncImage(
                model = product.images.firstOrNull(),
                contentDescription = product.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .clip(RoundedCornerShape(6.dp))
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = product.title,
                style = TextStyle(
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    color = PrimaryText
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
                    color = GreenPrimary
                )
            )
        }
    }
}

@Composable
fun RecommendedView(modifier: Modifier = Modifier, navController: NavHostController) {
    ProductRow(modifier = modifier, limit = 10) { product ->
        RecommendedProductItem(product = product, navController = navController)
    }
}

@Composable
fun RecommendedProductItem(product: ProductModel, navController: NavHostController) {
    GlassCard(
        modifier = Modifier
            .width(150.dp)
            .height(200.dp)
            .clickable { navController.navigate(Routes.productDetails(product.id)) },
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            AsyncImage(
                model = product.images.firstOrNull(),
                contentDescription = product.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .clip(RoundedCornerShape(8.dp))
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = product.title,
                style = TextStyle(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = PrimaryText
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
                    color = GreenPrimary
                )
            )
        }
    }
}

/** Internal helper that loads a product carousel from Firestore. */
@Composable
private fun ProductRow(
    modifier: Modifier,
    limit: Int,
    itemBuilder: @Composable (ProductModel) -> Unit
) {
    var productList by remember { mutableStateOf(listOf<ProductModel>()) }

    LaunchedEffect(Unit) {
        Firebase.firestore.collection("data")
            .document("stock")
            .collection("products")
            .limit(limit.toLong())
            .get().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    productList = task.result.documents.mapNotNull { doc ->
                        doc.toObject(ProductModel::class.java)
                    }
                }
            }
    }

    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(productList) { item -> itemBuilder(item) }
    }
}

private fun calculateDiscount(actualPrice: String, price: String): Int {
    return try {
        val actual = actualPrice.trim().replace("[^0-9.]".toRegex(), "").toDouble()
        val current = price.trim().replace("[^0-9.]".toRegex(), "").toDouble()
        if (actual > current && actual > 0.0) {
            ((actual - current) / actual * 100).toInt()
        } else 0
    } catch (e: NumberFormatException) {
        0
    }
}