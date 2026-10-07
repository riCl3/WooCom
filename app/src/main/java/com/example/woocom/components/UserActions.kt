package com.example.woocom.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import com.example.woocom.AppUtil
import com.example.woocom.data.Resource
import com.example.woocom.data.ServiceLocator
import com.example.woocom.data.resourceOf
import kotlinx.coroutines.launch

/**
 * Fire-and-report "add to cart" handler bound to the calling composition's lifecycle.
 *
 * The coroutine is cancelled if the screen goes away, so a toast can no longer be shown
 * onto a destroyed Activity, and failures surface to the user instead of being dropped.
 */
@Composable
fun rememberAddToCart(): (String) -> Unit {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    return { productId ->
        scope.launch {
            val outcome = resourceOf { ServiceLocator.userRepository.addToCart(productId) }
            AppUtil.showToast(
                context,
                if (outcome is Resource.Error) {
                    "Could not add item to cart"
                } else {
                    "Item added to cart"
                },
            )
        }
    }
}
