package com.example.woocom.pages

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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.woocom.AppUtil
import com.example.woocom.components.GlassCard
import com.example.woocom.components.NeonGlassCard
import com.example.woocom.components.PremiumBackground
import com.example.woocom.model.ProductModel
import com.example.woocom.ui.theme.FavoriteRed
import com.example.woocom.ui.theme.GreenPrimary
import com.example.woocom.ui.theme.PrimaryText
import com.example.woocom.ui.theme.SecondaryText
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.tbuonomo.viewpagerdotsindicator.compose.DotsIndicator
import com.tbuonomo.viewpagerdotsindicator.compose.model.DotGraphic
import com.tbuonomo.viewpagerdotsindicator.compose.type.ShiftIndicatorType
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailsPage(navController: NavHostController, productId: String) {
    var product by remember { mutableStateOf<ProductModel?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(productId) {
        Firebase.firestore
            .collection("data")
            .document("stock")
            .collection("products")
            .whereEqualTo("id", productId)
            .get().addOnCompleteListener { task ->
                isLoading = false
                if (task.isSuccessful) {
                    val result = task.result.toObjects(ProductModel::class.java)
                    if (result.isNotEmpty()) product = result.first()
                }
            }
    }

    PremiumBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Text("Product Details", color = PrimaryText) },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = PrimaryText
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        navigationIconContentColor = PrimaryText
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
                        color = SecondaryText
                    )

                    else -> ProductContent(product = product!!)
                }
            }
        }
    }
}

@Composable
private fun ProductContent(product: ProductModel) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    Column(
        modifier = Modifier
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
            color = PrimaryText
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "₹${product.price}",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = GreenPrimary
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = "₹${product.actualPrice}",
                fontSize = 16.sp,
                color = SecondaryText,
                style = TextStyle(textDecoration = TextDecoration.LineThrough)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        if (product.otherDetails.isNotEmpty()) {
            InfoCard(
                title = "Overview",
                content = product.otherDetails.map { "${it.key}: ${it.value}" }
            )

            Spacer(modifier = Modifier.height(16.dp))
        }

        InfoCard(
            title = "Description",
            content = listOf(product.description.ifBlank { "No description available" })
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { AppUtil.addToCart(productId = product.id, context = context) },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = GreenPrimary,
                contentColor = com.example.woocom.ui.theme.DarkText
            ),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text(
                text = "Add to Cart",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun InfoCard(title: String, content: List<String>) {
    NeonGlassCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryText
            )
            Spacer(modifier = Modifier.height(8.dp))
            content.forEach { item ->
                Text(
                    text = item,
                    fontSize = 16.sp,
                    color = SecondaryText,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun ImageCarouselWithFavorite(images: List<String>, productId: String) {
    val pagerState = rememberPagerState(pageCount = { images.size })
    val context = LocalContext.current
    var isFavorite by remember { mutableStateOf(false) }

    LaunchedEffect(productId) {
        AppUtil.isFavorite(productId) { isFavorite = it }
    }

    LaunchedEffect(Unit) {
        if (images.size > 1) {
            while (true) {
                delay(4000)
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
                modifier = Modifier
                    .fillMaxWidth()
                    .height(380.dp)
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
                    dotsGraphic = DotGraphic(color = Color.Gray)
                ),
                pagerState = pagerState
            )
        }

        IconButton(
            onClick = {
                if (isFavorite) {
                    AppUtil.removeFromFavorites(productId, context)
                } else {
                    AppUtil.addToFavorites(productId, context)
                }
                isFavorite = !isFavorite
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 24.dp, bottom = 70.dp)
                .size(48.dp)
                .background(PrimaryText.copy(alpha = 0.9f), CircleShape)
                .zIndex(10f)
        ) {
            Icon(
                imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                contentDescription = "Toggle favourite",
                tint = if (isFavorite) FavoriteRed else Color.Gray,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}