package com.example.messengerapp.model

class UserData {
    private var uid: String = ""
    private var username: String = ""
    private var profile: String = ""
    private var cover: String = ""
    private var status: String = ""
    private var search: String = ""
    private var facebook: String = ""
    private var about: String = ""

    constructor()
    constructor(
        uid: String,
        username: String,
        profile: String,
        cover: String,
        status: String,
        search: String,
        facebook: String,
        about: String,
    ) {
        this.uid = uid
        this.username = username
        this.profile = profile
        this.cover = cover
        this.status = status
        this.search = search
        this.facebook = facebook
        this.about = about
    }
    // uid
    fun getUid(): String {
        return uid
    }
    // username
    fun getUsername(): String {
        return username
    }
    // profile
    fun getProfile(): String {
        return profile
    }
    // cover
    fun getCover(): String {
        return cover
    }
    // status
    fun getStatus(): String {
        return status
    }

    // facebook
    fun getFacebook(): String {
        return facebook
    }
    // about
    fun getAbout(): String {
        return about
    }
}