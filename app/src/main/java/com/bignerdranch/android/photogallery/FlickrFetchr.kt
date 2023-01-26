package com.bignerdranch.android.photogallery

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.bignerdranch.android.photogallery.api.FlickrApi
import com.bignerdranch.android.photogallery.api.FlickrResponse
import com.bignerdranch.android.photogallery.api.PhotoInterceptor
import com.bignerdranch.android.photogallery.api.PhotoResponse
import com.bignerdranch.android.photogallery.model.GalleryItem
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

private const val TAG = "FlickrFetchr"

class FlickrFetchr {


    private val flickrApi: FlickrApi

//Добавление перехватчика в конфигурацию Retrofit
    /**
     * Мы создаем экземпляр OkHttpClient и добавляем
    PhotoInterceptor в качестве перехватчика. Затем мы
    устанавливаем только что настроенный клиент на экземпляр
    Retrofit, который заменяет клиент по умполчанию. Теперь
    Retrofit будет использовать предоставленный вами клиент и,
    в свою очередь, применять функцию
    PhotoInterceptor.intercept(...) к любым запросам.
     */
    init {
        val client = OkHttpClient.Builder()
            .addInterceptor(PhotoInterceptor())
            .build()
        val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl("https://api.flickr.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
        flickrApi = retrofit.create(FlickrApi::class.java)


    }

    fun fetchPhotosRequest(): Call<FlickrResponse> {
        return flickrApi.fetchPhotos()

    }

    fun searchPhotosRequest(query: String): Call<FlickrResponse> {
        return flickrApi.searchPhotos(query)

    }

    fun fetchPhotos(): LiveData<List<GalleryItem>> {
        return fetchPhotoMetadata(fetchPhotosRequest())
    }

    fun searchPhotos(query: String): LiveData<List<GalleryItem>> {
        return fetchPhotoMetadata(searchPhotosRequest(query))
    }

    private fun fetchPhotoMetadata(flickrRequest: Call<FlickrResponse>): LiveData<List<GalleryItem>> {
        val responseLiveData: MutableLiveData<List<GalleryItem>> = MutableLiveData()
        flickrRequest.enqueue(object : Callback<FlickrResponse> {
            override fun onResponse(
                call: Call<FlickrResponse>,
                response: Response<FlickrResponse>
            ) {
                Log.d(TAG, "Response received")
                val flickrResponse: FlickrResponse? = response.body()
                val photoResponse: PhotoResponse? = flickrResponse?.photos
                var galleryItem: List<GalleryItem> = photoResponse?.galleryItem ?: mutableListOf()
                galleryItem = galleryItem.filterNot {
                    it.url.isBlank()
                }
                responseLiveData.value = galleryItem
            }

            override fun onFailure(call: Call<FlickrResponse>, t: Throwable) {
                Log.e(TAG, "Failed to fetch photos", t)

            }

        })

        return responseLiveData
    }


    @WorkerThread
    fun fetchPhoto(url: String): Bitmap? {
        val response: Response<ResponseBody> = flickrApi.fetchUrlBytes(url).execute()
        val bitmap = response.body()?.byteStream()?.use(BitmapFactory::decodeStream)
        Log.i(TAG, "Decoded bitmap=$bitmap from Response=$response")
        return bitmap
    }


}