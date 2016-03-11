package by.nalivajr.yana

import android.content.ContentResolver
import android.net.Uri

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
    }
}