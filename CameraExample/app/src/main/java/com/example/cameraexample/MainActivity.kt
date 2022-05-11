package com.example.cameraexample

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import java.io.File

class MainActivity : AppCompatActivity() {
    private lateinit var imageView : ImageView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        imageView = findViewById(R.id.imageView)
        val button : Button = findViewById(R.id.button)
        val tempImageUri = initTempUri()

        val resultLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) {
            imageView.setImageURI(null)
            imageView.setImageURI(tempImageUri)
        }

        button.setOnClickListener {
            resultLauncher.launch(tempImageUri)
        }

        var mGetContent = registerForActivityResult(ActivityResultContracts.GetContent()) {
            result ->
            if (result != null) {
                imageView.setImageURI(result)
            }
        }

        val buttonGallery: Button = findViewById(R.id.buttonGallery)
        buttonGallery.setOnClickListener { v: View? ->
            mGetContent.launch(
                "image/*"
            )
        }
    }

    private fun initTempUri(): Uri {
        val tempImagesDir = File(
            applicationContext.filesDir,
            getString(R.string.temp_images_dir)
        )

        tempImagesDir.mkdir()

        val tempImage = File(
            tempImagesDir,
            getString(R.string.temp_image)
        )

        return FileProvider.getUriForFile(
            applicationContext,
            getString(R.string.authorities),
            tempImage
        )
    }
}