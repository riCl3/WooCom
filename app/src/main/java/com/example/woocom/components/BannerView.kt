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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.tbuonomo.viewpagerdotsindicator.compose.DotsIndicator
import com.tbuonomo.viewpagerdotsindicator.compose.model.DotGraphic
import com.tbuonomo.viewpagerdotsindicator.compose.type.ShiftIndicatorType
import kotlinx.coroutines.delay

@Composable
fun BannerView(
    modifier: Modifier = Modifier,
    banners: List<String> = emptyList(),
) {
    if (banners.isEmpty()) return

    Column(modifier = modifier.fillMaxWidth()) {
        val pagerState = rememberPagerState(pageCount = { banners.size })

        LaunchedEffect(banners.size) {
            if (banners.size > 1) {
                while (true) {
                    delay(4000)
                    val nextPage = (pagerState.currentPage + 1) % banners.size
                    pagerState.animateScrollToPage(nextPage)
                }
            }
        }

        HorizontalPager(
            state = pagerState,
            pageSpacing = 16.dp,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(150.dp),
        ) { index ->
            AsyncImage(
                model = banners.getOrNull(index),
                contentDescription = "Promotional banner ${index + 1}",
                contentScale = ContentScale.Crop,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp)),
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (banners.size > 1) {
            DotsIndicator(
                dotCount = banners.size,
                type =
                    ShiftIndicatorType(
                        dotsGraphic = DotGraphic(color = androidx.compose.ui.graphics.Color.LightGray),
                    ),
                pagerState = pagerState,
            )
        }
    }
}
