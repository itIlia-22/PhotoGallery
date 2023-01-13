package com.bignerdranch.android.photogallery.api

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Url

interface FlickrApi {
    @GET(
        "services/rest/?method=flickr.interestingness.getList&api_key=4d89841f458c7b0247d21b1156e8fe9f&format=json&nojsoncallback=1&extras=url_s "
    )
    fun fetchPhotos(): Call<FlickrResponse>

    @GET
    fun fetchUrlBytes(@Url url: String):
            Call<ResponseBody>
}