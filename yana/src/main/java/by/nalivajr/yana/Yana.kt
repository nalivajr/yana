package by.nalivajr.yana

import android.content.ContentResolver
import android.net.Uri
import by.nalivajr.yana.models.Message
import by.nalivajr.yana.models.MutableMessage
import by.nalivajr.yana.tools.MessageBuilder
import by.nalivajr.yana.tools.MessageUtils
import kotlin.reflect.KClass

/**
 * Created by Sergey Nalivko
 * Skype: nalivko_sergey
 */
class Yana {
    companion object {
        fun buildUri(authority : String, tableName : String) : Uri {
            return Uri.Builder().scheme(ContentResolver.SCHEME_CONTENT)
                    .authority(authority)
                    .appendPath(tableName)
                    .build();
        }

        fun <T : Any> asMessageWitPayload(message: Message<String>, payloadClass: Class<T>): Message<T> {
            val convertedPayload = MessageUtils.convertPayloadFromJson(message.payload, payloadClass);
            val result = MutableMessage<T>(message.isOrdered);
            result.command = message.command;
            result.payload = convertedPayload;
            result.creationDate = message.creationDate;
            result.groupId = message.groupId;
            result.senderId = message.senderId;
            result.recipientId = message.recipientId;
            result.messageId = message.messageId;
            result.order = message.order;
            return result;
        }
    }
}