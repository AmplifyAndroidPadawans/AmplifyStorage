package com.example.amplifystorage

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.otaliastudios.cameraview.CameraListener
import com.otaliastudios.cameraview.CameraView
import com.otaliastudios.cameraview.FileCallback
import com.otaliastudios.cameraview.PictureResult
import com.otaliastudios.cameraview.controls.Facing
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date


class MainActivity : AppCompatActivity(), FileCallback {
    private lateinit var btnCamara: Button
    private lateinit var btnShowImage: Button
    private lateinit var btnDeleteImage: Button
    private lateinit var camera: CameraView
    private lateinit var downloadedImage: ImageView

    private val permissions = listOf(
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.CAMERA
    )

    private val storage by lazy {
        StorageHelper()
    }

    companion object {
        private const val MULTIPLE_PERMISSIONS = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        checkPermissions()

        downloadedImage = findViewById(R.id.downloaded_image)

        camera = findViewById(R.id.camera)
        camera.setLifecycleOwner(this)
        camera.facing = Facing.BACK

        camera.addCameraListener(object : CameraListener(){
            override fun onPictureTaken(result: PictureResult) {
                result.toFile(File(getExternalFilesDir("Provisional"), "takenPicture.jpg"), this@MainActivity)
            }
        })

        btnCamara = findViewById(R.id.btnCamara)
        btnCamara.setOnClickListener {
            takePicture()
        }

        btnShowImage = findViewById(R.id.btn_show_image)
        btnShowImage.setOnClickListener {
            val dateFormatted = SimpleDateFormat("dd-MM-yyyy").format(Date())
            storage.retrieveImage(
                key = "image-$dateFormatted",
                level = AccessLevel.PUBLIC,
                completed = ::handleShowImage,
            )
        }

        btnDeleteImage = findViewById(R.id.btn_delete_image)
        btnDeleteImage.setOnClickListener {
            val dateFormatted = SimpleDateFormat("dd-MM-yyyy").format(Date())
            storage.deleteImage(
                key = "image-$dateFormatted",
                level = AccessLevel.PUBLIC,
            )
        }
    }

    private fun handleShowImage(bitmap: Bitmap) {
        downloadedImage.setImageBitmap(bitmap)
    }

    private fun checkPermissions(): Boolean {
        var result: Int
        val listPermissionsNeeded: MutableList<String> = ArrayList()
        for (p in permissions) {
            result = ContextCompat.checkSelfPermission(applicationContext, p)
            if (result != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(p)
            }
        }
        if (listPermissionsNeeded.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                listPermissionsNeeded.toTypedArray(),
                MULTIPLE_PERMISSIONS
            )
            return false
        }
        return true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            MULTIPLE_PERMISSIONS -> {
                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    var perStr = ""
                    for (per in permissions) {
                        perStr += """
                        $per\n
                        """.trimIndent()
                    }
                    Toast.makeText(this, "$perStr\nNot granted", Toast.LENGTH_SHORT).show()
                }
                return
            }
        }
    }

    private fun takePicture() {
        camera.takePicture()
    }

    override fun onFileReady(file: File?) {
        uploadImage(file)
    }

    private fun uploadImage(file: File?) {

        val dateFormatted = SimpleDateFormat("dd-MM-yyyy").format(Date())

        storage.storeImage(
            file = file!!,
            key = "image-$dateFormatted",
            level = AccessLevel.PUBLIC
        )
    }

    override fun onResume() {
        super.onResume()
        camera.open()
    }

    override fun onPause() {
        super.onPause()
        camera.close()
    }

    override fun onDestroy() {
        super.onDestroy()
        camera.destroy()
    }
}