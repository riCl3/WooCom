package com.example.woocom.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import com.example.woocom.model.ProductModel
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.tbuonomo.viewpagerdotsindicator.compose.DotsIndicator
import com.tbuonomo.viewpagerdotsindicator.compose.model.DotGraphic
import com.tbuonomo.viewpagerdotsindicator.compose.type.ShiftIndicatorType
import kotlinx.coroutines.delay

// Custom theme colors
val GreenPrimary = Color(0xFFB7FF00)
val GreenSecondary = Color(0xFFA5E800)
val DarkText = Color(0xFF212121)
val LightGray = Color(0xFFEEEEEE)
val FavoriteRed = Color(0xFFE91E63)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailsPage(modifier: Modifier = Modifier, productId: String) {
    var product by remember { mutableStateOf<ProductModel?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(key1 = Unit) {
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Product Details", color = DarkText) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .background(Color.White)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = GreenPrimary
                )
            } else if (product == null) {
                Text(
                    text = "Product not found",
                    modifier = Modifier.align(Alignment.Center),
                    color = DarkText
                )
            } else {
                ProductContent(product = product!!, modifier = modifier)
            }
        }
    }
}

@Composable
fun ProductContent(product: ProductModel, modifier: Modifier = Modifier) {
    // Add a ScrollState to make content scrollable
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState) // Add vertical scroll
            .padding(16.dp)
    ) {
        // Images first as requested
        if (product.images.isNotEmpty()) {
            ImageCarouselWithFavorite(images = product.images)
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Title and price below images
        Text(
            text = product.title,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = DarkText
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Price information
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "₹${product.price}",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = DarkText
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = "₹${product.actualPrice}",
                fontSize = 16.sp,
                color = Color.Gray,
                style = TextStyle(textDecoration = TextDecoration.LineThrough)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Product Overview - Show this section even if empty for debugging
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = LightGray
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Overview",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkText
                )

                Spacer(modifier = Modifier.height(8.dp))

                if (product.otherDetails.isEmpty()) {
                    Text(
                        text = "No details available",
                        fontSize = 16.sp,
                        color = Color.Gray
                    )
                } else {
                    product.otherDetails.forEach { (key, value) ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            Text(
                                text = "$key:",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = DarkText
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Text(
                                text = value.toString(),
                                fontSize = 16.sp,
                                color = DarkText
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Product Description
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = LightGray
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Description",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkText
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = product.description.ifEmpty { "No description available" },
                    fontSize = 16.sp,
                    color = DarkText
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Add to cart button using the provided style
        GreenButton(
            text = "Add to Cart",
            onClick = { /* TODO: Add to cart functionality */ },
            isLoading = false,
            modifier = Modifier.fillMaxWidth()
        )

        // Add extra space at the bottom for better scrolling experience
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun GreenButton(
    text: String,
    onClick: () -> Unit,
    isLoading: Boolean = false,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .height(56.dp)
            .border(1.dp, Color.Black)
            .background(
                Brush.horizontalGradient(
                    colors = listOf(
                        GreenPrimary,
                        GreenPrimary
                    )
                )
            ),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent
        ),
        enabled = !isLoading
    ) {
        Text(
            text = if (isLoading) "Loading..." else text,
            style = TextStyle(fontSize = 20.sp, color = Color.Black)
        )
    }
}

@Composable
fun ImageCarouselWithFavorite(images: List<String>) {
    val pagerState = rememberPagerState(pageCount = { images.size })
    var isFavorite by remember { mutableStateOf(false) }

    // Auto-scrolling effect
    LaunchedEffect(Unit) {
        while (true) {
            delay(4000)
            if (images.isNotEmpty()) {
                val nextPage = (pagerState.currentPage + 1) % images.size
                pagerState.animateScrollToPage(nextPage)
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            HorizontalPager(
                state = pagerState,
                pageSpacing = 16.dp,
                modifier = Modifier.height(450.dp) // Increased height as requested
            ) { page ->
                Card(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(4.dp),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Box {
                        AsyncImage(
                            model = images[page],
                            contentDescription = "Product image ${page + 1}",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            DotsIndicator(
                dotCount = images.size,
                type = ShiftIndicatorType(
                    dotsGraphic = DotGraphic(
                        color = Color.DarkGray
                    )
                ),
                pagerState = pagerState
            )
        }

        // Favorite button at the bottom right corner of the image
        IconButton(
            onClick = { isFavorite = !isFavorite },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 24.dp, bottom = 80.dp)
                .size(48.dp)
                .background(Color.White.copy(alpha = 0.8f), CircleShape)
                .zIndex(10f)
        ) {
            Icon(
                imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                contentDescription = "Favorite",
                tint = if (isFavorite) FavoriteRed else Color.Gray,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}