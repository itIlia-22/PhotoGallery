package com.bignerdranch.android.photogallery.api

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Url

interface FlickrApi {
    @GET("services/rest?method=flickr.interestingness.getList")
    fun fetchPhotos(): Call<FlickrResponse>


    @GET
    fun fetchUrlBytes(@Url url: String):
            Call<ResponseBody>

    /**
     * Аннотация @Query позволяет динамически добавлять к URL
    параметры запроса. В данном случае мы добавляем параметр
    запроса text. Значение, присваиваемое параметру, зависит от
    аргумента, переданного в searchPhotos(String). Например,
    вызов searchPhotos("robot") добавит в URL приписку
    text=robot.
     */
    @GET("services/rest?method=flickr.photos.search")
    fun searchPhotos(@Query("text") query: String): Call<FlickrResponse>
}