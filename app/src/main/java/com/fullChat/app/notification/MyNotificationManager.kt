package com.fullChat.app.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.fullChat.app.R
import com.fullChat.app.receiver.AnswerCall
import com.fullChat.app.receiver.CancelCall
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.util.*


class MyNotificationManager(private val mCtx: Context) {



    fun textNotification(title: String?, message: String?, intent: Intent?) {
        val rand = Random()
        val idNotification = rand.nextInt(1000000000)

        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationManager =  mCtx.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                    "Channel_id_default", "Channel_name_default", NotificationManager.IMPORTANCE_HIGH
            )
            val attributes = AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .build()

            notificationChannel.description = "Channel_description_default"
            notificationChannel.enableLights(true)
            notificationChannel.enableVibration(true)
            notificationChannel.setSound(soundUri, attributes)
            notificationManager.createNotificationChannel(notificationChannel)
        }
        val notificationBuilder = NotificationCompat.Builder(mCtx, "Channel_id_default")
        val resultPendingIntent = PendingIntent.getActivity(mCtx, idNotification, intent, PendingIntent.FLAG_UPDATE_CURRENT)


        notificationBuilder.setAutoCancel(true)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.ic_stat_name)
                .setTicker(mCtx.resources.getString(R.string.app_name))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setSound(soundUri)
                .setContentIntent(resultPendingIntent)
                .setContentTitle(title)
                .setContentText(message)
        notificationManager.notify(idNotification, notificationBuilder.build())
    }
    fun missedNotification(title: String?, message: String?, intent: Intent?) {
        // missedNotification and callNotification have same id cause when user didn't answer call missed notification show instead of call notification
        val idNotification = 1998

        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationManager =  mCtx.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                    "missedNotificationID", "missedNotification", NotificationManager.IMPORTANCE_HIGH
            )
            val attributes = AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .build()

            notificationChannel.description = "Missed notification"
            notificationChannel.enableLights(true)
            notificationChannel.enableVibration(true)
            notificationChannel.setSound(soundUri, attributes)
            notificationManager.createNotificationChannel(notificationChannel)
        }
        val notificationBuilder = NotificationCompat.Builder(mCtx, "missedNotificationID")


        notificationBuilder.setAutoCancel(true)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.ic_stat_name)
                .setTicker(mCtx.resources.getString(R.string.app_name))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .setSound(soundUri)
                 .setContentTitle(title)
                .setContentText(message)
        notificationManager.notify(idNotification, notificationBuilder.build())
    }

    fun notificationWithPicture(title: String?, message: String?, url: String, intent: Intent?) {
        val rand = Random()
        val idNotification = rand.nextInt(1000000000)

        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationManager =  mCtx.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                    "Channel_id_pictureStyle", "Channel_name_pictureStyle", NotificationManager.IMPORTANCE_HIGH
            )
            val attributes = AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .build()
            notificationChannel.description = "Channel_description_pictureStyle"
            notificationChannel.enableLights(true)
            notificationChannel.enableVibration(true)
            notificationChannel.setSound(soundUri, attributes)
            notificationManager.createNotificationChannel(notificationChannel)
        }
        val notificationBuilder = NotificationCompat.Builder(mCtx, "Channel_id_pictureStyle")
        val resultPendingIntent = PendingIntent.getActivity(mCtx, idNotification, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        val picture  = getBitmapFromURL(url)

        notificationBuilder
                .setAutoCancel(true)
                .setContentTitle(title)
                .setContentText(message)
                .setLargeIcon(picture)
                .setStyle(NotificationCompat.BigPictureStyle()
                        .bigPicture(picture)
                        .bigLargeIcon(null)) // .bigLargeIcon(null) to hide pic when expand notification and all pic show
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.ic_stat_name)
                .setTicker(mCtx.resources.getString(R.string.app_name))
                .setContentIntent(resultPendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .setSound(soundUri)

        notificationManager.notify(idNotification, notificationBuilder.build())



    }
    //The method will return Bitmap from an image URL
    private fun getBitmapFromURL(strURL: String): Bitmap? {
        return try {
            val url = URL(strURL)
            val connection =
                    url.openConnection() as HttpURLConnection
            connection.doInput = true
            connection.connect()
            val input = connection.inputStream
            BitmapFactory.decodeStream(input)
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    fun callNotification(message: String?, callerID: String?, callerName: String?, callType: String) {

        // missedNotification and callNotification have same id cause when user didn't answer call missed notification show instead of call notification
        val idNotification = 1998

        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationManager =  mCtx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                    callType+"_callNotification", callType, NotificationManager.IMPORTANCE_HIGH
            )
            val attributes = AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .build()
            notificationChannel.description = callType+"_description_callNotification"
            notificationChannel.enableLights(true)
            notificationChannel.enableVibration(true)
            notificationChannel.setSound(soundUri, attributes)
            notificationManager.createNotificationChannel(notificationChannel)
        }
        val notificationBuilder = NotificationCompat.Builder(mCtx   , callType+"_callNotification")
        val callingView = RemoteViews(mCtx.packageName, R.layout.call_notification)
        callingView.setTextViewText(R.id.tv_user_name,message)


        val answerIntent: Intent = Intent(mCtx, AnswerCall::class.java)
        answerIntent.putExtra("id", idNotification)
        answerIntent.putExtra("callType", callType)
        answerIntent.putExtra("callerName", callerName)
        val answerPendingIntent : PendingIntent = PendingIntent.getBroadcast(
            mCtx,
            0,
            answerIntent,
            PendingIntent.FLAG_UPDATE_CURRENT)
        //
        val cancelIntent: Intent = Intent(mCtx, CancelCall::class.java)
        cancelIntent.putExtra("id", idNotification)
        cancelIntent.putExtra("callerID", callerID)
        val cancelPendingIntent : PendingIntent = PendingIntent.getBroadcast(
            mCtx,
            1,
            cancelIntent,
            PendingIntent.FLAG_UPDATE_CURRENT)
        // for button action later
        callingView.setOnClickPendingIntent(R.id.answer_call,answerPendingIntent)
        callingView.setOnClickPendingIntent(R.id.end_call,cancelPendingIntent)


        notificationBuilder
                .setOngoing(true)
                .setStyle(NotificationCompat.DecoratedCustomViewStyle())
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.ic_stat_call)
                .setTicker(mCtx.resources.getString(R.string.app_name))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_CALL)
                .setSound(soundUri)
                .setCustomContentView(callingView)

        notificationManager.notify(idNotification, notificationBuilder.build())
    }


    private fun createUploadMediaNotification() {
        val notificationManager = mCtx.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                "Channel_id_progress", "Channel_name_progress", NotificationManager.IMPORTANCE_LOW
            )


            notificationChannel.description = "Channel_description_progress"
            notificationManager.createNotificationChannel(notificationChannel)
        }
        val notificationBuilder = NotificationCompat.Builder(mCtx, "Channel_id_progress")



    }

}