package com.fullChat.app.models

import com.google.firebase.firestore.FieldValue


class UsersModel {
    var name: String? = null
    var email: String? = null
    var userId: String? = null
    var type: String? = null
    var callStatus: Boolean? = null
    var callCanceled: Boolean? = null
    var lastSeen: Any? = ""

    constructor(name: String?, email: String?, userId: String?, type: String?, callStatus: Boolean,callCanceled: Boolean?) {
        this.name = name
        this.email = email
        this.userId = userId
        this.type = type
        this.callStatus = callStatus
        this.callCanceled = callCanceled
        this.lastSeen = FieldValue.serverTimestamp()
    }

    constructor() {}

}