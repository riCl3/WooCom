package com.example.woocom.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.woocom.Routes
import com.example.woocom.model.CategoryModel
import com.example.woocom.ui.theme.PrimaryText

@Composable
fun CategoriesView(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    categories: List<CategoryModel>,
) {
    if (categories.isEmpty()) return

    LazyRow(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(categories, key = { it.id }) { item ->
            CategoryItem(category = item, navController = navController)
        }
    }
}

@Composable
fun CategoryItem(
    category: CategoryModel,
    navController: NavHostController,
) {
    NeonGlassCard(
        modifier =
            Modifier
                .size(110.dp)
                .padding(4.dp)
                .clickable {
                    navController.navigate(Routes.categoryProducts(category.id)) {
                        launchSingleTop = true
                    }
                },
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(12.dp),
        ) {
            AppImage(
                model = category.imageUrl,
                contentDescription = category.Name,
                contentScale = ContentScale.Fit,
                modifier =
                    Modifier
                        .size(50.dp)
                        .clip(RoundedCornerShape(8.dp)),
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = category.Name,
                style =
                    TextStyle(
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryText,
                    ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
