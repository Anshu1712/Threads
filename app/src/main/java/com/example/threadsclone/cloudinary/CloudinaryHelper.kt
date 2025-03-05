package com.example.threadsclone.cloudinary

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.util.Log
import android.widget.ImageView
import com.cloudinary.Cloudinary
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.example.threadsclone.R
import com.squareup.picasso.Picasso
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object CloudinaryHelper {
    val config: MutableMap<String, Any> = mutableMapOf()
    lateinit var publicId: String
    var started: Boolean = false
    val CLOUDINARY_URL =
        "CLOUDINARY_URL=cloudinary://231582314684168:oWlUs0rm2Os1SsGMyrlWcLBfFh8@dmridh454"
    val cloudinary = Cloudinary(CLOUDINARY_URL)

    fun initializeConfig(context: Context) {
        config["cloud_name"] = "dmridh454"
        config["api_key"] = "231582314684168"
        config["api_secret"] = "oWlUs0rm2Os1SsGMyrlWcLBfFh8"
        MediaManager.init(context, config)
        started = true
    }

    // Function to upload image to Cloudinary
    fun uploadImage(name: String, filePath: String, onSuccess: (String) -> Unit) {
        deleteImageAsync(name)

        MediaManager.get().upload(filePath)
            .option("resource_type", "image")
            .option("public_id", name)
            .callback(object : UploadCallback {
                override fun onStart(requestId: String?) {
                    println("Upload Started")
                }

                override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {
                    val progress = (bytes * 100 / totalBytes).toInt()
                    println("Upload progress: $progress%")
                }

                override fun onSuccess(requestId: String?, resultData: MutableMap<Any?, Any?>?) {
                    val imageUrl = resultData?.get("url").toString()
                    publicId = resultData?.get("public_id").toString()

                    println("Image uploaded successfully. URL: $imageUrl")
                    onSuccess(imageUrl)
                }

                override fun onError(requestId: String?, error: ErrorInfo?) {
                    println("Error uploading image: ${error?.description}")
                }

                override fun onReschedule(requestId: String?, error: ErrorInfo?) {
                    TODO("Not yet implemented")
                }
            })
            .dispatch()
    }

    // Function to delete image from Cloudinary if exists
    private fun deleteImageAsync(publicId: String) {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val result = cloudinary.uploader().destroy(publicId, mapOf("invalidate" to true))
                withContext(Dispatchers.Main) {
                    Log.d("Cloudinary", "Delete result: $result")
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e("Cloudinary", "Error: ${e.message}")
                }
            }
        }
    }

    // Function to load image from Cloudinary into an ImageView
    fun fetchThatImage(name: String, img: ImageView) {
        val baseUrl = MediaManager.get().url().generate(name)
        val cacheBustedUrl = "$baseUrl?nocache=${System.currentTimeMillis()}"

        Picasso.get()
            .load(cacheBustedUrl)
            .placeholder(R.drawable.progress_animation)
            .error(R.drawable.user)
            .into(img)
    }

    // Helper function to get real path from URI
    fun getRealPathFromURI(uri: Uri?, context: Context): String? {
        // Handle different document URIs for API levels above KITKAT
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && DocumentsContract.isDocumentUri(context, uri)) {
            // Handle different document URIs
            when {
                isExternalStorageDocument(uri) -> {
                    val docId = DocumentsContract.getDocumentId(uri)
                    val split = docId.split(":")
                    val type = split[0]
                    if ("primary" == type) {
                        return "${android.os.Environment.getExternalStorageDirectory()}/${split[1]}"
                    }
                }
                isDownloadsDocument(uri) -> {
                    val id = DocumentsContract.getDocumentId(uri)
                    val contentUri = Uri.parse("content://downloads/public_downloads")
                    val uriContent = ContentUris.withAppendedId(contentUri, java.lang.Long.valueOf(id))
                    return getDataColumn(context, uriContent, null, null)
                }
                isMediaDocument(uri) -> {
                    val docId = DocumentsContract.getDocumentId(uri)
                    val split = docId.split(":")
                    val type = split[0]
                    val contentUri = when (type) {
                        "image" -> MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                        "video" -> MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                        "audio" -> MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                        else -> null
                    }
                    val selection = "_id=?"
                    val selectionArgs = arrayOf(split[1])
                    return contentUri?.let { getDataColumn(context, it, selection, selectionArgs) }
                }
            }
        } else if ("content".equals(uri?.scheme, ignoreCase = true)) {
            return getDataColumn(context, uri, null, null)
        } else if ("file".equals(uri?.scheme, ignoreCase = true)) {
            return uri?.path
        }
        return null
    }

    private fun getDataColumn(context: Context, uri: Uri?, selection: String?, selectionArgs: Array<String>?): String? {
        var cursor: Cursor? = null
        val column = "_data"
        val projection = arrayOf(column)

        try {
            cursor = uri?.let { context.contentResolver.query(it, projection, selection, selectionArgs, null) }
            if (cursor != null && cursor.moveToFirst()) {
                val columnIndex = cursor.getColumnIndexOrThrow(column)
                return cursor.getString(columnIndex)
            }
        } finally {
            cursor?.close()
        }
        return null
    }

    private fun isExternalStorageDocument(uri: Uri?): Boolean {
        return "com.android.externalstorage.documents" == uri?.authority
    }

    private fun isDownloadsDocument(uri: Uri?): Boolean {
        return "com.android.providers.downloads.documents" == uri?.authority
    }

    private fun isMediaDocument(uri: Uri?): Boolean {
        return "com.android.providers.media.documents" == uri?.authority
    }
}
