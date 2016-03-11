package by.nalivajr.yana.exceptions

/**
 * Created by Sergey Nalivko
 * Skype: nalivko_sergey
 */
class IllegalSenderException
constructor(val senderId : String, val messageSenderId : String)
: RuntimeException("Sender with ID $senderId could not send message which sender ID is $messageSenderId") {

}