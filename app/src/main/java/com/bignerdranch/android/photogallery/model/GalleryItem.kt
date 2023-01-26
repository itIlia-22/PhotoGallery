package com.bignerdranch.android.photogallery.model

import android.net.Uri
import com.google.gson.annotations.SerializedName

data class GalleryItem(
    var title: String = "",
    var id: String = "",
    @SerializedName("url_s")
    var url: String = "",
    @SerializedName("owner")
    var owner: String = ""
) {
    val photoPageUri: Uri
        get() {
            return Uri.parse("https://www.flickr.com/photos/")
                .buildUpon()
                .appendPath(owner)
                .appendPath(id)
                .build()

        }

    /**
     * Для определения URL-адреса фотографии создается новое
    свойство owner и добавляется вычисляемое свойство
    photoPageUri для генерации URL-адресов страницы фото, как
    описано выше. Так как Gson превращает ответы JSON в
    GalleryItems, вы можете немедленно начать использовать
    свойство photoPageUri, не внося другие изменения в код.

     */
}


