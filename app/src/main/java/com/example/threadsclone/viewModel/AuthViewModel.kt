package com.example.threadsclone.viewModel

import android.annotation.SuppressLint
import android.net.Uri
import android.content.Context // Import Android's Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.threadsclone.model.UserModel
import com.example.threadsclone.utils.SharedPref
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import java.util.UUID

class AuthViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseDatabase.getInstance()
    private val userRef = db.getReference("users")

    private val storageRef = Firebase.storage.reference
    private val imageRef = storageRef.child("users/${UUID.randomUUID()}.jpg")
    private val _firebaseUser = MutableLiveData<FirebaseUser>()
    val firebaseUser: LiveData<FirebaseUser> = _firebaseUser

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    init {
        _firebaseUser.value = auth.currentUser
    }

    fun login(email: String, password: String, context: Context) {
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener {
            if (it.isSuccessful) {
                _firebaseUser.postValue(auth.currentUser)

                getData(auth.currentUser!!.uid, context)
            } else {
                _error.postValue(it.exception!!.message)
            }
        }
    }

    private fun getData(uid: String, context: Context) {

        userRef.child(uid).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val userData = snapshot.getValue(UserModel::class.java)
                SharedPref.storeData(
                    userData!!.name, userData!!.email, userData!!.bio,
                    userData!!.userName, userData!!.imageUrl, context
                )

            }

            override fun onCancelled(error: DatabaseError) {

            }

        })

    }

    fun register(
        email: String,
        password: String,
        name: String,
        bio: String,
        userName: String,
        imageUri: Uri,
        context: Context // Use Android's Context here
    ) {
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener {
            if (it.isSuccessful) {
                _firebaseUser.postValue(auth.currentUser)
                saveImage(
                    email,
                    password,
                    name,
                    userName,
                    bio,
                    imageUri,
                    auth.currentUser?.uid,
                    context
                )
            } else {
                _error.postValue("Something went wrong")
            }
        }
    }

    private fun saveImage(
        email: String,
        password: String,
        name: String,
        userName: String,
        bio: String,
        imageUri: Uri,
        uid: String?,
        context: Context // Use Android's Context here
    ) {
        val uploadTask = imageRef.putFile(imageUri)
        uploadTask.addOnSuccessListener {
            imageRef.downloadUrl.addOnSuccessListener {
                saveData(email, bio, password, userName, name, it.toString(), uid, context)
            }
        }
    }

    private fun saveData(
        email: String,
        bio: String,
        password: String,
        userName: String,
        name: String,
        imageUrl: String,
        uid: String?,
        context: Context // Use Android's Context here
    ) {
        val userData = UserModel(email, password, name, bio, userName, imageUrl, uid!!)
        userRef.child(uid!!).setValue(userData).addOnSuccessListener {
            SharedPref.storeData(name, email, bio, userName, imageUrl, context)
        }.addOnFailureListener {
            _error.postValue("Failed to save user data")
        }
    }

    @SuppressLint("NullSafeMutableLiveData")
    fun Logout() {
        auth.signOut()
        _firebaseUser.postValue(null)
    }
}
