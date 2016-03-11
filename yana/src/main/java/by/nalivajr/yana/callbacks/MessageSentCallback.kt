package by.nalivajr.yana.callbacks

import by.nalivajr.yana.models.Message

/**
 * Created by Sergey Nalivko
 * Skype: nalivko_sergey
 */
interface MessageSentCallback : BaseCallback<Message<out Any>> {

    object stubCallback : MessageSentCallback {
        override fun onSuccess(result: Message<out Any>) {}
        override fun onFailure(e: Throwable) {}
    }
}