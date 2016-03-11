package by.nalivajr.yana.transmission

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import by.nalivajr.yana.callbacks.MessageSentCallback
import by.nalivajr.yana.exceptions.IllegalSenderException
import by.nalivajr.yana.models.DatabaseSchema
import by.nalivajr.yana.models.Message
import by.nalivajr.yana.models.MutableMessage
import by.nalivajr.yana.tools.MessageUtils
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

/**
 * Created by Sergey Nalivko
 * Skype: nalivko_sergey
 */
class YanaMessageSender : MessageSender {

    companion object constants {
        val DEFAULT_POOL_SIZE : Int = 5;
    }

    private val executor : ScheduledExecutorService;
    private val senderId : String;
    private val messageTableUri : Uri;
    private val context : Context ;


    constructor(context : Context, senderId : String, authority : String, poolSize : Int) {
        this.senderId = senderId;
        this.context = context;
        this.messageTableUri = Uri.Builder().scheme(ContentResolver.SCHEME_CONTENT)
                .authority(authority)
                .appendPath(DatabaseSchema.Message.TABLE_NAME)
                .build();
        executor = Executors.newScheduledThreadPool(poolSize);
    }

    constructor(context: Context, senderId : String, authority: String)
    : this(context, senderId, authority, constants.DEFAULT_POOL_SIZE) {}


    override fun getSenderId() : String {
        return senderId;
    }

    @Throws(IllegalArgumentException::class)
    override fun <T> send(message : Message<T>) : Message<T> {
        if (message.senderId != senderId) {
            throw IllegalSenderException(senderId, message.senderId);
        }
        val prepMessage = prepareMessage(message);
        val values = MessageUtils.toContentValues(prepMessage);
        context.contentResolver.insert(messageTableUri, values);
        return prepMessage;
    }

    private fun <T> prepareMessage(message : Message<T>) : Message<T> {
        val msgId = UUID.randomUUID().toString();
        val creationDate = Date();
        try {
            setValViaReflection(message, "messageId", msgId);
            setValViaReflection(message, "creationDate", creationDate);
        } catch (e : Exception) {
            //throwing up for developing efforts
            throw RuntimeException(e);
        }
        return message;
    }

    @Throws(NoSuchFieldException::class, IllegalAccessException::class)
    private fun <T> setValViaReflection(message : Message<T>, fieldName : String, value : Any) {
        val creationField = Message::class.java.getField(fieldName);
        creationField.isAccessible = true;
        creationField.set(message, value);
        creationField.isAccessible = false;
    }

    override fun <T : Any> sendAsync(message : Message<T>, callback : MessageSentCallback?) {
        executor.submit({
            try {
                MutableMessage.of(message);
                val msg = send(message);
                callback?.onSuccess(msg);
            } catch (e : Throwable) {
                callback?.onFailure(e);
            }
        });
    }

    override fun <T> sendOnMyBehalf(message : Message<T>) : Message<T> {
        val messageToSent = message;
        setValViaReflection(messageToSent, "senderId", getSenderId())
        return send(messageToSent);
    }

    override fun sendOnMyBehalfAsync(message : Message<Any>, callback : MessageSentCallback?) {
        val messageToSent = message;
        setValViaReflection(messageToSent, "senderId", getSenderId())
        sendAsync(messageToSent, callback);
    }

    override fun <T> sendWithDelayAsync(message : Message<T>, delay : Long, units : TimeUnit, callback : MessageSentCallback?) {

    }

    override fun <T> sendOnMyBehalfAsync(message : Message<T>, delay : Long, units : TimeUnit, callback : MessageSentCallback?) {

    }

    override fun <T> sendAsyncWhen(message : Message<T>, predicate : Predicate, callback : MessageSentCallback?) {

    }

    override fun <T> sendOnMyBehalfAsyncWhen(message : Message<T>, predicate : Predicate, callback : MessageSentCallback?) {

    }

    override fun close() {
        executor.shutdownNow();
    }

}

