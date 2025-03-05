package com.example.threadsclone.Screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.example.threadsclone.navigation.Routes

@Composable
fun Login(navController: NavHostController) {
    val email = remember { mutableStateOf("") }  // Fixed the mutableStateOf initialization
    val password = remember { mutableStateOf("") }  // Fixed the mutableStateOf initialization

    Column (modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center){

        Text(text =  "Login", style = TextStyle(
            fontWeight = FontWeight.ExtraBold,
            fontSize = 24.sp
        ))

        Box(modifier = Modifier.height(50.dp))
        OutlinedTextField(
            value = email.value, // Use email.value to access the current state
            onValueChange = { email.value = it }, // Update the state with email.value
            label = {
                Text(text = "Email")
            }, keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email
            ),
            singleLine = true,
            modifier =  Modifier.fillMaxWidth()
        )

        Box(modifier = Modifier.height(20.dp))
        OutlinedTextField(
            value = password.value, // Use email.value to access the current state
            onValueChange = { password.value = it }, // Update the state with email.value
            label = {
                Text(text = "Password")
            }, keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password
            ),
            singleLine = true,
            modifier =  Modifier.fillMaxWidth()
        )
        Box(modifier = Modifier.height(30.dp))
        ElevatedButton(onClick = {


        }, modifier = Modifier.fillMaxWidth()) {
            Text(text = "Login",style = TextStyle(
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp), modifier = Modifier.padding(vertical = 6.dp))
        }
        TextButton (onClick = {

            navController.navigate(Routes.Register.routes){
                popUpTo(navController.graph.startDestinationId)
                launchSingleTop = true
            }

        }, modifier = Modifier.fillMaxWidth()) {
            Text(text = "New User? Create Account",style = TextStyle(
                fontSize = 16.sp))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginView() {
  //  Login()
}
