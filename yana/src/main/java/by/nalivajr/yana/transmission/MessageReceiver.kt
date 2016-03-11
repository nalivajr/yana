package by.nalivajr.yana.transmission

import by.nalivajr.yana.models.Message

/**
 * Created by Sergey Nalivko
 * Skype: nalivko_sergey
 */
interface MessageReceiver {

    /**
     * Represents the identifier of this receiver. Can be used for receiving specific messages
     * @return the identifier of this receiver instance.
     */
    fun getReceiverId(): String

    /**
     * Represents the identifier of the group, this receiver intends. Can be used for receiving specific messages
     * @return the identifier of the receiver's group.
     */
    fun getGroupId(): String?

    /**
     * Asynchronous callback, which is invoked, when message received
     * @param message the received message
     */
    fun onReceive(message: Message<String>)

    /**
     * Synchronous method which blocks thread until any message will be received
     * @return received message
     */
    fun waitMessage(invokeOnReceive: Boolean = false): Message<String>

    /**
     * Synchronous method which blocks thread until any message from specific
     * sender will be received
     * @param senderId the id of sender
     * *
     * @param group optional id of group
     * *
     * @return received message
     */
    fun waitMessageFrom(senderId: String, group: String?, invokeOnReceive: Boolean = false): Message<String>
}