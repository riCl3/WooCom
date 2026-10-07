package com.example.woocom.pages

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.woocom.AppUtil
import com.example.woocom.BuildConfig
import com.example.woocom.components.CartItemView
import com.example.woocom.components.ErrorState
import com.example.woocom.components.LoadingState
import com.example.woocom.components.PremiumBackground
import com.example.woocom.data.CheckoutRequest
import com.example.woocom.data.PaymentSession
import com.example.woocom.data.ServiceLocator
import com.example.woocom.data.resourceOf
import com.example.woocom.ui.theme.CardSurface
import com.example.woocom.ui.theme.DarkText
import com.example.woocom.ui.theme.GreenPrimary
import com.example.woocom.ui.theme.NeonBorder
import com.example.woocom.ui.theme.PrimaryText
import com.example.woocom.ui.theme.SecondaryText
import com.example.woocom.viewmodel.CartLine
import com.razorpay.Checkout
import kotlinx.coroutines.launch
import org.json.JSONObject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutPage(navController: NavHostController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var lines by remember { mutableStateOf<List<CartLine>>(emptyList()) }
    var userName by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    var loadError by remember { mutableStateOf<String?>(null) }
    var attempt by remember { mutableStateOf(0) }

    LaunchedEffect(attempt) {
        isLoading = true
        loadError = null
        val result =
            resourceOf {
                val user =
                    ServiceLocator.userRepository.currentUser()
                        ?: throw IllegalStateException("Your account could not be loaded.")
                val products =
                    ServiceLocator.productRepository
                        .productsByIds(user.cartItems.keys)
                        .associateBy { it.id }
                userName = user.name
                user.cartItems.map { (productId, quantity) ->
                    CartLine(productId, quantity, products[productId])
                }
            }
        lines = result.dataOrNull ?: emptyList()
        loadError = result.errorMessageOrNull
        isLoading = false
    }

    PremiumBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Text("Checkout", color = PrimaryText) },
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

                    lines.isEmpty() ->
                        Text(
                            text = "Your cart is empty",
                            modifier = Modifier.align(Alignment.Center),
                            color = SecondaryText,
                        )

                    else ->
                        CheckoutContent(
                            userName = userName,
                            lines = lines,
                            onPay = {
                                val activity = context.findActivity()
                                if (activity == null) {
                                    AppUtil.showToast(context, "Payment is unavailable right now")
                                    return@CheckoutContent
                                }
                                scope.launch {
                                    beginPayment(context, lines, activity)
                                }
                            },
                        )
                }
            }
        }
    }
}

/**
 * Asks the backend to create the order in its `pending` state first, then opens Razorpay.
 *
 * The order is the single source of truth for whether the payment succeeded;
 * [com.example.woocom.MainActivity] settles it from the Razorpay callback, so a crash
 * between "opened checkout" and "callback received" leaves a recoverable `pending` row
 * instead of a paid-looking order with no payment. Which backend computes the amount is
 * decided inside [com.example.woocom.data.PaymentGateway].
 */
private suspend fun beginPayment(
    context: Context,
    lines: List<CartLine>,
    activity: Activity,
) {
    val subtotal = lines.sumOf { AppUtil.lineTotal(it.product?.price.orEmpty(), it.quantity) }
    val totalAmount = subtotal + SHIPPING_COST

    val session =
        resourceOf {
            ServiceLocator.paymentGateway.beginCheckout(
                CheckoutRequest(
                    items = lines.associate { it.productId to it.quantity },
                    amount = totalAmount,
                    itemCount = lines.sumOf { it.quantity }.toInt(),
                ),
            )
        }.dataOrNull

    if (session == null) {
        AppUtil.showToast(context, "Could not start payment, please try again.")
        return
    }

    PaymentSession.session = session
    startPayment(activity, session.amountPaise, session.razorpayOrderId)
}

@Composable
private fun CheckoutContent(
    userName: String,
    lines: List<CartLine>,
    onPay: () -> Unit,
) {
    val subtotal = lines.sumOf { AppUtil.lineTotal(it.product?.price.orEmpty(), it.quantity) }
    val totalAmount = subtotal + SHIPPING_COST

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(16.dp),
    ) {
        Text(
            text = "Hello, $userName",
            style = androidx.compose.material3.MaterialTheme.typography.titleLarge,
            color = PrimaryText,
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier =
                Modifier
                    .weight(1f)
                    .fillMaxWidth(),
        ) {
            items(lines, key = { it.productId }) { line ->
                CartItemView(
                    productId = line.productId,
                    quantity = line.quantity,
                    product = line.product,
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = CardSurface),
            border = BorderStroke(0.5.dp, NeonBorder),
            shape = RoundedCornerShape(16.dp),
        ) {
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(text = "Subtotal", fontSize = 14.sp, color = SecondaryText)
                    Text(text = "Delivery", fontSize = 14.sp, color = SecondaryText)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Amount Payable",
                        fontSize = 16.sp,
                        color = PrimaryText,
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(text = AppUtil.formatPrice(subtotal), fontSize = 14.sp, color = SecondaryText)
                    Text(
                        text = AppUtil.formatPrice(SHIPPING_COST),
                        fontSize = 14.sp,
                        color = SecondaryText,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = AppUtil.formatPrice(totalAmount),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = GreenPrimary,
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onPay,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(56.dp),
            colors =
                ButtonDefaults.buttonColors(
                    containerColor = GreenPrimary,
                    contentColor = DarkText,
                ),
            shape = RoundedCornerShape(14.dp),
        ) {
            Text(
                text = "Pay ${AppUtil.formatPrice(totalAmount)}",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

/** Launches the Razorpay checkout flow. */
private fun startPayment(
    activity: Activity,
    amountPaise: Int,
    razorpayOrderId: String?,
) {
    val checkout = Checkout()
    checkout.setKeyID(BuildConfig.RAZORPAY_KEY_ID)

    val options =
        JSONObject().apply {
            put("name", "WooCom")
            put("description", "Shopping payment")
            put("amount", amountPaise)
            put("currency", "INR")
            put("theme", JSONObject().put("color", "#A5E800"))
            // Present only when the server created the Razorpay order; Razorpay then
            // settles against that order id and hands back its signature.
            razorpayOrderId?.let { put("order_id", it) }
        }

    try {
        checkout.open(activity, options)
    } catch (e: Exception) {
        Toast.makeText(activity, "Unable to open payment", Toast.LENGTH_SHORT).show()
    }
}

internal fun Context.findActivity(): Activity? =
    when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> null
    }
