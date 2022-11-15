package com.example.cmpt_362_chitchat.data

class Message() {

    var message: String? = null
    var sendID: String? = null

    constructor(message: String?, sendID: String?) : this() {
        this.message = message
        this.sendID = sendID
    }

}