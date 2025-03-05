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

    // Login function
    fun login(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener {
            if (it.isSuccessful) {
                _firebaseUser.postValue(auth.currentUser)
            } else {
                _error.postValue("Something went wrong.")
            }
        }
    }

    // Register function with image upload
    fun register(
        email: String,
        password: String,
        name: String,
        bio: String,
        userName: String,
        imageUri: Uri,
        context: Context
    ) {

        // Create new user with email and password
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { it ->
            if (it.isSuccessful) {
                _firebaseUser.postValue(auth.currentUser)
                // Use CloudinaryHelper to upload image
                CloudinaryHelper.uploadImage(
                    name = userName,
                    filePath = imageUri.toString(), // Convert URI to file path
                    onSuccess = { imageUrl ->
                        // Once image is uploaded, save user details to Firebase
                        saveUserDetailsToFirebase(
                            email,
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
            }
        }
    }

    // Helper function to validate email format
    private fun isValidEmail(email: String): Boolean {
        val emailPattern = android.util.Patterns.EMAIL_ADDRESS
        return emailPattern.matcher(email).matches()
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

            }
        }
    }
}

