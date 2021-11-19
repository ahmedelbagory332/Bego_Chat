package com.fullChat.app.activities

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.PorterDuff
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.fullChat.app.R
import com.fullChat.app.utiles.UserData
import com.fullChat.app.viewModel.ChatActivityViewModel
import com.fullChat.app.viewModel.viewModelFactory.ChatActivityViewModelFactory
import io.agora.rtc.Constants
import io.agora.rtc.IRtcEngineEventHandler
import io.agora.rtc.RtcEngine


class VoiceCallActivity : AppCompatActivity() {
    private lateinit var timer: CountDownTimer

   lateinit var chatActivityViewModel: ChatActivityViewModel
    lateinit var remoteUserName: TextView

    private var mRtcEngine: RtcEngine? = null
    private val mRtcEventHandler = object : IRtcEngineEventHandler() {

        override fun onUserJoined(uid: Int, elapsed: Int) {
            runOnUiThread { onRemoteUserJoined(uid) }
        }


        override fun onUserOffline(uid: Int, reason: Int) {
            runOnUiThread { onRemoteUserLeft() }
        }


    }



    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_voice_call)

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)


        val application = requireNotNull(this).application
        val chatActivityViewModelFactory = ChatActivityViewModelFactory(application)

        remoteUserName = findViewById(R.id.remote_user_name)
        chatActivityViewModel = ViewModelProvider(this,chatActivityViewModelFactory).get(ChatActivityViewModel::class.java)
        chatActivityViewModel.setUpAgora()
        initAgoraEngineAndJoinChannel()
        chatActivityViewModel.updateCallStatus(true)
        chatActivityViewModel.resetCallCancelStatus()
        chatActivityViewModel.getCallCanceledStatus()
        chatActivityViewModel.setPeeredEmail(intent.getStringExtra("peeredEmail").toString())

        remoteUserName.text = "calling with "+intent.getStringExtra("callerName").toString()

        timer = object: CountDownTimer(20000, 1000) {
            override fun onTick(millisUntilFinished: Long) {}
            override fun onFinish() {
                Toast.makeText(applicationContext,"user did not answer", Toast.LENGTH_LONG).show()
                // send miss notification
                chatActivityViewModel.sendMissedCallNotification()
                finish()
            }
        }
          timer.start()

        chatActivityViewModel.callCanceledStatus.observe(this, Observer {
                if (it) {
                    Toast.makeText(applicationContext, "user cancel call", Toast.LENGTH_LONG).show()
                    chatActivityViewModel.updateCallCanceledStatus(false)// الكود بتاعها هيكون في BroadcastReceiver
                    viewModelStore.clear()
                    finish()
                }
        })





    }

    private fun initAgoraEngineAndJoinChannel() {
        initializeAgoraEngine()
        joinChannel()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        viewModelStore.clear()

        leaveChannel()

        RtcEngine.destroy()
        mRtcEngine = null
        chatActivityViewModel.updateCallStatus(false)
        Toast.makeText(this,"call end by you", Toast.LENGTH_LONG).show()
     }



    private fun initializeAgoraEngine() {
        try {
            mRtcEngine = RtcEngine.create(baseContext, chatActivityViewModel.appId.value, mRtcEventHandler)
        } catch (e: Exception) {
            Log.e("LOG_TAG", Log.getStackTraceString(e))

            throw RuntimeException("NEED TO check rtc sdk init fatal error\n" + Log.getStackTraceString(e))
        }
    }



    private fun onRemoteUserJoined(uid: Int) {
        timer.cancel()

    }

    private fun joinChannel() {
        // Sets the channel profile of the Agora RtcEngine.
        // CHANNEL_PROFILE_COMMUNICATION(0): (Default) The Communication profile. Use this profile in one-on-one calls or group calls, where all users can talk freely.

        mRtcEngine!!.setChannelProfile(Constants.CHANNEL_PROFILE_COMMUNICATION);

        mRtcEngine!!.joinChannel(chatActivityViewModel.token.value, chatActivityViewModel.channelName.value, "", 0)
    }
    private fun leaveChannel() {
        if (mRtcEngine == null)
        {
             try {
            mRtcEngine = RtcEngine.create(baseContext, chatActivityViewModel.appId.value, mRtcEventHandler)
        } catch (e: Exception) {
            Log.e("LOG_TAG", Log.getStackTraceString(e))

            throw RuntimeException("NEED TO check rtc sdk init fatal error\n" + Log.getStackTraceString(e))
        }
        }
        mRtcEngine!!.leaveChannel()
        chatActivityViewModel.updateCallStatus(false)

    }
    private fun onRemoteUserLeft() {
        viewModelStore.clear()

        Toast.makeText(this,"call end by User", Toast.LENGTH_LONG).show()
        finish()

    }

    fun onSwitchSpeakerphoneClicked(view: View) {
        val iv = view as ImageView
        if (iv.isSelected) {
            iv.isSelected = false
            iv.clearColorFilter()
        } else {
            iv.isSelected = true
            iv.setColorFilter(ContextCompat.getColor(applicationContext,R.color.colorPrimary), PorterDuff.Mode.MULTIPLY)
        }

        // Enables/Disables the audio playback route to the speakerphone.
        //
        // This method sets whether the audio is routed to the speakerphone or earpiece. After calling this method, the SDK returns the onAudioRouteChanged callback to indicate the changes.
        mRtcEngine!!.setEnableSpeakerphone(view.isSelected())
    }
    fun onLocalAudioMuteClicked(view: View) {
        val iv = view as ImageView
        if (iv.isSelected) {
            iv.isSelected = false
            iv.clearColorFilter()
        } else {
            iv.isSelected = true
            iv.setColorFilter(ContextCompat.getColor(applicationContext,R.color.colorPrimary), PorterDuff.Mode.MULTIPLY)
        }

        // Stops/Resumes sending the local audio stream.
        mRtcEngine!!.muteLocalAudioStream(iv.isSelected)
    }
    fun onEncCallClicked(view: View) {
        viewModelStore.clear()

        finish()
        mRtcEngine?.leaveChannel()
        RtcEngine.destroy()
        chatActivityViewModel.updateCallStatus(false)
        Toast.makeText(this,"call end by you", Toast.LENGTH_LONG).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModelStore.clear()

        leaveChannel()
        timer.cancel()

        RtcEngine.destroy()
        mRtcEngine = null
        chatActivityViewModel.updateCallStatus(false)

    }

    override fun onPause() {
        super.onPause()

        viewModelStore.clear()

        leaveChannel()
        timer.cancel()

        RtcEngine.destroy()
        mRtcEngine = null
        chatActivityViewModel.updateCallStatus(false)
         finish()
    }


}