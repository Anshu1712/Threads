package com.example.threadsclone.utils

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences

object SharedPref {

    // Function to store data in SharedPreferences
    fun storeData(
        name: String,
        email: String,
        bio: String,
        userName: String,
        imageUrl: String,
        context: Context
    ) {
        // Get SharedPreferences instance
        val sharedPreferences: SharedPreferences =
            context.getSharedPreferences("users", MODE_PRIVATE)

        // Get the SharedPreferences editor to write data
        val editor = sharedPreferences.edit()

        // Store data in SharedPreferences
        editor.putString("name", name)
        editor.putString("email", email)
        editor.putString("bio", bio)
        editor.putString("userName", userName)
        editor.putString("imageUrl", imageUrl)

        // Commit the changes
        editor.apply()  // apply() asynchronously saves the data
    }

    // Function to retrieve data from SharedPreferences
    fun getUserName(context: Context): String {
        val sharedPreferences: SharedPreferences =
            context.getSharedPreferences("users", MODE_PRIVATE)

        // Return the data as a map
        return sharedPreferences.getString("userName", "")!!
    }

    fun getEmail(context: Context): String {
        val sharedPreferences: SharedPreferences =
            context.getSharedPreferences("users", MODE_PRIVATE)

        // Return the data as a map
        return sharedPreferences.getString("email", "")!!
    }

    fun getBio(context: Context): String {
        val sharedPreferences: SharedPreferences =
            context.getSharedPreferences("users", MODE_PRIVATE)

        // Return the data as a map
        return sharedPreferences.getString("bio", "")!!
    }

    fun getName(context: Context): String {
        val sharedPreferences: SharedPreferences =
            context.getSharedPreferences("users", MODE_PRIVATE)

        // Return the data as a map
        return sharedPreferences.getString("name", "")!!
    }

    fun getImageUrl(context: Context): String {
        val sharedPreferences: SharedPreferences =
            context.getSharedPreferences("users", MODE_PRIVATE)

        // Return the data as a map
        return sharedPreferences.getString("imageUrl", "")!!
    }
}
