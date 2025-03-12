package com.example.woocom.components

import androidx.compose.material3.TextFieldDefaults
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.tbuonomo.viewpagerdotsindicator.compose.DotsIndicator
import com.tbuonomo.viewpagerdotsindicator.compose.model.DotGraphic
import com.tbuonomo.viewpagerdotsindicator.compose.type.ShiftIndicatorType

@Composable
fun BannerView(modifier: Modifier = Modifier) {
    var bannerList by remember { mutableStateOf(listOf<String>()) }

    LaunchedEffect(Unit) {
        Firebase.firestore.collection("data")
            .document("banner")
            .get().addOnCompleteListener() {
                bannerList = it.result.get("urls") as List<String>
            }
    }

    Column(modifier = modifier) {
        val pagerState = rememberPagerState(0) {
            bannerList.size
        }

        // Add this LaunchedEffect for auto-scrolling every 4 seconds
        LaunchedEffect(Unit) {
            while (true) {
                kotlinx.coroutines.delay(4000) // Wait for 4 seconds
                if (bannerList.isNotEmpty()) {
                    val nextPage = (pagerState.currentPage + 1) % bannerList.size
                    pagerState.animateScrollToPage(nextPage)
                }
            }
        }

        HorizontalPager(state = pagerState, pageSpacing = 24.dp) {
            AsyncImage(
                model = bannerList.getOrNull(it),
                contentDescription = "Banner Page",
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        DotsIndicator(
            dotCount = bannerList.size,
            type = ShiftIndicatorType(dotsGraphic = DotGraphic(color = Color.LightGray)),
            pagerState = pagerState
        )
    }
}