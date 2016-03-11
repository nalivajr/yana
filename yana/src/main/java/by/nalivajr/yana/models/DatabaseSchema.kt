package by.nalivajr.yana.models

import android.content.ContentResolver
import android.net.Uri

/**
 * Created by Sergey Nalivko
 * Skype: nalivko_sergey
 */
interface DatabaseSchema {

    companion object {
        val DATA_TYPE_TEXT = "TEXT"
        val DATA_TYPE_INTEGER = "INTEGER"

        val AUTHORITY = "YanaAuthority"
    }

    interface Message {

        companion object {
            val TABLE_NAME = "`Message`"
            val CODE_ONE = 100001
            val CODE_MANY = 100002
            val TYPE_ONE = "vnd.android.cursor.item/vnd.$AUTHORITY.$TABLE_NAME";
            val TYPE_MANY = "vnd.android.cursor.dir/vnd.$AUTHORITY.$TABLE_NAME";

            val MESSAGE_ID = "messageId"
            val CREATION_DATE = "creationDate"

            val SENDER_ID = "senderId"
            val RECIPIENT_ID = "recipientId"
            val GROUP_ID = "groupId"

            val ORDER = "order"
            val ORDERED = "isOrdered"

            val COMMAND = "command"
            val PAYLOAD = "payload"
        }
    }
}