package com.bignerdranch.android.photogallery.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.bignerdranch.android.photogallery.FlickrFetchr
import com.bignerdranch.android.photogallery.model.GalleryItem

class PhotoGalleryViewModel() : ViewModel() {


    val galleryLiveData: LiveData<List<GalleryItem>> = FlickrFetchr().fetchPhotos()



}