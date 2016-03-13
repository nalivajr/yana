package by.nalivajr.yana.tools

import by.nalivajr.yana.models.Message
import by.nalivajr.yana.models.MutableMessage
import java.math.BigInteger
import java.util.*

/**
 * Created by Sergey Nalivko
 * Skype: nalivko_sergey
 */
class MessageBuilder<T : Any> {
    private var senderId: String? = null

    private var recipientId: String? = null
    private var groupId: String? = null;

    private var order: BigInteger = BigInteger.ONE

    private var command: String? = null
    private var payload: T? = null

    companion object Creator {
        fun <T : Any> obtainMessageBuilderWithPayload(payload: T): MessageBuilder<T> {
            val builder = MessageBuilder<T>()
            builder.payload = payload
            return builder
        }

        fun <T : Any> obtainMessageBuilder(command: String): MessageBuilder<T> {
            val builder = MessageBuilder<T>()
            builder.command = command
            return builder
        }
    }

    fun recipient(recipientId: String?): MessageBuilder<T> {
        this.recipientId = recipientId
        return this
    }

    fun sender(senderId: String): MessageBuilder<T> {
        this.senderId = senderId
        return this
    }

    fun order(order: Int): MessageBuilder<T> {
        return order(order.toLong())
    }

    fun order(order: Long): MessageBuilder<T> {
        this.order = BigInteger.valueOf(order)
        return this
    }

    fun command(command: String): MessageBuilder<T> {
        this.command = command
        return this
    }

    fun group(groupId: String): MessageBuilder<T> {
        this.groupId = groupId
        return this
    }

    fun payload(payload: T): MessageBuilder<T> {
        this.payload = payload
        return this
    }

    fun build(): Message<T> {
        val msg = MutableMessage<T>(false)
        msg.creationDate = Date()
        msg.order = order
        msg.payload = payload
        if (senderId != null) {
            msg.senderId = senderId as String;
        }
        msg.recipientId = recipientId
        msg.command = command
        msg.groupId = groupId
        return msg
    }
}