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
    fun setSender(sender: String?) {
        this.sender = sender!!
    }

    fun getReceiver(): String {
        return receiver
    }
    fun setReceiver(receiver: String?) {
        this.receiver = receiver!!
    }

    fun getMessage(): String {
        return message
    }
    fun setMessage(message: String?) {
        this.message = message!!
    }

    fun getMessageId(): String {
        return messageId
    }
    fun setMessageId(messageId: String?) {
        this.messageId = messageId!!
    }

    fun getIsSeen(): Boolean {
        return isseen
    }
    fun setIsSeen(isseen: Boolean?) {
        this.isseen = isseen!!
    }

    fun getUrl(): String {
        return url
    }

    fun setUrl(url: String?) {
        this.url = url!!
    }

}