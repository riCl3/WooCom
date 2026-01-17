package com.example.woocom.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.ui.graphics.Color
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
    com.example.woocom.components.PremiumBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding() // Handled by system insets
                .verticalScroll(rememberScrollState())
        ) {
        HeaderView(Modifier.padding(horizontal = 16.dp))

        BannerView(modifier)

        Spacer(modifier = Modifier.height(8.dp)) // Small gap between banner and categories

        Text(
            text = "Categories",
            style = TextStyle(
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Color.White
            ),
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // CategoriesView handles its own padding or we wrap it
        CategoriesView(modifier = Modifier.padding(horizontal = 16.dp))

        Spacer(modifier = Modifier.height(24.dp))

        // Deals of the Day Section
        Text(
            text = "🔥 Deals of the Day",
            style = TextStyle(
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Color.White
            ),
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        DealsOfTheDayView(modifier = Modifier.padding(horizontal = 16.dp))

        Spacer(modifier = Modifier.height(24.dp))

        // Featured Products Section
        Text(
            text = "⭐ Featured Products",
            style = TextStyle(
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Color.White
            ),
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        FeaturedProductsView(modifier = Modifier.padding(horizontal = 16.dp))

        Spacer(modifier = Modifier.height(24.dp))

        // Recently Viewed Section
        Text(
            text = "👀 Recently Viewed",
            style = TextStyle(
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Color.White
            ),
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        RecentlyViewedView(modifier = Modifier.padding(horizontal = 16.dp))

        Spacer(modifier = Modifier.height(24.dp))

        // Recommended Section
        Text(
            text = "💡 Recommended for You",
            style = TextStyle(
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Color.White
            ),
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        RecommendedView(modifier = Modifier.padding(horizontal = 16.dp))

        Spacer(modifier = Modifier.height(16.dp))
    }
}
}