package by.nalivajr.yana.tools

import android.content.ContentValues
import android.database.Cursor
import by.nalivajr.yana.models.*
import com.google.gson.Gson
import java.math.BigInteger
import by.nalivajr.yana.models.DatabaseSchema as DBSchema;
import java.util.*
import kotlin.reflect.KClass

/**
 * Created by Sergey Nalivko
 * Skype: nalivko_sergey
 */
class MessageUtils {
    companion object Utils {

        private val gson = Gson();

        /**
         * Converts [Message] instance to [ContentValues] object
         * @param message the message to be converted
         */
        fun <T : Any> toContentValues(message: Message<T>): ContentValues {
            val contentValues = ContentValues()
            contentValues.put(DBSchema.Message.MESSAGE_ID, message.messageId.toString())
            contentValues.put(DBSchema.Message.SENDER_ID, message.senderId)
            putNullable(contentValues, DBSchema.Message.RECIPIENT_ID, message.recipientId)
            putNullable(contentValues, DBSchema.Message.GROUP_ID, message.groupId)
            contentValues.put(DBSchema.Message.CREATION_DATE, message.creationDate.time)
            putNullable(contentValues, DBSchema.Message.COMMAND, message.command)
            contentValues.put(DBSchema.Message.ORDERED, message.isOrdered)
            contentValues.put(DBSchema.Message.ORDER, message.order.toString())
            putNullable(contentValues, DBSchema.Message.PAYLOAD, gson.toJson(message.payload))
            return contentValues
        }

        /**
         * Converts cursor to instance of [Message]
         * return `null` if cursor is null or closed otherwise returns converted message
         */
        fun <T : Any> fromCursor(cursor: Cursor, payloadClass: KClass<T>?): Message<T> {
            val messageId = cursor.getString(cursor.getColumnIndex(DBSchema.Message.MESSAGE_ID))
            val senderId = cursor.getString(cursor.getColumnIndex(DBSchema.Message.SENDER_ID))
            val recipientId = cursor.getString(cursor.getColumnIndex(DBSchema.Message.RECIPIENT_ID))
            val groupId = cursor.getString(cursor.getColumnIndex(DBSchema.Message.GROUP_ID))
            val creation = cursor.getLong(cursor.getColumnIndex(DBSchema.Message.CREATION_DATE))
            val command = cursor.getString(cursor.getColumnIndex(DBSchema.Message.COMMAND))
            val order = cursor.getString(cursor.getColumnIndex(DBSchema.Message.ORDER))
            val ordered = cursor.getInt(cursor.getColumnIndex(DBSchema.Message.ORDERED)) == 1

            val mutableMessage = MutableMessage<T>(ordered)
            mutableMessage.messageId = BigInteger(messageId)
            mutableMessage.senderId = senderId
            mutableMessage.recipientId = recipientId
            mutableMessage.groupId = groupId
            mutableMessage.creationDate = Date(creation)
            mutableMessage.command = command
            mutableMessage.order = BigInteger(order)
            if (payloadClass != null) {
                val payloadJson = cursor.getString(cursor.getColumnIndex(DBSchema.Message.PAYLOAD));
                mutableMessage.payload = convertPayloadFromJson(payloadJson, payloadClass.java);
            }
            return mutableMessage
        }

        fun <T : Any> convertPayloadFromJson(payloadJson : String?, payloadClass : Class<T>) : T? {
            return payloadJson?.run { gson.fromJson(payloadJson, payloadClass) }
        }


        private fun putNullable(contentValues: ContentValues, key: String, recipientId: String?) {
            if (recipientId == null) {
                contentValues.putNull(key)
            } else {
                contentValues.put(key, recipientId)
            }
        }

    }
}