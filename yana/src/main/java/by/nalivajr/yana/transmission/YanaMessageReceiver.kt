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
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * Created by Sergey Nalivko
 * Skype: nalivko_sergey
 */
class YanaMessageReceiver(context: Context, authority : String,
                          private val groupId : String? = null,
                          private val receiverId: String) : MessageReceiver {

    companion object {
        private val lock: Any = Any();
    }

    private val observer : ContentObserver;
    private val handlerThread : HandlerThread;
    private var lastEvenTimestamp = System.currentTimeMillis();
    private val notifierThread : ExecutorService;
    private val messageWaiters = HashMap<Pair<String, String?>?, ArrayList<ItemBox>>()

    init {
        notifierThread = Executors.newCachedThreadPool();
        handlerThread = HandlerThread("MessageReceiverHandlerThread");
        handlerThread.start();

        val handler = Handler(handlerThread.looper)
        val uri = Yana.buildUri(authority, DatabaseSchema.Message.TABLE_NAME);

        observer = createContentObserver(context, handler)
        context.contentResolver.registerContentObserver(uri, true, observer)
    }

    private fun createContentObserver(context: Context, handler: Handler): ContentObserver
            = object : ContentObserver(handler) {

        override fun onChange(selfChange: Boolean, uri: Uri?) {
            var checkTimestamp = System.currentTimeMillis();

            val selection = "${DatabaseSchema.Message.CREATION_DATE} >= ? AND " +
                    "(${DatabaseSchema.Message.RECIPIENT_ID}=? OR ${DatabaseSchema.Message.RECIPIENT_ID} IS NULL)"
            val selectionArgs = arrayOf(receiverId, lastEvenTimestamp.toString())
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
                messages.sortWith(Comparator { m1, m2 -> m1.creationDate.compareTo(m2.creationDate) })
                checkTimestamp = messages.last().creationDate.time;
            }
            lastEvenTimestamp = checkTimestamp;


            notifierThread.submit { processReceivedMessage(messages) }
        }
    }

    override fun getReceiverId(): String {
        return receiverId;
    }

    override fun getGroupId(): String? {
        return groupId;
    }

    override fun onReceive(message: Message<String>) {
        Log.i(YanaMessageReceiver::class.simpleName, "New message received $message")
    }

    override fun waitMessage(invokeOnReceive: Boolean): Message<String> {
        return waitMessageInternal(invokeOnReceive, null);
    }

    override fun waitMessageFrom(senderId: String, group: String?, invokeOnReceive: Boolean): Message<String> {
        val pair = Pair(senderId, group)
        return waitMessageInternal(invokeOnReceive, pair)
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
        synchronized(lock, {
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
            synchronized(lock, {
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