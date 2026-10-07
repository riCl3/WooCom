package com.example.woocom.pages

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.woocom.components.BannerView
import com.example.woocom.components.CategoriesView
import com.example.woocom.components.DealsOfTheDayView
import com.example.woocom.components.ErrorState
import com.example.woocom.components.FeaturedProductsView
import com.example.woocom.components.HeaderView
import com.example.woocom.components.LoadingState
import com.example.woocom.components.PremiumBackground
import com.example.woocom.components.RecentlyViewedView
import com.example.woocom.components.RecommendedView
import com.example.woocom.data.Resource
import com.example.woocom.viewmodel.HomeCatalogue
import com.example.woocom.viewmodel.HomeViewModel

/**
 * Home tab.
 *
 * All catalogue data comes from [HomeViewModel], which is scoped to the home back-stack
 * entry: switching bottom tabs no longer discards state or re-runs every query.
 */
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun HomePage(
    navController: NavHostController,
    viewModel: HomeViewModel = viewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val pullRefreshState =
        rememberPullRefreshState(
            refreshing = state.isLoading && state.dataOrNull != null,
            onRefresh = viewModel::refresh,
        )

    PremiumBackground {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .pullRefresh(pullRefreshState)
                    .verticalScroll(rememberScrollState()),
        ) {
            when (val current = state) {
                Resource.Loading -> LoadingState(modifier = Modifier.padding(top = 120.dp))

                is Resource.Error ->
                    ErrorState(
                        message = current.message,
                        onRetry = viewModel::refresh,
                        modifier = Modifier.padding(top = 120.dp),
                    )

                is Resource.Success ->
                    HomeContent(
                        catalogue = current.data,
                        navController = navController,
                    )
            }
        }

        PullRefreshIndicator(
            refreshing = state.isLoading && state.dataOrNull != null,
            state = pullRefreshState,
            modifier = Modifier.align(Alignment.TopCenter),
        )
    }
}

@Composable
private fun HomeContent(
    catalogue: HomeCatalogue,
    navController: NavHostController,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        HeaderView(
            modifier = Modifier.padding(horizontal = 16.dp),
            navController = navController,
            userName = catalogue.userName,
        )

        Spacer(modifier = Modifier.height(16.dp))

        BannerView(
            modifier = Modifier.padding(horizontal = 16.dp),
            banners = catalogue.banners,
        )

        Spacer(modifier = Modifier.height(24.dp))

        SectionTitle("Categories")
        Spacer(modifier = Modifier.height(12.dp))
        CategoriesView(
            modifier = Modifier.padding(horizontal = 16.dp),
            navController = navController,
            categories = catalogue.categories,
        )

        Spacer(modifier = Modifier.height(28.dp))

        SectionTitle("Deals of the Day")
        Spacer(modifier = Modifier.height(12.dp))
        DealsOfTheDayView(
            modifier = Modifier.padding(horizontal = 16.dp),
            navController = navController,
            products = catalogue.deals,
        )

        Spacer(modifier = Modifier.height(28.dp))

        SectionTitle("Featured Products")
        Spacer(modifier = Modifier.height(12.dp))
        FeaturedProductsView(
            modifier = Modifier.padding(horizontal = 16.dp),
            navController = navController,
            products = catalogue.featured,
        )

        Spacer(modifier = Modifier.height(28.dp))

        SectionTitle("Recently Viewed")
        Spacer(modifier = Modifier.height(12.dp))
        RecentlyViewedView(
            modifier = Modifier.padding(horizontal = 16.dp),
            navController = navController,
            products = catalogue.recentlyViewed,
        )

        Spacer(modifier = Modifier.height(28.dp))

        SectionTitle("Recommended for You")
        Spacer(modifier = Modifier.height(12.dp))
        RecommendedView(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
            navController = navController,
            products = catalogue.recommended,
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier.padding(horizontal = 16.dp),
    )
}
