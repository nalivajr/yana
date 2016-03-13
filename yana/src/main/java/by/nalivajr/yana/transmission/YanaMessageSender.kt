package by.nalivajr.yana.transmission

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import by.nalivajr.yana.callbacks.MessageSentCallback
import by.nalivajr.yana.exceptions.IllegalSenderException
import by.nalivajr.yana.models.DatabaseSchema
import by.nalivajr.yana.models.Message
import by.nalivajr.yana.tools.MessageUtils
import java.util.*
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import kotlin.reflect.KMutableProperty
import kotlin.reflect.declaredMemberProperties

/**
 * Created by Sergey Nalivko
 * Skype: nalivko_sergey
 */
open class YanaMessageSender : MessageSender {

    companion object constants {
        val DEFAULT_POOL_SIZE : Int = 5;
        private val lock = Any();
    }

    private var executor : ScheduledExecutorService;
    private var stateCheckingExecutor : ScheduledExecutorService;
    private val senderId : String;
    private val messageTableUri : Uri;
    private val pendingMessages = CopyOnWriteArrayList<() -> Boolean>()
    private val context : Context ;
    private var started = false;
    private var poolSize = DEFAULT_POOL_SIZE;


    constructor(context : Context, senderId : String, authority : String, poolSize : Int) {
        this.senderId = senderId;
        this.context = context;
        this.messageTableUri = Uri.Builder().scheme(ContentResolver.SCHEME_CONTENT)
                .authority(authority)
                .appendPath(DatabaseSchema.Message.TABLE_NAME)
                .build();
        this.poolSize = poolSize;
        executor = Executors.newScheduledThreadPool(poolSize);
        stateCheckingExecutor = Executors.newSingleThreadScheduledExecutor();
    }

    constructor(context: Context, senderId : String, authority: String)
    : this(context, senderId, authority, constants.DEFAULT_POOL_SIZE) {}


    override fun getSenderId() : String {
        return senderId;
    }

    @Throws(IllegalArgumentException::class)
    override fun <T : Any> send(message : Message<T>) : Message<T> {
        if (message.senderId != senderId) {
            throw IllegalSenderException(senderId, message.senderId);
        }
        val prepMessage = prepareMessage(message);
        val values = MessageUtils.toContentValues(prepMessage);
        context.contentResolver.insert(messageTableUri, values);
        return prepMessage;
    }

    private fun <T : Any> prepareMessage(message : Message<T>) : Message<T> {
        val msgId = UUID.randomUUID().toString();
        val creationDate = Date();
        try {
            setValViaReflection(message, "creationDate", creationDate);
        } catch (e : Exception) {
            //throwing up for developing efforts
            throw RuntimeException(e);
        }
        return message;
    }

    @Throws(NoSuchFieldException::class, IllegalAccessException::class)
    private fun <T : Any> setValViaReflection(message : Message<T>, fieldName : String, value : Any) {
        val map = message.javaClass.kotlin.declaredMemberProperties.associate { it.name to it }
        (map[fieldName] as KMutableProperty<*>).setter.call(message, value);
    }

    override fun <T : Any> sendAsync(message : Message<T>, callback : MessageSentCallback?) {
        executor.submit({
            try {
                val msg = send(message);
                callback?.onSuccess(msg);
            } catch (e : Throwable) {
                callback?.onFailure(e);
            }
        });
    }

    override fun <T : Any> sendOnMyBehalf(message : Message<T>) : Message<T> {
        val messageToSent = message;
        setValViaReflection(messageToSent, "senderId", getSenderId())
        return send(messageToSent);
    }

    override fun sendOnMyBehalfAsync(message : Message<Any>, callback : MessageSentCallback?) {
        val messageToSent = message;
        setValViaReflection(messageToSent, "senderId", getSenderId())
        sendAsync(messageToSent, callback);
    }

    override fun <T : Any> sendWithDelayAsync(message : Message<T>, delay : Long, units : TimeUnit, callback : MessageSentCallback?) {
        val timeToSendMillis = System.currentTimeMillis() + TimeUnit.MILLISECONDS.convert(delay, units);
        val allowSendState = { System.currentTimeMillis() >= timeToSendMillis };
        sendAsyncWhen(message, allowSendState, callback);
    }

    override fun <T : Any> sendOnMyBehalfAsync(message : Message<T>, delay : Long, units : TimeUnit, callback : MessageSentCallback?) {
        val allowedToSendState = { true }
        sendOnMyBehalfAsyncWhen(message, allowedToSendState, callback)
    }

    override fun <T : Any> sendAsyncWhen(message: Message<T>, predicate: () -> Boolean, callback: MessageSentCallback?) {
        val checkStateTask : () -> Boolean = {
            val stateReady = predicate.invoke()
            if (stateReady) {
                sendAsync(message, callback);
            }
            stateReady
        }
        pendingMessages.add(checkStateTask);
    }

    override fun <T : Any> sendOnMyBehalfAsyncWhen(message : Message<T>, predicate: () -> Boolean, callback : MessageSentCallback?) {
        val messageToSent = message;
        setValViaReflection(messageToSent, "senderId", getSenderId())
        sendAsyncWhen(message, predicate, callback);
    }

    override fun start() {
        synchronized(lock, {
            if (started) {
                return@synchronized;
            }
            executor = Executors.newScheduledThreadPool(poolSize);
            stateCheckingExecutor = Executors.newSingleThreadScheduledExecutor();

            val checkingAction = Runnable {
                if (pendingMessages.isEmpty()) {
                    return@Runnable;
                }
                val task = pendingMessages.get(0);
                val consumed = task.invoke();
                if (consumed) pendingMessages.remove(task);

            };
            stateCheckingExecutor.scheduleAtFixedRate(checkingAction, 0, 100, TimeUnit.MILLISECONDS);
            started = true;
        })
    }

    override fun stop() {
        synchronized(lock, {
            started = false;
            executor.shutdownNow();
            stateCheckingExecutor.shutdownNow();
        })
    }

}

