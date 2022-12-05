package com.example.cmpt_362_chitchat.data

class Message() {
    var message: String? = null
    var sendID: String? = null
    var imageName: String? = ""

    constructor(message: String?, sendID: String?) : this() {
        this.message = message
        this.sendID = sendID
    }

    constructor(message: String?, sendID: String?, imageName: String?) : this() {
        this.message = message
        this.sendID = sendID
        this.imageName = imageName
    }
}