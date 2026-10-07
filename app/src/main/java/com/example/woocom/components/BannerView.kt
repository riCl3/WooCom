package com.example.woocom.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.tbuonomo.viewpagerdotsindicator.compose.DotsIndicator
import com.tbuonomo.viewpagerdotsindicator.compose.model.DotGraphic
import com.tbuonomo.viewpagerdotsindicator.compose.type.ShiftIndicatorType
import kotlinx.coroutines.delay

@Composable
fun BannerView(modifier: Modifier = Modifier) {
    var bannerList by remember { mutableStateOf(listOf<String>()) }

    LaunchedEffect(Unit) {
        Firebase.firestore.collection("data")
            .document("banner")
            .get()
            .addOnSuccessListener { result ->
                bannerList = result.get("urls") as? List<String> ?: emptyList()
            }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        val pagerState = rememberPagerState(pageCount = { bannerList.size })

        LaunchedEffect(bannerList.size) {
            if (bannerList.size > 1) {
                while (true) {
                    delay(4000)
                    val nextPage = (pagerState.currentPage + 1) % bannerList.size
                    pagerState.animateScrollToPage(nextPage)
                }
            }
        }

        HorizontalPager(
            state = pagerState,
            pageSpacing = 16.dp,
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
        ) { index ->
            AsyncImage(
                model = bannerList.getOrNull(index),
                contentDescription = "Promotional banner ${index + 1}",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (bannerList.size > 1) {
            DotsIndicator(
                dotCount = bannerList.size,
                type = ShiftIndicatorType(
                    dotsGraphic = DotGraphic(color = androidx.compose.ui.graphics.Color.LightGray)
                ),
                pagerState = pagerState
            )
        }
    }
}