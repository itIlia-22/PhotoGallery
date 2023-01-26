package com.bignerdranch.android.photogallery.viewweb

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.webkit.WebView
import androidx.appcompat.app.AppCompatActivity
import com.bignerdranch.android.photogallery.R


class PhotoPageActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo_page)
        if (savedInstanceState == null) {
            intent.data?.let { PhotoPageFragment.newInstance(it) }?.let {
                supportFragmentManager.beginTransaction()
                    .add(
                        R.id.fragment_container,
                        it
                    ).addToBackStack("")
                    .commit()
            }
        }

    }



    companion object {
        fun newIntent(context: Context, photoPageUri: Uri): Intent {
            return Intent(context, PhotoPageActivity::class.java).apply {
                data = photoPageUri
            }
        }
    }
}