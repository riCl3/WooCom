package com.example.woocom.components

import androidx.compose.material3.TextFieldDefaults
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HeaderView(modifier: Modifier = Modifier) {
    var name by remember { mutableStateOf("") }
    var searcher by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        FirebaseFirestore.getInstance().collection("user")
            .document(FirebaseAuth.getInstance().currentUser?.uid ?: "")
            .get().addOnCompleteListener {
                name = it.result?.get("name").toString()
            }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp, start = 4.dp, end = 4.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = "Welcome,",
                style = TextStyle(
                    fontSize = 30.sp,
                    fontFamily = FontFamily.Cursive,
                    textAlign = TextAlign.Start,
                    fontWeight = FontWeight.ExtraBold
                )
            )
            Text(
                text = name,
                style = TextStyle(
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.SansSerif,
                    textAlign = TextAlign.Start
                )
            )
        }

        OutlinedTextField(
            value = searcher,
            onValueChange = { searcher = it },
            trailingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search Icon",
                    tint = Color.LightGray // Green color for the icon
                )
            },
            modifier = Modifier
                .weight(1.8f)
                .height(55.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFFFFA500), // Focused border color is green
                unfocusedBorderColor = Color.Black, // Unfocused border color is black
                cursorColor = Color(0xFFFFA500) // Cursor color is green
            ),
            placeholder = {
                Text(
                    text = "Search",
                    style = TextStyle(color = Color.Black,
                        textAlign = TextAlign.Center,
                        fontSize = 10.sp) // Placeholder text style
                )
            },
            shape = RoundedCornerShape(40.dp),
            singleLine = true
        )
    }
}
