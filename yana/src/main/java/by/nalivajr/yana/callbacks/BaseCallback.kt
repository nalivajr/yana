package by.nalivajr.yana.callbacks

/**
 * Created by Sergey Nalivko
 * Skype: nalivko_sergey
 */
interface BaseCallback<T> {

    fun onSuccess(result: T)

    fun onFailure(e: Throwable)
}