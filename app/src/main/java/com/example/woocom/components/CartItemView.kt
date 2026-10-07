package com.example.woocom.components

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import coil.compose.AsyncImage
import com.example.woocom.AppUtil
import com.example.woocom.model.ProductModel
import com.example.woocom.ui.theme.CardSurface
import com.example.woocom.ui.theme.DarkText
import com.example.woocom.ui.theme.GreenPrimary
import com.example.woocom.ui.theme.NeonBorder
import com.example.woocom.ui.theme.PrimaryText
import com.example.woocom.ui.theme.SecondaryText

/**
 * Single cart row. Purely presentational: data and mutations come from [CartViewModel]
 * through the callbacks, so this composable holds no Firebase references and can be
 * previewed / screenshot-tested in isolation.
 */
@Composable
fun CartItemView(
    productId: String,
    quantity: Long,
    modifier: Modifier = Modifier,
    product: ProductModel? = null,
    onQuantityChanged: (Long) -> Unit = {},
    onRemove: () -> Unit = {},
) {
    Card(
        modifier = modifier.fillMaxWidth().padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, NeonBorder),
        shape = RoundedCornerShape(16.dp),
    ) {
        when (product) {
            null -> NotFoundBox()
            else ->
                CartItemContent(
                    product = product,
                    quantity = quantity,
                    onQuantityChanged = onQuantityChanged,
                    onRemove = onRemove,
                )
        }
    }
}

@Composable
private fun NotFoundBox() {
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(120.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text("This product is no longer available", color = SecondaryText)
    }
}

@Composable
private fun CartItemContent(
    product: ProductModel,
    quantity: Long,
    onQuantityChanged: (Long) -> Unit,
    onRemove: () -> Unit,
) {
    Box(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier =
                    Modifier
                        .size(100.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.LightGray),
            ) {
                if (product.images.isNotEmpty()) {
                    AsyncImage(
                        model = product.images.first(),
                        contentDescription = product.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier =
                    Modifier
                        .weight(1f)
                        .padding(end = 8.dp),
            ) {
                Text(
                    text = product.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = PrimaryText,
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "₹${product.price}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = GreenPrimary,
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "₹${product.actualPrice}",
                    fontSize = 14.sp,
                    color = SecondaryText,
                    textDecoration = TextDecoration.LineThrough,
                )

                Spacer(modifier = Modifier.height(8.dp))

                val itemTotal = AppUtil.lineTotal(product.price, quantity)
                Text(
                    text = "Total: ${AppUtil.formatPrice(itemTotal)}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = PrimaryText,
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Spacer(modifier = Modifier.height(32.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    QuantityButton(
                        icon = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Decrease quantity",
                        tint = if (quantity > 1) DarkText else Color.Gray,
                        enabled = quantity > 1,
                        background =
                            if (quantity > 1) {
                                GreenPrimary.copy(alpha = 0.25f)
                            } else {
                                Color.Gray.copy(alpha = 0.1f)
                            },
                        onClick = { onQuantityChanged(quantity - 1) },
                    )

                    Text(
                        text = "$quantity",
                        modifier = Modifier.padding(horizontal = 8.dp),
                        fontWeight = FontWeight.Bold,
                        color = PrimaryText,
                    )

                    QuantityButton(
                        icon = Icons.Default.KeyboardArrowUp,
                        contentDescription = "Increase quantity",
                        tint = DarkText,
                        enabled = true,
                        background = GreenPrimary.copy(alpha = 0.25f),
                        onClick = { onQuantityChanged(quantity + 1) },
                    )
                }
            }
        }

        IconButton(
            onClick = onRemove,
            modifier =
                Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .size(36.dp)
                    .background(Color.Red.copy(alpha = 0.1f), RoundedCornerShape(8.dp)),
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Remove ${product.title} from cart",
                tint = Color.Gray,
            )
        }
    }
}

@Composable
private fun QuantityButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    tint: Color,
    enabled: Boolean,
    background: Color,
    onClick: () -> Unit,
) {
    IconButton(
        onClick = onClick,
        enabled = enabled,
        modifier =
            Modifier
                .size(32.dp)
                .background(background, RoundedCornerShape(8.dp)),
    ) {
        Icon(imageVector = icon, contentDescription = contentDescription, tint = tint)
    }
}
