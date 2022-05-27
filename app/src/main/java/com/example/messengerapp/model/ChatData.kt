package com.example.messengerapp.model

class ChatData {
    private var sender: String = ""
    private var receiver: String = ""
    private var message: String = ""
    private var messageId: String = ""
    private var isseen = false
    private var url: String = ""

    constructor()
    constructor(
        sender: String,
        receiver: String,
        message: String,
        messageId: String,
        isseen: Boolean,
        url: String
    ) {
        this.sender = sender
        this.receiver = receiver
        this.message = message
        this.messageId = messageId
        this.isseen = isseen
        this.url = url
    }

    fun getSender(): String {
        return sender
    }

    fun getReceiver(): String {
        return receiver
    }

    fun getMessage(): String {
        return message
    }

    fun getIsSeen(): Boolean {
        return isseen
    }

    fun getUrl(): String {
        return url
    }

}