package by.nalivajr.yana.transmission

import by.nalivajr.yana.callbacks.MessageSentCallback
import by.nalivajr.yana.exceptions.IllegalSenderException
import by.nalivajr.yana.models.Message
import java.util.concurrent.TimeUnit

/**
 * Created by Sergey Nalivko
 * Skype: nalivko_sergey
 */
interface MessageSender {

    /**
     * @return the identifier of this sender instance, which must be
     * * the same in all messages which will be send by this sender.
     */
    fun getSenderId(): String

    /**
     * Sends the message
     * @param message the message object to be sent
     * *
     * @return the instance of message which was sent. Should be the same instance as the given one
     * *
     * *
     * @throws IllegalSenderException if message's sender id is not the same as this sender's ID
     * * To force sending such messages use [MessageSender.sendOnMyBehalf]
     */
    @Throws(IllegalSenderException::class)
    fun <T : Any> send(message: Message<T>): Message<T>

    /**
     * This method is similar to [MessageSender.send] but sends the message asynchronously
     * @param message the message object to be sent
     * *
     * @param callback the callback which should be invoked on messages is sent or any error occurred
     */
    fun <T : Any> sendAsync(message: Message<T>, callback: MessageSentCallback?)

    /**
     * Sends the message and updates the senderId in the given message object
     * @param message the message object to be sent
     * *
     * @return new message instance with the sender ID set to this KotlinMessageSender ID
     */
    fun <T : Any> sendOnMyBehalf(message: Message<T>): Message<T>

    /**
     * This method is similar to [MessageSender.sendOnMyBehalf] but sends the message asynchronously.
     * The message returned in callback is a new instance of KotlinMessage with the sender ID set to this KotlinMessageSender ID
     * @param message the message object to be sent
     * *
     * @param callback the callback which should be invoked on messages is sent or any error occurred
     */
    fun sendOnMyBehalfAsync(message: Message<Any>, callback: MessageSentCallback?)

    /**
     * This method is similar to [MessageSender.sendAsync]
     * but sends message with the given delay
     * The message returned in callback should be the same instance as the given one
     * @param message the message object to be sent
     * *
     * @param delay the initial delay
     * *
     * @param units time units of delay
     * *
     * @param callback the callback which should be invoked on messages is sent or any error occurred
     */
    fun <T : Any> sendWithDelayAsync(message: Message<T>, delay: Long, units: TimeUnit, callback: MessageSentCallback?)

    /**
     * This method is similar to [MessageSender.sendOnMyBehalf]
     * but sends message with the given delay
     * The message returned in callback is a new instance of KotlinMessage with the sender ID set to this KotlinMessageSender ID
     * @param message the message object to be sent
     * *
     * @param delay the initial delay
     * *
     * @param units time units of delay
     * *
     * @param callback the callback which should be invoked on messages is sent or any error occurred
     */
    fun <T : Any> sendOnMyBehalfAsync(message: Message<T>, delay: Long, units: TimeUnit, callback: MessageSentCallback?)


    /**
     * This method is similar to [MessageSender.sendAsync]
     * but periodically checks
     * The message returned in callback should be the same instance as the given one
     * @param message the message object to be sent
     * *
     * @param predicate the object which will be used to periodically check whether message is allowed to be sent
     * *
     * @param callback the callback which should be invoked on messages is sent or any error occurred
     */
    fun <T : Any> sendAsyncWhen(message: Message<T>, predicate: () -> Boolean, callback: MessageSentCallback?)

    /**
     * This method is similar to [MessageSender.sendOnMyBehalf] but sends the message asynchronously
     * The message returned in callback is a new instance of KotlinMessage with the sender ID set to this KotlinMessageSender ID
     * @param message the message object to be sent
     * *
     * @param predicate the object which will be used to periodically check whether message is allowed to be sent
     * *
     * @param callback the callback which should be invoked on messages is sent or any error occurred
     */
    fun <T : Any> sendOnMyBehalfAsyncWhen(message: Message<T>, predicate: () -> Boolean, callback: MessageSentCallback?)

    /**
     * Starts this sender.
     */
    fun start()

    /**
     * Stops this sender.
     */
    fun stop()
}