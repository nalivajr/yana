package by.nalivajr.yana.transmission

import android.content.Context
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import by.nalivajr.yana.Yana
import by.nalivajr.yana.models.DatabaseSchema
import by.nalivajr.yana.models.Message
import by.nalivajr.yana.tools.MessageUtils
import java.math.BigInteger
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * Created by Sergey Nalivko
 * Skype: nalivko_sergey
 */
open class YanaMessageReceiver(private val context: Context,
                          private val authority : String,
                          private val groupId : String? = null,
                          private val receiverId: String) : MessageReceiver {

    companion object {
        private val waitersLock: Any = Any();
        private val stateLock: Any = Any();
    }

    private lateinit var observer : ContentObserver;
    private lateinit var handlerThread : HandlerThread;
    private lateinit var notifierThread : ExecutorService;
    private var startTime = System.currentTimeMillis();
    private var lastMessageId : BigInteger? = null
    private val messageWaiters = HashMap<Pair<String, String?>?, ArrayList<ItemBox>>()
    private var started = false;

    private fun createContentObserver(context: Context, handler: Handler): ContentObserver
            = object : ContentObserver(handler) {

        override fun onChange(selfChange: Boolean, uri: Uri?) {
            val clauseColumn = if (lastMessageId == null) DatabaseSchema.Message.CREATION_DATE else DatabaseSchema.Message.MESSAGE_ID;
            val clauseArg = if (lastMessageId == null) startTime.toString() else lastMessageId.toString()

            val selection = "$clauseColumn > ? AND " +
                    "(${DatabaseSchema.Message.RECIPIENT_ID}=? OR ${DatabaseSchema.Message.RECIPIENT_ID} IS NULL)"
            val selectionArgs = arrayOf(clauseArg, receiverId)
            val cursor = context.contentResolver.query(uri, null, selection, selectionArgs, null);
            val messages = arrayListOf<Message<String>>();
            if (cursor?.moveToFirst() == true) {
                do {
                    val message = MessageUtils.fromCursor(cursor, String::class);
                    messages.add(message)
                } while (cursor.moveToNext())
                cursor.close();
            }
            if (messages.isEmpty() == false) {
                messages.sortWith(Comparator { m1, m2 -> m1.messageId.compareTo(m2.messageId) })
                lastMessageId = messages.last().messageId;
            }

            notifierThread.submit { processReceivedMessage(messages) }
        }
    }

    override fun getReceiverId(): String {
        return receiverId;
    }

    override fun getGroupId(): String? {
        return groupId;
    }

    open override fun onReceive(message: Message<String>) {
        Log.i(YanaMessageReceiver::class.simpleName, "New message received $message")
    }

    override fun waitMessage(invokeOnReceive: Boolean): Message<String> {
        return waitMessageInternal(invokeOnReceive, null);
    }

    override fun waitMessageFrom(senderId: String, group: String?, invokeOnReceive: Boolean): Message<String> {
        val pair = Pair(senderId, group)
        return waitMessageInternal(invokeOnReceive, pair)
    }

    override fun start() {
        synchronized(stateLock, {
            if (started) {
                return@synchronized
            }
            notifierThread = Executors.newCachedThreadPool();
            handlerThread = HandlerThread("MessageReceiverHandlerThread");
            handlerThread.start();

            val handler = Handler(handlerThread.looper)
            val uri = Yana.buildUri(authority, DatabaseSchema.Message.TABLE_NAME);

            observer = createContentObserver(context, handler)
            context.contentResolver.registerContentObserver(uri, true, observer)

            startTime = System.currentTimeMillis();
            started = true;

        })
    }

    override fun stop() {
        synchronized(stateLock, block = {
            if (!started) {
                return@synchronized
            }
            notifierThread.shutdownNow();
            handlerThread.quit();
            context.contentResolver.unregisterContentObserver(observer);
            started = false;
        })
    }

    private fun waitMessageInternal(invokeOnReceive: Boolean, pair: Pair<String, String?>?): Message<String> {
        val box = ItemBox();
        registerWaiter(box, pair)
        while (box.isEmpty()) {
            Thread.sleep(10);
        }
        val item = box.item!!
        if (invokeOnReceive) {
            onReceive(item);
        }
        return item
    }

    private fun registerWaiter(box: ItemBox, pair: Pair<String, String?>?) {
        synchronized(waitersLock, {
            var registered = messageWaiters[pair];
            if (registered == null) {
                registered = arrayListOf();
                messageWaiters.put(null, registered);
            }
            registered.add(box);
        })
    }

    private fun processReceivedMessage(message: ArrayList<Message<String>>) {
        try {
            var registeredWaiters : MutableMap<Pair<String, String?>?, List<ItemBox>> = HashMap();
            synchronized(waitersLock, {
                registeredWaiters.putAll(messageWaiters);
                messageWaiters.clear();
            })
            message.forEach {
                if (checkAwaitedAndNoOnReceiveRequired(registeredWaiters, it)) {
                    return@forEach;
                }
                onReceive(it);
            }
            registeredWaiters.clear();
        } catch (e : Throwable) {
            Log.e(YanaMessageReceiver::class.simpleName, "Unexpected error", e)
        }
    }

    private fun checkAwaitedAndNoOnReceiveRequired(waiters: MutableMap<Pair<String, String?>?, List<ItemBox>>,
                                                   message: Message<String>): Boolean {

        var initialSize = waiters.size;
        waiters.remove(null)?.forEach { it -> it.put(message) };
        waiters.remove(Pair(message.senderId, message.groupId))?.forEach { it -> it.put(message) }
        return initialSize != waiters.size;
    }

    private class ItemBox {
        var item : Message<String>? = null;

        fun put(item: Message<String>) {
            this.item = item;
        }

        fun isEmpty() : Boolean = item == null
    }
}