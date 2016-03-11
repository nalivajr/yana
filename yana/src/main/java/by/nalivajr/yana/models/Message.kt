package by.nalivajr.yana.models

import java.math.BigInteger
import java.util.*

/**
 * Created by Sergey Nalivko
 * Skype: nalivko_sergey
 */
abstract class Message<T : Any> (open val isOrdered: Boolean) {

    open lateinit var messageId : String
    protected set;

    open lateinit var creationDate : Date
    protected set;

    open lateinit var senderId : String
    protected set;

    open var recipientId : String? = null;

    open val groupId : String? = null;
    open val order = BigInteger.ONE.negate();
    open val command : String? = null;
    open val payload : T? = null;
    override fun toString(): String{
        return "Message(isOrdered=$isOrdered, groupId=$groupId, order=$order, command=$command, payload=$payload)"
    }


}
