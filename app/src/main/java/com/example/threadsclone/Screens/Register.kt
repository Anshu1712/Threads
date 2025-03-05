package com.example.threadsclone.Screens

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.threadsclone.R
import com.example.threadsclone.navigation.Routes
import com.example.threadsclone.viewModel.AuthViewModel
import coil3.compose.rememberAsyncImagePainter

@Composable
fun Register(navHostController: NavHostController) {

    // Email, name, bio, userName, password state variables
    val email = remember { mutableStateOf("") }
    val name = remember { mutableStateOf("") }
    val bio = remember { mutableStateOf("") }
    val userName = remember { mutableStateOf("") }
    val password = remember { mutableStateOf("") }

    var imageUri by remember { mutableStateOf<Uri?>(null) }

    val authViewModel: AuthViewModel = viewModel()
    val firebaseUser by authViewModel.firebaseUser.observeAsState(null)

    // Permissions for accessing the device storage
    val permissionToRequest = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_IMAGES
    } else Manifest.permission.READ_EXTERNAL_STORAGE

    val context = LocalContext.current

    // Image picker launcher
    val launcher =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri: Uri? ->
            imageUri = uri
        }

    // Permission request launcher
    val permissionRequestLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                launcher.launch("image/*")
            } else {
                Toast.makeText(context, "Permission denied to access storage", Toast.LENGTH_SHORT).show()
            }
        }

    // Function to validate email format
    fun isEmailValid(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    // UI Layout
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        // Register text header
        Text(
            text = "Register Here", style = TextStyle(
                fontWeight = FontWeight.ExtraBold,
                fontSize = 24.sp
            )
        )

        Box(modifier = Modifier.height(25.dp))

        // Profile image upload button
        Image(
            painter = if (imageUri == null) painterResource(id = R.drawable.man)
            else rememberAsyncImagePainter(model = imageUri),
            contentDescription = "person",
            modifier = Modifier
                .size(96.dp)
                .clip(CircleShape)
                .background(Color.LightGray)
                .clickable {
                    // Check if the permission is granted
                    val isGranted = ContextCompat.checkSelfPermission(
                        context, permissionToRequest
                    ) == PackageManager.PERMISSION_GRANTED

                    if (isGranted) {
                        // Launch the image picker if permission is granted
                        launcher.launch("image/*")
                    } else {
                        // Request permission if not granted
                        permissionRequestLauncher.launch(permissionToRequest)
                    }
                },
            contentScale = ContentScale.Crop
        )

        Box(modifier = Modifier.height(25.dp))

        // Name, UserName, Bio, Email, and Password fields
        OutlinedTextField(
            value = name.value,
            onValueChange = { name.value = it },
            label = { Text(text = "Name") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = userName.value,
            onValueChange = { userName.value = it },
            label = { Text(text = "Username") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = bio.value,
            onValueChange = { bio.value = it },
            label = { Text(text = "Bio") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        // Email field
        OutlinedTextField(
            value = email.value,
            onValueChange = { email.value = it },
            label = { Text(text = "Email") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        // Password field
        OutlinedTextField(
            value = password.value,
            onValueChange = { password.value = it },
            label = { Text(text = "Password") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Box(modifier = Modifier.height(30.dp))

        // Register button
        ElevatedButton(onClick = {
            try {
                // Validate user input before attempting registration
                if (name.value.isEmpty() || email.value.isEmpty() || bio.value.isEmpty() || password.value.isEmpty() || imageUri == null) {
                    Toast.makeText(context, "Please fill all details", Toast.LENGTH_SHORT).show()
                } else if (!isEmailValid(email.value)) {
                    Toast.makeText(context, "Invalid email format", Toast.LENGTH_SHORT).show()
                } else {
                    // Pass email and other fields to the register function after trimming spaces
                    authViewModel.register(
                        email.value.trim(),
                        password.value.trim(),
                        name.value.trim(),
                        bio.value.trim(),
                        userName.value.trim(),
                        imageUri!!,
                        context
                    )
                }
            } catch (e: Exception) {
                // Log the exception and show an error message
                Log.e("Register", "Registration failed", e)
                Toast.makeText(context, "Registration failed. Please try again.", Toast.LENGTH_SHORT).show()
            }
        }, modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Register", style = TextStyle(
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                ), modifier = Modifier.padding(vertical = 6.dp)
            )
        }

        // Navigate to login page if already registered
        TextButton(onClick = {
            navHostController.navigate(Routes.Login.routes) {
                popUpTo(navHostController.graph.startDestinationId)
                launchSingleTop = true
            }
        }, modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Already registered? Login here", style = TextStyle(
                    fontSize = 16.sp
                )
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RegisterView() {
    // Register()
}
