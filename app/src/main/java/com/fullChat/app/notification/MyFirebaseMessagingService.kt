package com.fullChat.app.notification

import android.content.Intent
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.fullChat.app.activities.LastMessageActivity
import com.fullChat.app.models.UpdateModel
import com.fullChat.app.netWork.ApiClient
import com.fullChat.app.utiles.UserData
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class MyFirebaseMessagingService : FirebaseMessagingService() {
    private val TAG = "MyFirebaseMsgService"


    override fun onNewToken(s: String) {
        super.onNewToken(s)
        //Displaying token on logcat
        Log.d("MyFirebaseIIDService", "Refreshed token: $s")
        //calling the method store token and passing token
        storeToken(s)
        if (FirebaseAuth.getInstance().currentUser != null) {
            ApiClient().getINSTANCE()
                ?.updateToken(FirebaseAuth.getInstance().currentUser!!.email!!, s)
                ?.enqueue(object : Callback<UpdateModel> {
                    override fun onResponse(
                        call: Call<UpdateModel>,
                        response: Response<UpdateModel>
                    ) {

//                        if (response.body()!!.response == "0") Toast.makeText(
//                            applicationContext,
//                            "updated",
//                            Toast.LENGTH_LONG
//                        ).show()

                    }

                    override fun onFailure(call: Call<UpdateModel>, t: Throwable) {
                        Toast.makeText(applicationContext, t.message.toString(), Toast.LENGTH_LONG)
                            .show()
                    }
                })
        }
    }
    private fun storeToken(token: String) {
        UserData(applicationContext).writeToken(token)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onMessageReceived(p0: RemoteMessage) {
        super.onMessageReceived(p0)
        if (p0.data.isNotEmpty()) {
            Log.e(TAG, "Data Payload: " + p0.data.toString())
            try {
                val json = JSONObject(p0.data.toString())
                sendPushNotification(json)
            } catch (e: Exception) {
                Log.e(TAG, "Exception: " + e.message)
            }
        }
    }



     private fun sendPushNotification(json: JSONObject) {
        //optionally we can display the json into log
        Log.e(TAG, "Notification JSON $json")
        try {
            //getting the json data
            val data = json.getJSONObject("data")

            //parsing json data
            val title = data.getString("title")
            val message = data.getString("message")
            val imageUrl = data.getString("image")

            //creating MyNotificationManager object
            val mNotificationManager =
                MyNotificationManager(
                    applicationContext
                )
            //creating an intent for the notification
            val intent = Intent(applicationContext, LastMessageActivity::class.java)
            intent.putExtra("NotificationMessage", message)
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)




            if (imageUrl == "null" && !title.contains("call")) {

                mNotificationManager.textNotification(title, message, intent)
            }
            else  if (imageUrl == "null" && title.contains("Missed call")) {

                mNotificationManager.missedNotification(title, message, null)
            }
            else if (imageUrl == "null" && title.contains("video call")) {

                mNotificationManager.callNotification(message,title.split("video call")[1],title.split("video call")[0],"video call")
            }
            else if (imageUrl == "null" && title.contains("voice call")) {

                mNotificationManager.callNotification(message,title.split("voice call")[1],title.split("voice call")[0],"voice call")
            }
            else if (imageUrl == "null" && title.contains("missed call")) {

                mNotificationManager.textNotification(message,title,null)
            }
            else {
                //if there is an image
                //displaying a big notification
                mNotificationManager.notificationWithPicture(title, message, imageUrl, intent)
            }
        } catch (e: JSONException) {
            Log.e(TAG, "Json Exception: " + e.message)
        } catch (e: java.lang.Exception) {
            Log.e(TAG, "Exception: " + e.message)
        }
    }

}