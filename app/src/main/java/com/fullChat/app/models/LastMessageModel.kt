package com.fullChat.app.models

import androidx.annotation.Keep
import com.google.firebase.firestore.FieldValue

@Keep
class LastMessageModel  {

    var chatId: String? = null
    var receiverID: String? = null
    var Name: String? = null
    var peeredEmail: String? = null
    var message: String? = null
    var timestamp: Any? = ""
    constructor(chatId: String?, receiverID: String?, Name: String?, peeredEmail: String?, message: String?) {
        this.chatId = chatId
        this.receiverID = receiverID
        this.Name = Name
        this.peeredEmail = peeredEmail
        this.message = message
        this.timestamp = FieldValue.serverTimestamp()

    }




    constructor() {}

}