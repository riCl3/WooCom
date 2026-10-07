package com.example.woocom.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.woocom.components.EmptyState
import com.example.woocom.components.ErrorState
import com.example.woocom.components.LoadingState
import com.example.woocom.components.PremiumBackground
import com.example.woocom.components.ProductItemView
import com.example.woocom.data.ServiceLocator
import com.example.woocom.data.resourceOf
import com.example.woocom.model.ProductModel
import com.example.woocom.ui.theme.PrimaryText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryProductsPage(
    navController: NavHostController,
    categoryId: String,
) {
    var productList by remember { mutableStateOf(listOf<ProductModel>()) }
    var isLoading by remember { mutableStateOf(true) }
    var loadError by remember { mutableStateOf<String?>(null) }
    var attempt by remember { mutableStateOf(0) }

    LaunchedEffect(categoryId, attempt) {
        isLoading = true
        loadError = null
        val result =
            resourceOf {
                ServiceLocator.productRepository.productsInCategory(categoryId)
            }
        productList = result.dataOrNull.orEmpty()
        loadError = result.errorMessageOrNull
        isLoading = false
    }

    PremiumBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Text("Products", color = PrimaryText) },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = PrimaryText,
                            )
                        }
                    },
                    colors =
                        TopAppBarDefaults.topAppBarColors(
                            containerColor = Color.Transparent,
                            titleContentColor = PrimaryText,
                            navigationIconContentColor = PrimaryText,
                        ),
                )
            },
        ) { paddingValues ->
            Box(
                modifier =
                    Modifier
                        .padding(paddingValues)
                        .fillMaxSize(),
            ) {
                when {
                    isLoading -> LoadingState(modifier = Modifier.align(Alignment.Center))

                    loadError != null ->
                        ErrorState(
                            message = loadError!!,
                            onRetry = { attempt++ },
                            modifier = Modifier.align(Alignment.Center),
                        )

                    productList.isEmpty() ->
                        EmptyState(
                            title = "No products in this category yet",
                            subtitle = "Check back soon for new arrivals",
                            modifier = Modifier.align(Alignment.Center),
                        )

                    else ->
                        LazyColumn(
                            modifier =
                                Modifier
                                    .fillMaxSize()
                                    .padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            items(
                                items = productList.chunked(2),
                                key = { chunk -> chunk.first().id },
                            ) { rowItems ->
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    modifier = Modifier.fillMaxWidth(),
                                ) {
                                    rowItems.forEach { product ->
                                        ProductItemView(
                                            product = product,
                                            modifier =
                                                Modifier
                                                    .weight(1f)
                                                    .height(260.dp),
                                            navController = navController,
                                        )
                                    }
                                    if (rowItems.size == 1) {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                }
            }
        }
    }
}
