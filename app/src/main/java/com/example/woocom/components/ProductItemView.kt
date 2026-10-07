package com.example.woocom.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ShoppingCart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.woocom.Routes
import com.example.woocom.model.ProductModel
import com.example.woocom.ui.theme.DarkSurface
import com.example.woocom.ui.theme.GreenPrimary
import com.example.woocom.ui.theme.PriceRed
import com.example.woocom.ui.theme.PrimaryText
import com.example.woocom.ui.theme.SecondaryText

@Composable
fun ProductItemView(
    product: ProductModel,
    modifier: Modifier = Modifier,
    navController: NavHostController,
) {
    val discount = calculateDiscount(product.actualPrice, product.price)
    val addToCart = rememberAddToCart()

    Card(
        modifier =
            modifier
                .padding(8.dp)
                .clickable {
                    navController.navigate(Routes.productDetails(product.id)) {
                        launchSingleTop = true
                    }
                },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, GreenPrimary.copy(alpha = 0.3f)),
    ) {
        Column(
            modifier =
                Modifier
                    .padding(12.dp)
                    .fillMaxWidth(),
        ) {
            Box(
                modifier =
                    Modifier
                        .height(150.dp)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(GreenPrimary.copy(alpha = 0.06f)),
            ) {
                AppImage(
                    model = product.images.firstOrNull(),
                    contentDescription = product.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )

                if (discount > 0) {
                    Surface(
                        modifier =
                            Modifier
                                .padding(8.dp)
                                .align(Alignment.TopStart),
                        color = PriceRed,
                        shape = RoundedCornerShape(8.dp),
                    ) {
                        Text(
                            text = "-$discount%",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            fontSize = 12.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }

            Text(
                text = product.title,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = PrimaryText,
                modifier =
                    Modifier
                        .padding(top = 8.dp)
                        .fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "₹${product.price}",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = GreenPrimary,
                    )

                    if (discount > 0) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "₹${product.actualPrice}",
                            fontSize = 12.sp,
                            textDecoration = TextDecoration.LineThrough,
                            color = SecondaryText,
                        )
                    }
                }

                Box(
                    modifier =
                        Modifier
                            .size(36.dp)
                            .background(GreenPrimary.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Rounded.ShoppingCart,
                        contentDescription = "Add to Cart",
                        modifier =
                            Modifier
                                .size(20.dp)
                                .clickable { addToCart(product.id) },
                        tint = GreenPrimary,
                    )
                }
            }
        }
    }
}

private fun calculateDiscount(
    actualPrice: String,
    price: String,
): Int {
    return try {
        val actual = actualPrice.trim().replace("[^0-9.]".toRegex(), "").toDouble()
        val current = price.trim().replace("[^0-9.]".toRegex(), "").toDouble()
        if (actual > current && actual > 0.0) {
            ((actual - current) / actual * 100).toInt()
        } else {
            0
        }
    } catch (e: Exception) {
        0
    }
}
