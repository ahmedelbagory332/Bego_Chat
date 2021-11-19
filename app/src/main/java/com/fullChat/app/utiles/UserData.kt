package com.fullChat.app.utiles

import android.content.Context
import android.content.SharedPreferences

class UserData(context: Context) {
    private val sharedPreferences: SharedPreferences =
            context.getSharedPreferences("UserToken", Context.MODE_PRIVATE)

    fun  writeToken(token: String?) {
        val editor = sharedPreferences.edit()
        editor.putString("Token", token)
        editor.apply()
    }

    fun readToken(): String? {
        return sharedPreferences.getString("Token", "123456")
    }


}