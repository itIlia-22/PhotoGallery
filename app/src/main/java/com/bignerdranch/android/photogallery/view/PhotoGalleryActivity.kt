package com.bignerdranch.android.photogallery.view

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bignerdranch.android.photogallery.PhotoGalleryFragment
import com.bignerdranch.android.photogallery.R

class PhotoGalleryActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo_gallery)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .add(R.id.fragmentContainer, PhotoGalleryFragment.newInstance()).addToBackStack("")
                .commit()
        }
    }

    companion object {
        fun newIntent(context: Context): Intent {
            return Intent(
                context,
                PhotoGalleryActivity::class.java
            )
        }
    }

}