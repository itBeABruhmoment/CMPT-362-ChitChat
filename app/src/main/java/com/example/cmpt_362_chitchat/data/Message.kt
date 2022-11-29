package com.example.cmpt_362_chitchat.data

class Message() {
    var message: String? = null
    var sendID: String? = null
    var username: String? = null

    constructor(message: String?, username: String?, sendID: String?) : this() {
        this.message = message
        this.username = username
        this.sendID = sendID
    }
}