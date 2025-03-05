package com.example.threadsclone.viewModel

import android.app.Application
import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.threadsclone.cloudinary.CloudinaryHelper
import com.example.threadsclone.model.UserModel
import com.example.threadsclone.utils.SharedPref
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase

class AuthViewModel : ViewModel() {

    val auth = FirebaseAuth.getInstance()
    private val db = FirebaseDatabase.getInstance()
    val userRef = db.getReference("users")

    // LiveData for Firebase User and errors
    private val _firebaseUser = MutableLiveData<FirebaseUser>()
    val firebaseUser: LiveData<FirebaseUser> = _firebaseUser

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    init {
        _firebaseUser.value = auth.currentUser
    }

    // Login function with email format validation
    fun login(email: String, password: String) {
        val trimmedEmail = email.trim() // Trim any leading/trailing spaces
        Log.d("EmailCheck", "Email being validated for login: $trimmedEmail") // Log the email

        if (!isValidEmail(trimmedEmail)) {
            _error.postValue("Invalid email format.")
            return
        }

        auth.signInWithEmailAndPassword(trimmedEmail, password).addOnCompleteListener {
            if (it.isSuccessful) {
                _firebaseUser.postValue(auth.currentUser)
            } else {
                _error.postValue("Something went wrong.")
                Log.e("FirebaseAuth", "Login Error: ${it.exception?.message}")
            }
        }
    }

    // Register function with email format validation and image upload
    fun register(
        email: String,
        password: String,
        name: String,
        bio: String,
        userName: String,
        imageUri: Uri,
        context: Context
    ) {
        val trimmedEmail = email.trim() // Trim any leading/trailing spaces
        Log.d("EmailCheck", "Email being validated for registration: $trimmedEmail") // Log the email

        if (!isValidEmail(trimmedEmail)) {
            _error.postValue("Invalid email format.")
            Toast.makeText(context, "Invalid email format", Toast.LENGTH_LONG).show()
            return
        }

        // Validate password length
        if (password.length < 6) {
            _error.postValue("Password must be at least 6 characters long.")
            Toast.makeText(context, "Password must be at least 6 characters", Toast.LENGTH_LONG).show()
            return
        }

        // Check if the email is already in use
        auth.fetchSignInMethodsForEmail(trimmedEmail).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val signInMethods = task.result?.signInMethods
                if (signInMethods != null && signInMethods.isNotEmpty()) {
                    _error.postValue("Email is already in use.")
                    Toast.makeText(context, "Email is already in use", Toast.LENGTH_LONG).show()
                    return@addOnCompleteListener
                }
            } else {
                _error.postValue("Error checking email existence: ${task.exception?.message}")
                Log.e("FirebaseAuth", "Error checking email existence: ${task.exception?.message}")
                return@addOnCompleteListener
            }

            // Proceed to create the user
            auth.createUserWithEmailAndPassword(trimmedEmail, password).addOnCompleteListener { it ->
                if (it.isSuccessful) {
                    _firebaseUser.postValue(auth.currentUser)

                    // Use CloudinaryHelper to upload image
                    CloudinaryHelper.uploadImage(
                        name = userName,
                        filePath = imageUri.toString(), // Convert URI to file path
                        onSuccess = { imageUrl ->
                            // Once image is uploaded, save user details to Firebase
                            saveUserDetailsToFirebase(
                                trimmedEmail,
                                password,
                                name,
                                bio,
                                userName,
                                imageUrl,
                                auth.currentUser?.uid,
                                context
                            )
                        }
                    )
                } else {
                    _error.postValue("Firebase error: ${it.exception?.message}")
                    Toast.makeText(
                        context,
                        "Registration failed: ${it.exception?.message}",
                        Toast.LENGTH_LONG
                    ).show()
                    it.exception?.let { exception ->
                        Log.e("FirebaseAuth", "Error: ${exception.message}")
                    }
                }
            }
        }
    }

    // Helper function to validate email format
    private fun isValidEmail(email: String): Boolean {
        val trimmedEmail = email.trim()
        val emailPattern = android.util.Patterns.EMAIL_ADDRESS
        val valid = emailPattern.matcher(trimmedEmail).matches()

        if (!valid) {
            Log.d("EmailValidation", "Invalid email format: $trimmedEmail") // Log invalid email
        }

        return valid
    }

    // Function to save user details in Firebase Realtime Database
    private fun saveUserDetailsToFirebase(
        email: String, password: String, name: String, bio: String, userName: String,
        imageUrl: String, uid: String?, context: Context
    ) {
        val userData = UserModel(
            email, password, name, bio, userName, imageUrl
        )

        // Save user data to Firebase Realtime Database
        uid?.let {
            userRef.child(uid).setValue(userData).addOnSuccessListener {
                SharedPref.storeData(name, email, bio, userName, imageUrl, context)
            }.addOnFailureListener {
                _error.postValue("Failed to save user data")
                Log.e("FirebaseAuth", "Error saving user data: ${it.message}")
            }
        }
    }
}
