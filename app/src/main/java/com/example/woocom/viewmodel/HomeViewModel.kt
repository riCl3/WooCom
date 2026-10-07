package com.example.woocom.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.woocom.data.ProductRepository
import com.example.woocom.data.Resource
import com.example.woocom.data.ServiceLocator
import com.example.woocom.data.UserRepository
import com.example.woocom.data.resourceOf
import com.example.woocom.model.CategoryModel
import com.example.woocom.model.ProductModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** Everything the home screen needs, fetched once per screen lifetime. */
data class HomeCatalogue(
    val banners: List<String> = emptyList(),
    val categories: List<CategoryModel> = emptyList(),
    val products: List<ProductModel> = emptyList(),
    val userName: String = ""
) {
    val deals: List<ProductModel> get() = products.take(DEAL_LIMIT)
    val featured: List<ProductModel> get() = products.take(FEATURED_LIMIT)
    val recentlyViewed: List<ProductModel> get() = products.take(RECENT_LIMIT)
    val recommended: List<ProductModel> get() = products.take(RECOMMENDED_LIMIT)

    private companion object {
        const val DEAL_LIMIT = 10
        const val FEATURED_LIMIT = 10
        const val RECENT_LIMIT = 8
        const val RECOMMENDED_LIMIT = 10
    }
}

/**
 * Single source of truth for the home tab.
 *
 * Scoped to the home back-stack entry, so it survives bottom-tab switches: previously
 * every tab change threw away all `remember`ed state and re-ran four near-identical
 * product queries plus categories, banners and the profile read.
 */
class HomeViewModel(
    private val catalogue: ProductRepository = ServiceLocator.productRepository,
    private val users: UserRepository = ServiceLocator.userRepository
) : ViewModel() {

    private val _state = MutableStateFlow<Resource<HomeCatalogue>>(Resource.Loading)
    val state: StateFlow<Resource<HomeCatalogue>> = _state.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _state.value = Resource.Loading
            _state.value = resourceOf {
                val existing = (_state.value as? Resource.Success)?.data
                // One product query feeds all four carousels instead of four queries.
                val products = catalogue.products(PRODUCT_FETCH_LIMIT)
                HomeCatalogue(
                    banners = catalogue.banners(),
                    categories = catalogue.categories(),
                    products = products,
                    userName = existing?.userName ?: users.currentUser()?.name.orEmpty()
                )
            }
        }
    }

    private companion object {
        const val PRODUCT_FETCH_LIMIT = 20
    }
}
