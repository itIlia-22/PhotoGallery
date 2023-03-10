package com.bignerdranch.android.photogallery.api

import com.bignerdranch.android.photogallery.model.GalleryItem
import com.google.gson.annotations.SerializedName

class PhotoResponse {
    @SerializedName("photo")
    lateinit var galleryItem: List<GalleryItem>
}