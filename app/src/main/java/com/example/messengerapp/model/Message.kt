package com.example.messengerapp.model

data class Message(
    val notification: Notification,
    val to: String
)

data class Notification(
    val title: String,
    val body: String,
    val priority: String,
    val click_action: String,
)