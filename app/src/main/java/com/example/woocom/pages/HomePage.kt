package com.example.woocom.pages

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.woocom.components.BannerView
import com.example.woocom.components.HeaderView

@Composable
fun HomePage(modifier: Modifier=Modifier) {
    Column(modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)) {
        HeaderView(Modifier)
        BannerView(modifier)
    }
}