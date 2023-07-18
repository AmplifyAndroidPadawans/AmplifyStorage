package com.example.amplifystorage

import android.app.Application
import android.util.Log
import com.amplifyframework.AmplifyException
import com.amplifyframework.auth.cognito.AWSCognitoAuthPlugin
import com.amplifyframework.core.Amplify
import com.amplifyframework.storage.s3.AWSS3StoragePlugin

class AmplifyStorageApplication: Application() {

    override fun onCreate() {
        super.onCreate()

        try {
            Amplify.addPlugin(AWSCognitoAuthPlugin())
            Amplify.addPlugin(AWSS3StoragePlugin())

            Amplify.configure(applicationContext)

            Log.i("AmplifyStorage", "Initialized Amplify")
        } catch (e: AmplifyException) {
            Log.e("AmplifyStorage", "Could not initialize Amplify", e)
        }
    }
}