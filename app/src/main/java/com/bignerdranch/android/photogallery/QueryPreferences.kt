package com.bignerdranch.android.photogallery

import android.content.Context
import android.preference.PreferenceManager
import androidx.core.content.edit

private const val PREF_SEARCH_QUERY = "searchQuery"
private const val PREF_LAST_RESULT_ID = "lastResultId"
private const val PREF_IS_POLLING = "isPolling"


/**
 * Значение PREF_SEARCH_QUERY используется в качестве
ключа для хранения запроса. Этот ключ применяется во всех
операциях чтения или записи запроса.

 */
object QueryPreferences {

    /**
     * Функция
    PreferencesManager.getDefaultSharedPreferences(Con
    text) возвращает экземпляр с именем по умолчанию и
    приватными разрешениями (так что предпочтения доступны
    только изнутри вашего приложения). Чтобы получить
    конкретный экземпляр SharedPreferences, вы можете
    использовать функцию
    Context.getSharedPreferences(String,Int).
     * Функция getStoredQuery(Context) возвращает значение
    запроса, хранящееся в общих настройках. Для этого функция
    сначала получает объект SharedPreferences по умолчанию
    для заданного контекста. (Так как QueryPreferences не имеет
    собственного контекста, вызывающий компонент должен
    передать свой контекст как входной параметр.)
    Получение ранее сохраненного значения сводится к
    простому вызову SharedPreferences.getString(...),
    SharedPreferences.getInt(...) или другой функции,
    соответствующей типу данных. Второй параметр
    SharedPreferences.getString (String,String)
    определяет возвращаемое значение по умолчанию, которое
    должно возвращаться при отсутствии записи с ключом
    PREF_SEARCH_QUERY.
    Возвращаемый тип SharedPreferences.getString(...)
    определен как тип String, допускающий значение null, так как
    компилятор не может гарантировать, что значение, связанное с
    PREF_SEARCH_QUERY, существует и что оно не является null. Но
    вы знаете, что такой ситуации не бывает, поэтому
    предоставляете пустую строку в качестве значения по
    умолчанию в тех случаях, когда функция
    setStoredQuery(context:Context,query:String) еще не
    вызывалась. Поэтому здесь безопасно использовать оператор
    ненулевого утверждения (!!) без блока try/catch.

     */
    fun getStoredQuery(context: Context): String {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        return prefs.getString(PREF_SEARCH_QUERY, "")!!
    }

    /**
     * Функция setStoredQuery(Context) записывает запрос в
    хранилище общих настроек для заданного контекста. В
    приведенном выше коде QueryPreferences вызов
    SharedPreferences.edit() используется для получения
    экземпляра SharedPreferences.Editor. Этот класс
    используется для сохранения значений в SharedPreferences.
    Он позволяет объединять изменения в транзакции по аналогии
    с тем, как это делается во FragmentTransaction.
    Множественные изменения могут быть сгруппированы в одну
    операцию записи в хранилище.
    После того как все изменения будут внесены, вызовите
    apply() для объекта Editor, чтобы эти изменения стали
    видимыми для всех пользователей файла SharedPreferences.
    Функция apply() вносит изменения в память немедленно, а
    непосредственная запись в файл осуществляется в фоновом
    потоке.
    QueryPreferences предоставляет всю функциональность
    долгосрочного хранения данных для PhotoGallery.
     */
    fun setStoredQuery(context: Context, query: String) {
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit {
                putString(PREF_SEARCH_QUERY, query)
            }


    }

    fun getLastResultId(context: Context): String {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        return prefs.getString(PREF_LAST_RESULT_ID, "")!!
    }

    fun setLastResultId(context: Context, lastResult: String) {
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit {
                putString(PREF_LAST_RESULT_ID, lastResult)
            }
    }

    /**
     * Для переключения между функциями сначала необходимо
    определить, запущен ли работник в данный момент. Для этого
    добавьте QueryPreferences, чтобы сохранить флаг,
    указывающий, включен ли работник.
     *@author
     */
    fun isPolling(context: Context): Boolean {
        return PreferenceManager.getDefaultSharedPreferences(context)
            .getBoolean(PREF_IS_POLLING, false)
    }

    fun setPolling(
        context: Context, isOn:
        Boolean
    ) {
        PreferenceManager.getDefaultSharedPreferences(context).edit {
            putBoolean(PREF_IS_POLLING, isOn)
        }
    }

}