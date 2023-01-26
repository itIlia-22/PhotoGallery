package com.bignerdranch.android.photogallery.api

import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response

private const val API_KEY = "4d89841f458c7b0247d21b1156e8fe9f"

class PhotoInterceptor() : Interceptor {
    /**
     * Chain.request() для
    доступа к исходному запросу. Функция
    originalRequest.url() извлекает исходный URL из запроса,
    а затем используется HttpUrl.Builder для добавления
    параметров запроса.
    HttpUrl.Builder создает новый запрос на основе
    оригинального запроса и заменяет исходный URL на новый.
    Наконец, мы вызываем функцию
    chain.continue(newRequest) для создания ответа.
     */
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest: Request = chain.request()
        val newUrl: HttpUrl = originalRequest.url().newBuilder()
            .addQueryParameter("api_key", API_KEY)
            .addQueryParameter("format", "json")
            .addQueryParameter("nojsoncallback", "1")
            .addQueryParameter("extras", "url_s")
            .addQueryParameter("safesearch", "1")
            .build()
        val newRequest: Request = originalRequest.newBuilder().url(newUrl).build()
        return chain.proceed(newRequest)

    }

}