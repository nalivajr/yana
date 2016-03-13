package by.nalivajr.yana.models

import java.math.BigInteger
import java.util.*

/**
 * Created by Sergey Nalivko
 * Skype: nalivko_sergey
 */
open class Message<T : Any> (open val isOrdered: Boolean) {

    open var messageId : BigInteger = BigInteger.ZERO
    protected set;

    open lateinit var creationDate : Date
    protected set;

    open lateinit var senderId : String
    protected set;

    open var recipientId : String? = null
    protected set;

    open var groupId : String? = null;
    protected set

    open var order = BigInteger.ONE.negate();
    protected set

    open var command : String? = null;
    protected set

    open var payload : T? = null;
    protected set

    override fun toString(): String{
        return "Message(isOrdered=$isOrdered, groupId=$groupId, order=$order, command=$command, payload=$payload)"
    }


}
