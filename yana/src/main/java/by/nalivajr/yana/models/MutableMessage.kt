package by.nalivajr.yana.models

import java.math.BigInteger
import java.util.*

/**
 * Created by Sergey Nalivko
 * Skype: nalivko_sergey
 */

/**
 * Base class which represents all message contents, but also
 * provides set- methods to update content
 * @param <T> type of payload object.
 */
class MutableMessage <T : Any> : Message<T> {

    override lateinit var messageId: String
    public set;
    override lateinit var creationDate: Date
    public set;
    override lateinit var senderId: String
    public set;
    override var recipientId: String? = null;

    override var groupId: String? = null;
    override var order = BigInteger.ONE.negate();
    override var isOrdered: Boolean = false;
    override var command: String? = null;
    override var payload: T? = null;

    constructor() : this(false);

    constructor(isOrdered: Boolean) : super(isOrdered);

    companion object Factory {
        fun <E : Any> of(message: Message<E>): MutableMessage<E> {
            val res = MutableMessage<E>(message.isOrdered);
            return res;
        }
    }
}