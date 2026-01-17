package com.example.woocom.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.woocom.model.ProductModel
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchPage(
    modifier: Modifier = Modifier,
    navController: NavController,
    query: String
) {
    var searchResults by remember { mutableStateOf<List<ProductModel>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(query) {
        if (query.isBlank()) {
            isLoading = false
            searchResults = emptyList()
            return@LaunchedEffect
        }
        
        isLoading = true
        try {
            // Basic Firestore prefix search on title
            // Note: This is case-sensitive and limited. 
            // For better search, consider a third-party service or client-side filtering if dataset is small.
            // For this implementation, we'll fetch all products and filter client-side for better UX (case-insensitive)
            // assuming the product list isn't massive.
            
            val snapshot = Firebase.firestore.collection("data")
                .document("stock")
                .collection("products")
                .get()
                .await()
            
            val allProducts = snapshot.toObjects(ProductModel::class.java)
            searchResults = allProducts.filter { 
                it.title.contains(query, ignoreCase = true) || 
                it.category.contains(query, ignoreCase = true) 
            }
        } catch (e: Exception) {
            searchResults = emptyList()
        } finally {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Search Results: $query") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        com.example.woocom.components.PremiumBackground {
            Box(
                modifier = modifier
                    .padding(padding)
                    .fillMaxSize()
            ) {
            when {
                isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                searchResults.isEmpty() -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No items found for \"$query\"",
                            fontSize = 18.sp,
                            color = Color.White
                        )
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(searchResults) { product ->
                            SearchProductItem(product = product, onClick = {
                                navController.navigate("product-details/${product.id}")
                            })
                        }
                    }
                }
            }
        }
            }
        }
}

@Composable
fun SearchProductItem(product: ProductModel, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = com.example.woocom.ui.theme.CardSurface),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, com.example.woocom.ui.theme.NeonBorder)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = product.images.firstOrNull(),
                contentDescription = product.title,
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.LightGray),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = product.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 2
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "₹${product.price}",
                    fontSize = 14.sp,
                    color = com.example.woocom.ui.theme.GreenPrimary
                )
            }
        }
    }
}
