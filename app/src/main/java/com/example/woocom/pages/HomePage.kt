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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.woocom.components.BannerView
import com.example.woocom.components.CategoriesView
import com.example.woocom.components.DealsOfTheDayView
import com.example.woocom.components.FeaturedProductsView
import com.example.woocom.components.HeaderView
import com.example.woocom.components.PremiumBackground
import com.example.woocom.components.RecentlyViewedView
import com.example.woocom.components.RecommendedView

@Composable
fun HomePage(navController: NavHostController) {
    PremiumBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
        ) {
            HeaderView(
                modifier = Modifier.padding(horizontal = 16.dp),
                navController = navController
            )

            Spacer(modifier = Modifier.height(16.dp))

            BannerView(modifier = Modifier.padding(horizontal = 16.dp))

            Spacer(modifier = Modifier.height(24.dp))

            SectionTitle("Categories")
            Spacer(modifier = Modifier.height(12.dp))
            CategoriesView(
                modifier = Modifier.padding(horizontal = 16.dp),
                navController = navController
            )

            Spacer(modifier = Modifier.height(28.dp))

            SectionTitle("Deals of the Day")
            Spacer(modifier = Modifier.height(12.dp))
            DealsOfTheDayView(
                modifier = Modifier.padding(horizontal = 16.dp),
                navController = navController
            )

            Spacer(modifier = Modifier.height(28.dp))

            SectionTitle("Featured Products")
            Spacer(modifier = Modifier.height(12.dp))
            FeaturedProductsView(
                modifier = Modifier.padding(horizontal = 16.dp),
                navController = navController
            )

            Spacer(modifier = Modifier.height(28.dp))

            SectionTitle("Recently Viewed")
            Spacer(modifier = Modifier.height(12.dp))
            RecentlyViewedView(
                modifier = Modifier.padding(horizontal = 16.dp),
                navController = navController
            )

            Spacer(modifier = Modifier.height(28.dp))

            SectionTitle("Recommended for You")
            Spacer(modifier = Modifier.height(12.dp))
            RecommendedView(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                navController = navController
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier.padding(horizontal = 16.dp)
    )
}