package com.bignerdranch.android.photogallery

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Handler
import android.os.HandlerThread
import android.os.Message
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import java.util.concurrent.ConcurrentHashMap

/**
 * Фоновый поток - Класс
 * Единственной целью ThumbnailDownloader является
загрузка и передача изображений в PhotoGalleryFragment
 */

/**
 * Реализация LifecycleObserver означает, что вы можете
подписать ThumbnailDownloader на получение обратных
вызовов жизненного цикла от любого владельца
LifecycleOwner.
 */
private const val TAG = "ThumbnailDownloader"

/**
 * Значение MESSAGE_DOWNLOAD будет использоваться для
идентификации сообщений как запросов на загрузку.
(ThumbnailDownloader присваивает его полю what
создаваемых сообщений загрузки.)

 */
private const val MESSAGE_DOWNLOAD = 0


class ThumbnailDownloader<in T : Any>(
    private val responseHandler: Handler,
    private val onThumbnailDownloaded: (T, Bitmap) -> Unit
) : HandlerThread(TAG), LifecycleObserver {
    /**
     * В переменной requestHandler будет храниться ссылка на
    объект Handler, отвечающий за постановку в очередь запросов
    на загрузку в фоновом потоке ThumbnailDownloader. Этот
    объект также будет отвечать за обработку сообщений запросов
    на загрузку при извлечении их из очереди.
     */
    private lateinit var requestHandler: Handler

    /**
     * Переменная requestMap содержит ConcurrentHashMap —
    разновидность HashMap, безопасную по отношению к потокам.
    В данном случае использование объекта-идентификатора типа
    T запроса на загрузку в качестве ключа позволяет хранить и
    загружать URL-адрес, связанный с конкретным запросом.
    (Здесь объектом-идентификатором является PhotoHolder, так
    что по ответу на запрос можно легко вернуться к элементу
    пользовательского интерфейса, в котором должно находиться
    загруженное изображение.)
     */
    private val requestMap = ConcurrentHashMap<T, String>()

    /**
     * В свойстве flickrFetchr хранится ссылка на экземпляр
    FlickrFetchr. Таким образом, весь код установки Retrofit
    будет выполняться только один раз в течение жизни потока.
     */
    private val flickrFetchr = FlickrFetchr()

    private var hasQuit = false
    override fun quit(): Boolean {
        hasQuit = true
        return super.quit()
    }

    /**
     * requestMap - код обновления
    переменной requestMap и постановки нового сообщения в
    очередь сообщений фонового потока.

    requestHandler - Сообщение берется непосредственно из переменной
    requestHandler, в результате чего поле target нового
    объекта Message немедленно заполняется переменной
    requestHandler. Это означает, что переменная
    requestHandler будет отвечать за обработку сообщения при
    его извлечении из очереди сообщений. Поле what сообщения
    заполняется значением MESSAGE_DOWNLOAD. В поле obj
    заносится значение target (PhotoHolder в данном случае),
    переданное функцией queueThumbnail(...).

     */
    fun queueThumbnail(target: T, url: String) {
        Log.i(TAG, "Got a URL: $url")
        requestMap[target] = url
        requestHandler.obtainMessage(MESSAGE_DOWNLOAD, target)
            .sendToTarget()


    }


    /**
     * В коде Handler.handleMessage(...) мы проверяем тип
    сообщения, читаем значение obj (которое имеет тип T и
    служит идентификатором для запроса) и передаем его функции
    handleRequest(...).

     */
    @Suppress("UNCHECKED_CAST")
    @SuppressLint("HandlerLeak")
    override fun onLooperPrepared() {
        super.onLooperPrepared()
        requestHandler = object : Handler() {
            override fun handleMessage(msg: Message) {
                super.handleMessage(msg)
                if (msg.what == MESSAGE_DOWNLOAD) {
                    val target = msg.obj as T
                    Log.i(TAG, "Got a request for URL: ${requestMap[target]}")
                    handleRequest(target)
                }
            }


        }
    }

    /**
     * Вся загрузка осуществляется в функции handleRequest().
    Мы проверяем существование URL-адреса, после чего передаем
    его новому экземпляру знакомого класса FlickrFetchr. При
    этом используется функция
    FlickrFetchr.getUrlBytes(...).
     * А поскольку responseHandler связывается с Looper
    главного потока, весь код функции run() в Runnable будет
    выполнен в главном потоке.
    Что делает этот код? Сначала он проверяет requestMap.
    Такая проверка необходима, потому что RecyclerView заново
    использует свои представления. К тому времени, когда
    ThumbnailDownloader завершит загрузку Bitmap, может
    оказаться, что виджет RecyclerView уже переработал
    ImageView и запросил для него изображение с другого URL-адреса. Эта проверка гарантирует, что каждый объект
    PhotoHolder получит правильное изображение, даже если за
    прошедшее время был сделан другой запрос.
    Затем проверяется hasQuit. Если выполнение
    ThumbnailDownloader уже завершилось, выполнение каких-либо обратных вызовов небезопасно.
    Наконец, мы удаляем из requestMap связь «PhotoHolder
    —URL» и назначаем изображение для PhotoHolder.
     */

    private fun handleRequest(target: T) {
        val url = requestMap[target] ?: return
        val bitmap = flickrFetchr.fetchPhoto(url) ?: return
        responseHandler.post(Runnable {
            if (requestMap[target] != url) {
                return@Runnable
            }
            requestMap.remove(target)
            onThumbnailDownloaded(target, bitmap)
        })
    }
//Рефакторинг наблюдателя за жизненным циклом фрагмента
    val fragmentLifecycleObserver:
            LifecycleObserver = object : LifecycleObserver {
        @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
        fun setup() {
            Log.i(TAG, "Starting background thread")
            start()
            looper
        }

        @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        fun tearDown() {
            Log.i(TAG, "Destroying background thread")
            quit()
        }
    }
    fun clearQueue() {
        requestHandler.removeMessages(MESSAGE_DOWNLOAD)
        requestMap.clear()
    }
//Добавление наблюдателя жизненного цикла представления
    val viewLifecycleObserver:
            LifecycleObserver =
        object : LifecycleObserver {
            @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
            fun tearDown() {
                Log.i(TAG, "Clearing all requests from queue")
                requestHandler.removeMessages(MESSAGE_DOWNLOAD)
                requestMap.clear()
            }
        }


}


