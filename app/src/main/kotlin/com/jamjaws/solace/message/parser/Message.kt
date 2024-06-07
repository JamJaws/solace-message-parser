package com.jamjaws.solace.message.parser

data class Message(
    val jmsDestination: String = "",
    val jmsMessageID: String = "",
    val jmsTimestamp: String = "",
    val jmsType: String = "",
    val json: String = "",
    val validJson: Boolean = false,
    val exception: Exception? = null,
) {
    val destination get() = jmsDestination.substringAfter("'", "").substringBeforeLast("'")
}
