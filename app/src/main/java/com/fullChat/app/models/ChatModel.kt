package com.fullChat.app.models

import androidx.annotation.Keep
import com.google.firebase.firestore.FieldValue

@Keep
class ChatModel  {

    var from: String? = null
    var to: String? = null
    var sender: String? = null
    var message: String? = null
    var file: String? = null
    var type: String? = null
    var timestamp: Any? = ""


    constructor(from: String?,to: String?, sender: String?, message: String?, file: String?, type: String?) {
        this.from = from
        this.to = to
        this.sender = sender
        this.message = message
        this.file = file
        this.type = type
        this.timestamp = FieldValue.serverTimestamp()

    }

    constructor() {}



}