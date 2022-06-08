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

    fun getUid(): String {
        return uid
    }
    fun setUid(uid: String) {
        this.uid = uid
    }

    fun getUsername(): String {
        return username
    }
    fun setUsername(username: String) {
        this.username = username
    }

    fun getProfile(): String {
        return profile
    }
    fun setProfile(profile: String) {
        this.profile = profile
    }

    fun getCover(): String {
        return cover
    }
    fun setCover(cover: String) {
        this.cover = cover
    }

    fun getStatus(): String {
        return status
    }
    fun setStatus(status: String) {
        this.status = status
    }

    fun getSearch(): String {
        return search
    }
    fun setSearch(search: String) {
        this.search = search
    }

    fun getFacebook(): String {
        return facebook
    }
    fun setFacebook(facebook: String) {
        this.facebook = facebook
    }

    fun getAbout(): String {
        return about
    }
    fun setAbout(about: String) {
        this.about = about
    }
}
