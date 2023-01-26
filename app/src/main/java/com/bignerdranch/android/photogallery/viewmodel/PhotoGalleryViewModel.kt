package com.bignerdranch.android.photogallery.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.bignerdranch.android.photogallery.FlickrFetchr
import com.bignerdranch.android.photogallery.QueryPreferences
import com.bignerdranch.android.photogallery.model.GalleryItem

/**
 * Вашему ViewModel нужен контекст для использования
функций QueryPreferences. Изменение родительского класса
PhotoGalleryViewModel с ViewModel на AndroidViewModel
предоставляет PhotoGalleryViewModel доступ к контексту
приложения. Хранить ссылку на контекст приложения в
PhotoGalleryViewModel безопасно, потому что контекст
приложения переживает PhotoGalleryViewModel.
 */
class PhotoGalleryViewModel(private val app: Application) : AndroidViewModel(app) {


    val galleryLiveData: LiveData<List<GalleryItem>>

    private val flickrFetchr = FlickrFetchr()
    private val mutableSearchTerm = MutableLiveData<String>()

    val searchTerm: String
        get() = mutableSearchTerm.value ?: ""

    init {
        mutableSearchTerm.value = QueryPreferences.getStoredQuery(app)
        galleryLiveData = Transformations.switchMap(mutableSearchTerm) { searchTerm ->
            if (searchTerm.isBlank()) {
                flickrFetchr.fetchPhotos()
            } else {

                flickrFetchr.searchPhotos(searchTerm)
            }
        }


    }

    fun fetchPhotos(query: String = "") {
        QueryPreferences.setStoredQuery(app, query)
        mutableSearchTerm.value = query
    }

}