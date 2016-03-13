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

    override var messageId: BigInteger
    get() = super.messageId
    public set(value) { super.messageId = value }

    override var creationDate: Date
    get() = super.creationDate
    public set(value) { super.creationDate = value }

    override var senderId: String
    get() = super.senderId
    public set(value) { super.senderId = value }

    override var recipientId: String?
    get() = super.recipientId
    public set(value) { super.recipientId = value}

    override var groupId: String?
    get() = super.groupId
    public set (value) { super.groupId = value}


    override var order : BigInteger?
    get() = super.order
    public set(value) { super.order = value }

    override var command: String?
    get() = super.command
    public set(value) { super.command = value }

    override var payload: T?
    get() = super.payload
    public set(value) { super.payload = value}

    constructor() : this(false);

    constructor(isOrdered: Boolean) : super(isOrdered) {
        this.order = BigInteger.ONE.negate();
    }
}