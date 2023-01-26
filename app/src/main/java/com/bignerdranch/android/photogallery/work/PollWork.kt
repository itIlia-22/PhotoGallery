package com.bignerdranch.android.photogallery.work

import android.annotation.SuppressLint
import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.bignerdranch.android.photogallery.FlickrFetchr
import com.bignerdranch.android.photogallery.QueryPreferences
import com.bignerdranch.android.photogallery.R
import com.bignerdranch.android.photogallery.channel.NOTIFICATION_CHANNEL_ID
import com.bignerdranch.android.photogallery.model.GalleryItem
import com.bignerdranch.android.photogallery.view.PhotoGalleryActivity

const val TAG = "PollWork"

class PollWork(private val context: Context, workerParams: WorkerParameters) :
    Worker(context, workerParams) {
    /**@author
     * Функция doWork() вызывается из фонового потока,
    поэтому вы можете выполнять в ней любые долгосрочные
    задачи
     */
    @SuppressLint("MissingPermission")
    override fun doWork(): Result {
        val query = QueryPreferences.getStoredQuery(context)
        val lastResultId = QueryPreferences.getLastResultId(context)
        val items: List<GalleryItem> = if (query.isEmpty()) {
            FlickrFetchr().fetchPhotosRequest()
                .execute()
                .body()
                ?.photos
                ?.galleryItem
        } else {
            FlickrFetchr().searchPhotosRequest(query)
                .execute()
                .body()
                ?.photos
                ?.galleryItem
        } ?: emptyList()

        if (items.isEmpty()) {
            return Result.success()
        }
        val resultId = items.first().id
        if (resultId == lastResultId) {
            Log.i(TAG, "Got an old result:$resultId")
        } else {
            Log.i(TAG, "Got a new result:$resultId")
            QueryPreferences.setLastResultId(context, resultId)
            /**
             * @
             * Мы используем класс NotificationCompat для работы с
            уведомлениями как на устройствах до Oreo, так и после Oreo.
            NotificationCompat.Builder принимает ID канала и
            использует его для установки параметра канала уведомления,
            если пользователь запустил приложение на Oreo или выше.
            Если у пользователя запущена более ранняя версия Android,
            NotificationCompat.Builder игнорирует канал. (Обратите
            внимание, что идентификатор канала, который вы передаете
            здесь, происходит от константы NOTIFICATION_CHANNEL_ID,
            которую вы добавили в PhotoGalleryApplication.)
            В листинге 27.9 перед созданием канала вы проверили
            версию SDK, потому что AppCompat API для создания канала не
            существует. Здесь это не нужно делать, потому что
            NotificationCompat в AppCompat выполняет всю работу по
            проверке версии сборки, сохраняя ваш код в чистоте и красоте.
            Это одна из причин, по которой нужно использовать версию
            AppCompat Android API, когда это доступно.
            Текст уведомления и значок мы настраиваем с помощью
            функций setTicker (CharSequence) и
            setSmallIcon(Int). (Ресурс значка, который вы используете,
            входит во фреймворк Android, обозначается классификатором
            имен пакетов
            androidвandroid.R.drawable.ic_menu_report_image, так
            что вам не нужно переносить изображение значка в папку
            ресурса.)
            После этого мы настроим внешний вид Notification на
            панели. Можно создать полностью пользовательский внешний
            вид, но проще всего использовать стандартный вид
            уведомления, в котором есть иконка, заголовок и текстовая
            область. Иконка берется из функции setSmallIcon(Int). Для
            установки заголовка и текста вызываются функции
            setContentTitle(CharSequence) и
            setContentText(CharSequence) соответственно.
            Затем мы задаем, что произойдет, когда пользователь
            нажмет на уведомление. Это делается с помощью объекта
            PendingIntent. Объект PendingIntent, который вы
            передаете в setContentIntent(PendingIntent), будет
            уничтожен, когда пользователь нажмет на уведомление на
            панели. Вызов функции setAutoCancel(true) немного
            корректирует это поведение: уведомление также будет удалено
            из ящика уведомлений, когда пользователь нажмет на него.
            Наконец, мы получаем экземпляр NotificationManager
            из текущего контекста (NotificationManagerCompat.from)
            и вызываем функцию NotificationManager.notify(...)
            для размещения вашего уведомления.
            Целый параметр, который вы передаете в функцию
            notify(...), является идентификатором вашего
            уведомления. Он должен быть уникальным во всем вашем
            приложении, но может быть использован повторно. Одно
            уведомление заменит другое тем же самым идентификатором,
            который все еще находится в ящике уведомлений. Если нет
            существующего уведомления с идентификатором, система
            покажет новое уведомление. Именно так вы реализуете
            индикатор выполнения или другие динамические
            визуализации.

             */
            val intent =
                PhotoGalleryActivity.newIntent(context)
            val pendingIntent =
                PendingIntent.getActivity(context, 0, intent, 0)
            val resources = context.resources
            val notification = NotificationCompat
                .Builder(
                    context, NOTIFICATION_CHANNEL_ID
                )
                .setTicker(
                    resources.getString(R.string.new_pictures_title)
                )
                .setSmallIcon(
                    android.R.drawable.ic_menu_report_image
                )
                .setContentTitle(
                    resources.getString(R.string.new_pictures_title)
                )
                .setContentText(
                    resources.getString(R.string.new_pictures_text)
                )
                .setContentIntent(
                    pendingIntent
                )
                .setAutoCancel(true)
                .build()
            val notificationManager =
                NotificationManagerCompat.from(context)
            notificationManager.notify(0, notification)
            showBackgroundNotification(0, notification)

        }
        return Result.success()
    }

    /**
     * Функция
    Context.sendOrderedBroadcast(Intent,String?) ведет
    себя очень похоже на функцию sendBroadcast(...), но при
    этом гарантирует, что трансляция будет доставлена приемнику
    вовремя. Код результата устанавливается равным
    Activity.RESULT_OK, когда эта трансляция будет отправлена.
     */

    private fun showBackgroundNotification(requestCode: Int, notification: Notification) {
        val intent = Intent(ACTION_SHOW_NOTIFICATION).apply {
            putExtra(REQUEST_CODE, requestCode)
            putExtra(NOTIFICATION, notification)

        }
        context.sendBroadcast(intent, PERM_PRIVATE)
    }

    /**
     * Самая простая часть решения — отправка ваших собственных
    широковещательных интентов. Если говорить конкретнее, вы
    разошлете широковещательный интент, который уведомляет
    заинтересованные компоненты о том, что оповещение о новых
    результатах поиска готово. Чтобы отправить
    широковещательный интент, просто создайте интент и
    передайте его sendBroadcast(Intent). В нашем случае
    широковещательная рассылка будет применяться к
    определенному нами действию, поэтому также следует
    определить константу действия.
     */

    companion object {
        const val ACTION_SHOW_NOTIFICATION =
            "com.bignerdranch.android.photogallery.SHOW_NOTIFICATION"
        const val PERM_PRIVATE =
            "com.bignerdranch.android.photogallery.PRIVATE"
        const val REQUEST_CODE = "REQUEST_CODE"
        const val NOTIFICATION = "NOTIFICATION"
    }
}