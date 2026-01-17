package com.example.woocom.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction
import com.example.woocom.GlobalNavigation

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
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid != null) {
            FirebaseFirestore.getInstance().collection("user")
                .document(uid)
                .get().addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        name = task.result?.get("name").toString()
                    }
                }
        }
    }

    Row(
        modifier = modifier
            .fillMaxWidth() // Top padding handled by HomePage statusBarsPadding
            .padding(top = 8.dp), 
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
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White // Dark Mode
                )
            )
            Text(
                text = name,
                style = TextStyle(
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.SansSerif,
                    textAlign = TextAlign.Start,
                    color = Color.White // Dark Mode
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
                    tint = Color(0xFFB7FF00), // Green color for the icon
                    modifier = Modifier.clickable {
                        if (searcher.isNotBlank()) {
                            GlobalNavigation.navController.navigate("search/$searcher")
                        }
                    }
                )
            },
            modifier = Modifier
                .weight(1.8f)
                .height(55.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFFB7FF00), // Focused border color is green
                unfocusedBorderColor = Color.LightGray, 
                cursorColor = Color(0xFFB7FF00), // Cursor color is green
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            placeholder = {
                Text(
                    text = "Search",
                    style = TextStyle(color = Color.LightGray,
                        textAlign = TextAlign.Center,
                        fontSize = 10.sp) // Placeholder text style
                )
            },
            shape = RoundedCornerShape(40.dp),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(
                onSearch = {
                     if (searcher.isNotBlank()) {
                        GlobalNavigation.navController.navigate("search/$searcher")
                    }
                }
            )
        )
    }
}
