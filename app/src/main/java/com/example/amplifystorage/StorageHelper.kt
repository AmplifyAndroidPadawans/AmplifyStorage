package com.example.amplifystorage

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import com.amplifyframework.core.Amplify
import com.amplifyframework.storage.StorageAccessLevel
import com.amplifyframework.storage.options.StorageDownloadFileOptions
import com.amplifyframework.storage.options.StoragePagedListOptions
import com.amplifyframework.storage.options.StorageRemoveOptions
import com.amplifyframework.storage.options.StorageUploadFileOptions
import java.io.File
import java.io.FileInputStream

const val TAG = "StorageHelper"

enum class AccessLevel {
    PRIVATE,
    PROTECTED,
    PUBLIC
}

class StorageHelper {

    fun storeImage(file: File, key: String, level: AccessLevel = AccessLevel.PRIVATE) {

        val options = StorageUploadFileOptions.builder()
            .accessLevel(
                when (level) {
                    AccessLevel.PRIVATE -> StorageAccessLevel.PRIVATE
                    AccessLevel.PROTECTED -> StorageAccessLevel.PROTECTED
                    AccessLevel.PUBLIC -> StorageAccessLevel.PUBLIC
                }
            )
            .build()

        Amplify.Storage.uploadFile(
            key,
            file,
            options,
            { progress -> Log.i(TAG, "Fraction completed: ${progress.fractionCompleted}") },
            { result -> Log.i(TAG, "Successfully uploaded: " + result.key) },
            { error -> Log.e(TAG, "Upload failed", error) }
        )
    }

    fun deleteImage(key : String, level: AccessLevel = AccessLevel.PRIVATE) {

        val options = StorageRemoveOptions.builder()
            .accessLevel(
                when (level) {
                    AccessLevel.PRIVATE -> StorageAccessLevel.PRIVATE
                    AccessLevel.PROTECTED -> StorageAccessLevel.PROTECTED
                    AccessLevel.PUBLIC -> StorageAccessLevel.PUBLIC
                }
            )
            .build()

        Amplify.Storage.remove(
            key,
            options,
            { result -> Log.i(TAG, "Successfully removed: " + result.key) },
            { error -> Log.e(TAG, "Remove failure", error) }
        )
    }

    fun retrieveImage(key: String, level: AccessLevel = AccessLevel.PRIVATE, completed : (image: Bitmap) -> Unit) {
        val options = StorageDownloadFileOptions.builder()
            .accessLevel(
                when (level) {
                    AccessLevel.PRIVATE -> StorageAccessLevel.PRIVATE
                    AccessLevel.PROTECTED -> StorageAccessLevel.PROTECTED
                    AccessLevel.PUBLIC -> StorageAccessLevel.PUBLIC
                }
            )
            .build()

        val file = File.createTempFile("image", ".image")

        Amplify.Storage.downloadFile(
            key,
            file,
            options,
            { progress -> Log.i(TAG, "Fraction completed: ${progress.fractionCompleted}") },
            { result ->
                Log.i(TAG, "Successfully downloaded: ${result.file.name}")
                val imageStream = FileInputStream(file)
                val image = BitmapFactory.decodeStream(imageStream)
                completed(image)
            },
            { error -> Log.e(TAG, "Download Failure", error) }
        )
    }

    fun listNames() {

        val options = StoragePagedListOptions.builder()
            .setPageSize(1000)
            .build()

        Amplify.Storage.list("", options,
            { result ->
                result.items.forEach { item ->
                    Log.i(TAG, "Item: ${item.key}")
                }
                Log.i(TAG, "Next Token: ${result.nextToken}")
            },
            { Log.e(TAG, "List failure", it) }
        )
    }
}