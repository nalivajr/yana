package by.nalivajr.yana.models

import java.math.BigInteger
import java.util.*

/**
 * Created by Sergey Nalivko
 * Skype: nalivko_sergey
 */
abstract class Message<T> (open val isOrdered: Boolean) {

    open lateinit var messageId : String
    protected set;

    open lateinit var creationDate : Date
    protected set;

    open lateinit var senderId : String
    protected set;

    open lateinit var recipientId : String
    protected set;

    open val groupId : String? = null;
    open val order = BigInteger.ONE.negate();
    open val command : String? = null;
    open val payload : T? = null;
}
