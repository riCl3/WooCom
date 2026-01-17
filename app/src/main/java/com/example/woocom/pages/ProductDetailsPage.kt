package com.example.woocom.pages

import android.util.Log
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import com.example.woocom.AppUtil
import com.example.woocom.model.ProductModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.firestore
import com.tbuonomo.viewpagerdotsindicator.compose.DotsIndicator
import com.tbuonomo.viewpagerdotsindicator.compose.model.DotGraphic
import com.tbuonomo.viewpagerdotsindicator.compose.type.ShiftIndicatorType
import com.example.woocom.components.GlassCard
import com.example.woocom.components.NeonGlassCard
import kotlinx.coroutines.delay

val GreenPrimary = Color(0xFFB7FF00)
val GreenSecondary = Color(0xFFA5E800)
val DarkText = Color.White
val LightGray = com.example.woocom.ui.theme.CardSurface
val FavoriteRed = Color(0xFFE91E63)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailsPage(modifier: Modifier = Modifier, productId: String) {
    var product by remember { mutableStateOf<ProductModel?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
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

    com.example.woocom.components.PremiumBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Text("Product Details", color = DarkText) },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        navigationIconContentColor = Color.White,
                        actionIconContentColor = Color.White
                    )
                )
            }
    ) { paddingValues ->
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

                product == null -> Text(
                    text = "Product not found",
                    modifier = Modifier.align(Alignment.Center),
                    color = DarkText
                )

                else -> ProductContent(product = product!!, modifier = modifier)
            }
    }
}
}
}

@Composable
fun ProductContent(product: ProductModel, modifier: Modifier = Modifier) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        if (product.images.isNotEmpty()) {
            ImageCarouselWithFavorite(images = product.images, productId = product.id)
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = product.title,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = DarkText
        )

        Spacer(modifier = Modifier.height(12.dp))

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

        InfoCard(title = "Overview", content = product.otherDetails.map { "${it.key}: ${it.value}" })

        Spacer(modifier = Modifier.height(16.dp))

        InfoCard(title = "Description", content = listOf(product.description.ifEmpty { "No description available" }))

        Spacer(modifier = Modifier.height(24.dp))

        GreenButton(
            text = "Add to Cart",
            onClick = { AppUtil.addToCart(productId = product.id, context = context) },
            isLoading = false,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun InfoCard(title: String, content: List<String>) {
    NeonGlassCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = DarkText
            )
            Spacer(modifier = Modifier.height(8.dp))
            content.forEach {
                Text(text = it, fontSize = 16.sp, color = DarkText, modifier = Modifier.padding(vertical = 4.dp))
            }
        }
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
            .padding(horizontal = 16.dp)
            .height(56.dp),
        colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
        enabled = !isLoading
    ) {
        Text(
            text = if (isLoading) "Loading..." else text,
            style = TextStyle(fontSize = 20.sp, color = Color.Black)
        )
    }
}

@Composable
fun ImageCarouselWithFavorite(images: List<String>, productId: String) {
    val pagerState = rememberPagerState(pageCount = { images.size })
    val context = LocalContext.current
    var isFavorite by remember { mutableStateOf(false) }

    // Load initial favorite state from Firestore
    LaunchedEffect(productId) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return@LaunchedEffect
        Firebase.firestore.collection("user")
            .document(uid)
            .get()
            .addOnSuccessListener { doc ->
                val favs = doc.get("favorites") as? Map<*, *>
                isFavorite = favs?.containsKey(productId) == true
            }
    }

    LaunchedEffect(Unit) {
        while (true) {
            delay(4000)
            if (images.isNotEmpty()) {
                val nextPage = (pagerState.currentPage + 1) % images.size
                pagerState.animateScrollToPage(nextPage)
            }
        }
    }

    Box(modifier = Modifier.fillMaxWidth()) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            HorizontalPager(
                state = pagerState,
                pageSpacing = 16.dp,
                modifier = Modifier.height(450.dp)
            ) { page ->
                GlassCard(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(4.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    AsyncImage(
                        model = images[page],
                        contentDescription = "Product image ${page + 1}",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            DotsIndicator(
                dotCount = images.size,
                type = ShiftIndicatorType(
                    dotsGraphic = DotGraphic(color = Color.DarkGray)
                ),
                pagerState = pagerState
            )
        }

        IconButton(
            onClick = {
                val uid = FirebaseAuth.getInstance().currentUser?.uid
                if (uid != null) {
                    val userDoc = Firebase.firestore.collection("user").document(uid)
                    if (isFavorite) {
                        // Remove from favorites
                        userDoc.update("favorites.$productId", FieldValue.delete())
                        AppUtil.showToast(context, "Removed from favorites")
                    } else {
                        // Add to favorites
                        userDoc.update("favorites.$productId", true)
                        AppUtil.showToast(context, "Added to favorites")
                    }
                    isFavorite = !isFavorite
                }
            },
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
