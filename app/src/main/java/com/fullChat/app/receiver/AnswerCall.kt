package com.fullChat.app.receiver

import android.Manifest
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.fullChat.app.activities.VideoCallActivity
import com.fullChat.app.activities.VoiceCallActivity
import com.fullChat.app.utiles.UserData
import com.google.firebase.auth.FirebaseAuth

class AnswerCall : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val notificationManager =  context!!.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val openVideoCallActivity = Intent(context, VideoCallActivity::class.java)
        val openVoiceCallActivity = Intent(context, VoiceCallActivity::class.java)
        val id:Int = intent!!.getIntExtra("id",0)
        val callType:String = intent.getStringExtra("callType").toString()
        val callerName:String = intent.getStringExtra("callerName").toString()
        if ((ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)&& (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED)) {
            if (callType=="video call"){
                openVideoCallActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                notificationManager.cancel(id)

                context.startActivity(openVideoCallActivity)
            }
            else{
                openVoiceCallActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                openVoiceCallActivity.putExtra("callerName", callerName)
                notificationManager.cancel(id)
                context.startActivity(openVoiceCallActivity)
            }
        }else{
            Toast.makeText(context,"Please, enable camera and mic permission to answer the call",Toast.LENGTH_LONG).show()

        }


    }
}