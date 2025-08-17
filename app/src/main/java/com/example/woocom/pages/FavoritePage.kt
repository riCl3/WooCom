package com.example.woocom.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.woocom.model.ProductModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritePage(modifier: Modifier = Modifier) {
    val auth = FirebaseAuth.getInstance()
    val db = Firebase.firestore

    var isLoading by remember { mutableStateOf(true) }
    var userDocId by remember { mutableStateOf<String?>(null) }
    var favoriteProducts by remember { mutableStateOf<List<ProductModel>>(emptyList()) }

    LaunchedEffect(Unit) {
        try {
            val uid = auth.currentUser?.uid ?: return@LaunchedEffect

            // IMPORTANT: Your user docs have auto-generated IDs; query by userId field
            val userQuery = db.collection("user")
                .whereEqualTo("userId", uid)
                .get()
                .await()

            if (!userQuery.isEmpty) {
                val userDoc = userQuery.documents.first()
                userDocId = userDoc.id

                val favoritesMap = userDoc.get("favorites") as? Map<*, *> ?: emptyMap<String, Boolean>()
                val favoriteIds = favoritesMap.keys.filterIsInstance<String>()

                if (favoriteIds.isNotEmpty()) {
                    // Firestore whereIn supports max 10 values; chunk if needed
                    val chunks = favoriteIds.chunked(10)
                    val collected = mutableListOf<ProductModel>()
                    for (chunk in chunks) {
                        val snap = db.collection("data")
                            .document("stock")
                            .collection("products")
                            .whereIn("id", chunk)
                            .get()
                            .await()
                        collected += snap.toObjects(ProductModel::class.java)
                    }
                    favoriteProducts = collected
                } else {
                    favoriteProducts = emptyList()
                }
            } else {
                favoriteProducts = emptyList()
            }
        } catch (e: Exception) {
            // You can surface a snackbar/toast if desired
            favoriteProducts = emptyList()
        } finally {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Favorites") },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->
        Box(
            modifier = modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            when {
                isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                favoriteProducts.isEmpty() -> EmptyFavoritesView(modifier = Modifier.align(Alignment.Center))
                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(favoriteProducts, key = { it.id }) { product ->
                            FavoriteItemCard(
                                product = product,
                                onRemove = {
                                    val id = userDocId ?: return@FavoriteItemCard
                                    // remove from favorites
                                    Firebase.firestore.collection("user")
                                        .document(id)
                                        .update("favorites.${product.id}", FieldValue.delete())
                                    // update local list
                                    favoriteProducts = favoriteProducts.filter { it.id != product.id }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyFavoritesView(modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = "No favorite items yet", fontSize = 18.sp, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(8.dp))
        Text(text = "Tap the heart on a product to add it here", fontSize = 14.sp, color = Color.Gray)
    }
}

@Composable
private fun FavoriteItemCard(
    product: ProductModel,
    onRemove: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
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
                    .background(Color(0xFFF0F0F0))
            ) {
                AsyncImage(
                    model = product.images.firstOrNull(),
                    contentDescription = product.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(product.title, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(4.dp))
                Text("₹${product.price}", fontSize = 14.sp, color = Color.Black)
            }

            IconButton(
                onClick = onRemove,
                modifier = Modifier
                    .size(40.dp)
                    .background(Color.Red.copy(alpha = 0.06f), RoundedCornerShape(10.dp))
            ) {
                Icon(Icons.Default.Delete, contentDescription = "Remove", tint = Color(0xFF9E9E9E))
            }
        }
    }
}
