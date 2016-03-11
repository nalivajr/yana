package by.nalivajr.yana.exceptions

import android.net.Uri

/**
 * Created by Sergey Nalivko
 * Skype: nalivko_sergey
 */
class UnknownTableException constructor(uri : Uri) : RuntimeException ("Unsupported uri: ${uri.toString()}."){

    private val uri: Uri = uri;

    fun getUri(): Uri {
        return uri
    }
}