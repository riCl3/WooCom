package com.example.woocom.pages

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.woocom.AppUtil
import com.example.woocom.components.EmptyState
import com.example.woocom.components.ErrorState
import com.example.woocom.components.LoadingState
import com.example.woocom.components.PremiumBackground
import com.example.woocom.data.ServiceLocator
import com.example.woocom.data.resourceOf
import com.example.woocom.model.OrderModel
import com.example.woocom.ui.theme.CardSurface
import com.example.woocom.ui.theme.GreenPrimary
import com.example.woocom.ui.theme.NeonBorder
import com.example.woocom.ui.theme.PrimaryText
import com.example.woocom.ui.theme.SecondaryText
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrdersPage(navController: NavController) {
    var orders by remember { mutableStateOf<List<OrderModel>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var loadError by remember { mutableStateOf<String?>(null) }
    var attempt by remember { mutableStateOf(0) }

    LaunchedEffect(attempt) {
        isLoading = true
        loadError = null
        val result = resourceOf { ServiceLocator.userRepository.ordersForUser() }
        orders = result.dataOrNull.orEmpty()
        loadError = result.errorMessageOrNull
        isLoading = false
    }

    PremiumBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Text("My Orders", color = PrimaryText) },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = PrimaryText,
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                )
            },
        ) { padding ->
            Box(
                modifier =
                    Modifier
                        .padding(padding)
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

                    orders.isEmpty() ->
                        EmptyState(
                            icon = Icons.AutoMirrored.Filled.ReceiptLong,
                            title = "No orders placed yet",
                            subtitle = "Your completed orders will appear here",
                            modifier = Modifier.align(Alignment.Center),
                        )

                    else ->
                        OrderList(
                            orders = orders,
                            contentPadding = padding,
                        )
                }
            }
        }
    }
}

@Composable
private fun OrderList(
    orders: List<OrderModel>,
    contentPadding: PaddingValues,
) {
    val datePattern = remember { SimpleDateFormat("d MMM yyyy, h:mm a", Locale.getDefault()) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(orders, key = { it.orderId.ifBlank { it.createdAt.toString() } }) { order ->
            OrderRow(order = order, datePattern = datePattern)
        }
    }
}

@Composable
private fun OrderRow(
    order: OrderModel,
    datePattern: SimpleDateFormat,
) {
    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        border = BorderStroke(0.5.dp, NeonBorder),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                StatusBadge(status = order.status)
                Text(
                    text = AppUtil.formatPrice(order.amount),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = GreenPrimary,
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "${order.itemCount} item${if (order.itemCount == 1) "" else "s"}",
                fontSize = 14.sp,
                color = PrimaryText,
            )

            if (order.createdAt > 0L) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = datePattern.format(Date(order.createdAt)),
                    fontSize = 13.sp,
                    color = SecondaryText,
                )
            }

            if (order.failureReason.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = order.failureReason,
                    fontSize = 12.sp,
                    color = SecondaryText,
                )
            }
        }
    }
}

@Composable
private fun StatusBadge(status: String) {
    val (label, color) =
        when (status) {
            OrderModel.STATUS_PAID -> "Paid" to GreenPrimary
            OrderModel.STATUS_FAILED -> "Failed" to androidx.compose.material3.MaterialTheme.colorScheme.error
            else -> "Pending" to SecondaryText
        }

    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier =
                Modifier
                    .size(8.dp)
                    .background(color = color, shape = RoundedCornerShape(50)),
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = label, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = color)
    }
}
