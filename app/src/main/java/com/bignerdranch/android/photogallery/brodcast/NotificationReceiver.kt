package com.bignerdranch.android.photogallery.brodcast

import android.app.Activity
import android.app.Notification
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationManagerCompat
import com.bignerdranch.android.photogallery.work.PollWork

/**
 * Широковещательный приемник — компонент, который
получает интенты, как и служба или activity. При получении
интента экземпляром NotificationReceiver будет вызвана
его функция onReceive(...).
Откройте файл AndroidManifest.xml и включите
объявление NotificationReceiver как автономный
приемник.

 */
private const val TAG = "NotificationReceiver"

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.i(TAG, "received result: $resultCode")
        /**
         * Чтобы NotificationReceiver принимал трансляцию
        после вашего динамически зарегистрированного приемника
        (чтобы можно было проверить, следует ли ему размещать
        уведомление в NotificationManager), вам необходимо
        установить низкий приоритет для NotificationReceiver в
        манифесте. Назначьте приоритет -999, чтобы он работал
        последним. Это самый низкий приоритет, который можно
        задать (значения -1000 и ниже зарезервированы).
         */
        if (resultCode != Activity.RESULT_OK){
            val requestCode = intent.getIntExtra(PollWork.REQUEST_CODE,0)
            val notification: Notification = intent.getParcelableExtra(PollWork.NOTIFICATION)!!
            val notificationManager = context.let { NotificationManagerCompat.from(it) }
            notificationManager.notify(requestCode,notification)
        }
    }
}