package com.example.cmpt_362_chitchat.data.model

import java.util.*

class FriendRequest {
    var id: String = ""
    var time: Long = 0
    var sender: String = ""
    var recipient: String = ""
    var friendRequestStatus: FriendRequestStatus = FriendRequestStatus.PENDING

    constructor() {}

    constructor(id: String, time: Long, sender: String, recipient: String, status: FriendRequestStatus) {
        this.id = id
        this.time = time
        this.sender = sender
        this.recipient = recipient
        this.friendRequestStatus = status
    }

    constructor(id: String, sender: String, recipient: String) {
        this.id = id
        this.time = Date().time
        this.sender = sender
        this.recipient = recipient
        this.friendRequestStatus = FriendRequestStatus.PENDING
    }

    companion object {
        enum class FriendRequestStatus {
            PENDING,
            CANCELED_BY_SENDER,
            REJECTED_BY_RECIPIENT,
            ACCEPTED_BY_RECIPIENT
        }
    }

}