package com.example.woocom.pages

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.woocom.AppUtil
import com.example.woocom.components.EmptyState
import com.example.woocom.components.ErrorState
import com.example.woocom.components.LoadingState
import com.example.woocom.components.PremiumBackground
import com.example.woocom.data.ServiceLocator
import com.example.woocom.data.resourceOf
import com.example.woocom.model.ProductModel
import com.example.woocom.ui.theme.CardSurface
import com.example.woocom.ui.theme.GreenPrimary
import com.example.woocom.ui.theme.NeonBorder
import com.example.woocom.ui.theme.PrimaryText
import com.example.woocom.ui.theme.SecondaryText
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritePage() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var isLoading by remember { mutableStateOf(true) }
    var loadError by remember { mutableStateOf<String?>(null) }
    var favoriteProducts by remember { mutableStateOf<List<ProductModel>>(emptyList()) }
    var attempt by remember { mutableStateOf(0) }

    LaunchedEffect(attempt) {
        isLoading = true
        loadError = null
        val result = resourceOf {
            val user = ServiceLocator.userRepository.currentUser()
                ?: throw IllegalStateException("Please sign in to see your favourites.")
            val favoriteIds = user.favorites.filterValues { it }.keys.toList()
            if (favoriteIds.isEmpty()) {
                emptyList()
            } else {
                ServiceLocator.productRepository.productsByIds(favoriteIds)
            }
        }
        favoriteProducts = result.dataOrNull.orEmpty()
        loadError = result.errorMessageOrNull
        isLoading = false
    }

    fun remove(product: ProductModel) {
        val remaining = favoriteProducts.filterNot { it.id == product.id }
        favoriteProducts = remaining
        scope.launch {
            val result = resourceOf {
                ServiceLocator.userRepository.setFavorite(product.id, favorite = false)
            }
            if (result.errorMessageOrNull != null) {
                favoriteProducts = favoriteProducts + product
                AppUtil.showToast(context, "Could not remove ${product.title}")
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Favorites", color = PrimaryText) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = Color.Transparent
    ) { padding ->
        PremiumBackground {
            Box(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
            ) {
                when {
                    isLoading -> LoadingState(modifier = Modifier.align(Alignment.Center))

                    loadError != null -> ErrorState(
                        message = loadError!!,
                        onRetry = { attempt++ },
                        modifier = Modifier.align(Alignment.Center)
                    )

                    favoriteProducts.isEmpty() -> EmptyState(
                        icon = Icons.Default.FavoriteBorder,
                        title = "No favourites yet",
                        subtitle = "Tap the heart on a product to add it here",
                        modifier = Modifier.align(Alignment.Center)
                    )

                    else -> LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(favoriteProducts, key = { it.id }) { product ->
                            FavoriteItemCard(
                                product = product,
                                onRemove = { remove(product) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FavoriteItemCard(
    product: ProductModel,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, NeonBorder),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(84.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(GreenPrimary.copy(alpha = 0.08f))
            ) {
                AsyncImage(
                    model = product.images.firstOrNull(),
                    contentDescription = product.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = product.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = PrimaryText,
                    maxLines = 2
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "₹${product.price}",
                    fontSize = 14.sp,
                    color = GreenPrimary
                )
            }

            IconButton(
                onClick = onRemove,
                modifier = Modifier
                    .size(40.dp)
                    .background(Color.Red.copy(alpha = 0.08f), RoundedCornerShape(10.dp))
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Remove from favourites",
                    tint = Color(0xFFFF6B6B)
                )
            }
        }
    }
}