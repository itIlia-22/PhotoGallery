package com.bignerdranch.android.photogallery.view

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bignerdranch.android.photogallery.work.PollWork
private const val TAG = "VisibleFragment"
abstract class VisibleFragment() : Fragment() {
    /**
     * Создание и регистрация динамического приемника
    Теперь вам понадобится приемник для широковещательного
    интента ACTION_SHOW_NOTIFICATION. Его задача будет
    заключаться в предотвращении показа уведомления, если
    приложение находится на переднем плане.
    Этот приемник будет зарегистрирован лишь тогда, когда
    activity находится на переднем плане. Если бы этот приемник
    был объявлен на более длительный срок службы (например, в
    течение жизненного цикла процесса вашего приложения), то
    вам пришлось бы как-то иначе узнавать, что
    PhotoGalleryFragment запущен (и тогда динамический
    приемник был бы не нужен).
    Задача решается использованием динамического
    широковещательного приемника. Он регистрируется в коде, а
    не в манифесте. Для регистрации приемника используется
    вызов функции Context.registerReceiver(BroadcastReceiver,IntentFilter), а для ее отмены — вызов
    Context.unregisterReceiver (Broadcast Receiver). Сам
    приемник обычно определяется как внутренний экземпляр или
    лямбда, по аналогии со слушателем щелчка на кнопке. Но
    поскольку в функциях registerReceiver(...) и
    unregisterReceiver(...) должен использоваться один
    экземпляр, приемник необходимо присвоить переменной
    экземпляра.

     * В нашей ситуации нужно
    отменить оповещение; эта информация передается в виде
    простого целочисленного кода результата путем присвоения
    resultCode значения Activity.RESULT_CANCELED.
    Внесите изменения в VisibleFragment, чтобы вернуть
    информацию отправителю SHOW_NOTIFICATION. Информация
    также будет отправляться другим широковещательным
    приемникам по цепочке.

     *Так как в нашем примере необходимо лишь подать сигнал
    «да/нет», нам достаточно кода результата. Если потребуется
    вернуть более сложные данные, используйте значение
    resultData или вызывайте функцию
    setResultExtras(Bundle?). А если захотите задать все три
    значения, вызовите функцию
    setResult(Int,String?,Bundle?).
     *
     */
    private val onShowNotification = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            Log.i(TAG, "canceling notification")
            resultCode = Activity.RESULT_CANCELED

        }

    }

    /**
     * Динамически регистрируемые широковещательные
    приемники также должны принять меры для своей
    деинициализации. Как правило, если вы регистрируете
    приемник в функции жизненного цикла, вызываемом при
    запуске, в соответствующей функции завершения вызывается
    функция Context.unregisterReceiver
    (BroadcastReceiver). В нашем примере регистрация
    выполняется в onStart() и отменяется в onStop().
    Аналогичным образом, если бы регистрация выполнялась в
    onCreate(...), то отменяться она должна была бы в
    onDestroy().
    (Кстати, будьте осторожны с onCreate(...) и
    onDestroy() при удержании фрагментов. Функция
    getActivity() будет возвращать разные значения в
    onCreate(...) и onDestroy(), если экран был повернут.
    Если вы хотите регистрировать/отменять регистрацию во
    Fragment.onCreate(...) и Fragment.onDestroy(),
    используйте
    requireActivity().getApplicationContext().)
    Сделайте PhotoGalleryFragment подклассом только что
    созданного класса VisibleFragment.
     */
    override fun onStart() {
        super.onStart()
        val filter = IntentFilter(PollWork.ACTION_SHOW_NOTIFICATION)
        requireActivity().registerReceiver(
            onShowNotification,
            filter,
            PollWork.PERM_PRIVATE,
            null
        )


    }

    override fun onStop() {
        super.onStop()
        requireActivity().unregisterReceiver(onShowNotification)
    }
}