package com.example.woocom.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.woocom.AppUtil
import com.example.woocom.data.ProductRepository
import com.example.woocom.data.Resource
import com.example.woocom.data.ServiceLocator
import com.example.woocom.data.UserRepository
import com.example.woocom.data.resourceOf
import com.example.woocom.model.ProductModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** One cart row: the resolved product plus its quantity. */
data class CartLine(
    val productId: String,
    val quantity: Long,
    val product: ProductModel?,
)

data class CartState(
    val lines: List<CartLine> = emptyList(),
    val subtotal: Double = 0.0,
    val isLoading: Boolean = true,
    val error: String? = null,
) {
    val isEmpty: Boolean get() = !isLoading && error == null && lines.isEmpty()
    val itemCount: Int get() = lines.sumOf { it.quantity }.toInt()
}

/**
 * Cart state that outlives composition.
 *
 * Mutations are awaited before the cart is re-read, so the UI never refreshes against a
 * write that has not landed yet (the previous implementation fired the refresh first and
 * then deleted, showing a stale cart).
 */
class CartViewModel(
    private val users: UserRepository = ServiceLocator.userRepository,
    private val catalogue: ProductRepository = ServiceLocator.productRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(CartState())
    val state: StateFlow<CartState> = _state.asStateFlow()

    private val _messages = MutableSharedFlow<String>(extraBufferCapacity = 4)
    val messages: SharedFlow<String> = _messages.asSharedFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            val result = resourceOf { loadCart() }
            result.dataOrNull?.let { lines ->
                _state.update {
                    it.copy(
                        lines = lines,
                        subtotal =
                            lines.sumOf {
                                AppUtil.lineTotal(it.product?.price.orEmpty(), it.quantity)
                            },
                        isLoading = false,
                        error = null,
                    )
                }
            } ?: run {
                val message = (result as? Resource.Error)?.message ?: "Could not load your cart"
                _state.update { it.copy(isLoading = false, error = message) }
            }
        }
    }

    fun changeQuantity(
        productId: String,
        quantity: Long,
    ) {
        mutate("Could not update quantity") {
            users.setCartQuantity(productId, quantity)
        }
    }

    fun remove(productId: String) {
        mutate("Could not remove item") {
            users.removeFromCart(productId)
        }
    }

    private inline fun mutate(
        failureMessage: String,
        crossinline action: suspend () -> Unit,
    ) {
        viewModelScope.launch {
            val outcome = resourceOf { action() }
            val error = outcome as? Resource.Error
            _messages.tryEmit(error?.message ?: failureMessage)
            if (error == null) refresh() else _state.update { it.copy(isLoading = false) }
        }
    }

    private suspend fun loadCart(): List<CartLine> {
        val user = users.currentUser() ?: return emptyList()
        val cartItems = user.cartItems
        if (cartItems.isEmpty()) return emptyList()

        // Single batched query instead of one query per cart row.
        val products = catalogue.productsByIds(cartItems.keys).associateBy { it.id }
        return cartItems.map { (productId, quantity) ->
            CartLine(productId, quantity, products[productId])
        }
    }
}
