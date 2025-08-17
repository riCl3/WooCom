package com.example.woocom.pages

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.woocom.components.BannerView
import com.example.woocom.components.CategoriesView
import com.example.woocom.components.HeaderView
import com.example.woocom.components.FeaturedProductsView
import com.example.woocom.components.DealsOfTheDayView
import com.example.woocom.components.RecentlyViewedView
import com.example.woocom.components.RecommendedView

@Composable
fun HomePage(modifier: Modifier = Modifier) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        HeaderView(Modifier)

        BannerView(modifier)

        Text(
            text = "Categories",
            style = TextStyle(
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        CategoriesView()

        Spacer(modifier = Modifier.height(24.dp))

        // Deals of the Day Section
        Text(
            text = "🔥 Deals of the Day",
            style = TextStyle(
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        DealsOfTheDayView()

        Spacer(modifier = Modifier.height(24.dp))

        // Featured Products Section
        Text(
            text = "⭐ Featured Products",
            style = TextStyle(
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        FeaturedProductsView()

        Spacer(modifier = Modifier.height(24.dp))

        // Recently Viewed Section
        Text(
            text = "👀 Recently Viewed",
            style = TextStyle(
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        RecentlyViewedView()

        Spacer(modifier = Modifier.height(24.dp))

        // Recommended Section
        Text(
            text = "💡 Recommended for You",
            style = TextStyle(
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        RecommendedView()

        Spacer(modifier = Modifier.height(16.dp))
    }
}