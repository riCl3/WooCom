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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.woocom.GlobalNavigation
import com.example.woocom.model.CategoryModel
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObject
import com.example.woocom.ui.theme.CardSurface
import com.example.woocom.ui.theme.NeonBorder
import androidx.compose.foundation.BorderStroke
import com.example.woocom.components.GlassCard
import com.example.woocom.components.NeonGlassCard

@Composable
fun CategoriesView(modifier: Modifier=Modifier) {

    var categoryList by remember { mutableStateOf(listOf<CategoryModel>()) }

    LaunchedEffect(Unit) {
        Firebase.firestore.collection("data")
            .document("stock")
            .collection("categoties")
            .get().addOnCompleteListener(){
                if(it.isSuccessful){
                    val resultList = it.result.documents.mapNotNull { doc ->
                        doc.toObject(CategoryModel::class.java)
                    }
                    categoryList = resultList
                }
            }
    }
    LazyRow(modifier = modifier) {
            items(categoryList){ item ->
                CategoryItem(category = item)
            }
        }
}

@Composable
fun CategoryItem(category: CategoryModel) {
    NeonGlassCard(
        modifier = Modifier
            .size(110.dp)
            .padding(4.dp)
            .clickable {
                GlobalNavigation.navController.navigate("category-products/"+category.id)
            },
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(12.dp)
        ) {
            // Image
            AsyncImage(
                model = category.imageUrl,
                contentDescription = null,
                modifier = Modifier.size(50.dp)
            )

            // Spacer between image and text
            Spacer(modifier = Modifier.height(8.dp))

            // Text with specified color and size
            Text(
                text = category.Name,
                style = TextStyle(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White // Title White
                ),
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
        }
    }
}