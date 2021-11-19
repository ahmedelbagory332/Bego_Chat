package com.fullChat.app.activities

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.PorterDuff
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.SurfaceView
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.fullChat.app.R
import com.fullChat.app.viewModel.ChatActivityViewModel
import com.fullChat.app.viewModel.viewModelFactory.ChatActivityViewModelFactory
import io.agora.rtc.IRtcEngineEventHandler
import io.agora.rtc.RtcEngine
import io.agora.rtc.video.VideoCanvas
import io.agora.rtc.video.VideoEncoderConfiguration

class VideoCallActivity : AppCompatActivity() {
    private lateinit var timer: CountDownTimer

     lateinit var chatActivityViewModel: ChatActivityViewModel

    private var mRtcEngine: RtcEngine? = null
    private val mRtcEventHandler = object : IRtcEngineEventHandler() {
         override fun onUserJoined(uid: Int, elapsed: Int) {
            runOnUiThread { setupRemoteVideo(uid) }
        }


        override fun onUserOffline(uid: Int, reason: Int) {
            runOnUiThread { onRemoteUserLeft() }
        }


        override fun onUserMuteVideo(uid: Int, muted: Boolean) {
            runOnUiThread { onRemoteUserVideoMuted(uid, muted) }
        }


    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_call)

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)


        val application = requireNotNull(this).application
        val chatActivityViewModelFactory = ChatActivityViewModelFactory(application)
        chatActivityViewModel = ViewModelProvider(this,chatActivityViewModelFactory).get(ChatActivityViewModel::class.java)
        chatActivityViewModel.setUpAgora()
        initAgoraEngineAndJoinChannel()
        chatActivityViewModel.updateCallStatus(true)
        chatActivityViewModel.resetCallCancelStatus()
        chatActivityViewModel.getCallCanceledStatus()
        chatActivityViewModel.setPeeredEmail(intent.getStringExtra("peeredEmail").toString())



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
                    chatActivityViewModel.updateCallCanceledStatus(false)// الكود بتاعها هيمون في BroadcastReceiver
                    viewModelStore.clear()
                    finish()
                }
        })

    }

    private fun initAgoraEngineAndJoinChannel() {
        initializeAgoraEngine()
        setupVideoProfile()
        setupLocalVideo()
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

    private fun setupRemoteVideo(uid: Int) {
        timer.cancel()

        val remoteContainer = findViewById<FrameLayout>(R.id.remote_video_view_container)

        val remoteFrame = RtcEngine.CreateRendererView(baseContext)
         remoteContainer.addView(remoteFrame)
        mRtcEngine!!.setupRemoteVideo(VideoCanvas(remoteFrame, VideoCanvas.RENDER_MODE_HIDDEN, uid))

    }

    private fun initializeAgoraEngine() {
        try {
            mRtcEngine = RtcEngine.create(baseContext, chatActivityViewModel.appId.value, mRtcEventHandler)
        } catch (e: Exception) {
            Log.e("LOG_TAG", Log.getStackTraceString(e))

            throw RuntimeException("NEED TO check rtc sdk init fatal error\n" + Log.getStackTraceString(e))
        }
    }
    private fun setupVideoProfile() {

        mRtcEngine!!.enableVideo()

        mRtcEngine!!.setVideoEncoderConfiguration(VideoEncoderConfiguration(VideoEncoderConfiguration.VD_640x360,
                VideoEncoderConfiguration.FRAME_RATE.FRAME_RATE_FPS_15,
                VideoEncoderConfiguration.STANDARD_BITRATE,
                VideoEncoderConfiguration.ORIENTATION_MODE.ORIENTATION_MODE_FIXED_PORTRAIT))

     }


    private fun setupLocalVideo() {

        val container = findViewById<FrameLayout>(R.id.local_video_view_container)
        val surfaceView = RtcEngine.CreateRendererView(baseContext)
        surfaceView.setZOrderMediaOverlay(true)
        container.addView(surfaceView)

        mRtcEngine!!.setupLocalVideo(VideoCanvas(surfaceView, VideoCanvas.RENDER_MODE_FIT, 0))
    }

    private fun joinChannel() {

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

        val container = findViewById<FrameLayout>(R.id.remote_video_view_container)
        container.removeAllViews()
        Toast.makeText(this,"call end by User", Toast.LENGTH_LONG).show()
        finish()

    }

    private fun onRemoteUserVideoMuted(uid: Int, muted: Boolean) {
        val container = findViewById<FrameLayout>(R.id.remote_video_view_container)

        val surfaceView = container.getChildAt(0) as SurfaceView

        val tag = surfaceView.tag
        if (tag != null && tag as Int == uid) {
            surfaceView.visibility = if (muted) View.GONE else View.VISIBLE
        }
    }
    fun onLocalVideoMuteClicked(view: View) {
        val iv = view as ImageView
        if (iv.isSelected) {
            iv.isSelected = false
            iv.clearColorFilter()
        } else {
            iv.isSelected = true
            iv.setColorFilter(ContextCompat.getColor(applicationContext,R.color.colorPrimary), PorterDuff.Mode.MULTIPLY)
        }

        // Stops/Resumes sending the local video stream.
        mRtcEngine!!.muteLocalVideoStream(iv.isSelected)

        val container = findViewById<FrameLayout>(R.id.local_video_view_container)
        val surfaceView = container.getChildAt(0) as SurfaceView
        surfaceView.setZOrderMediaOverlay(!iv.isSelected)
        surfaceView.visibility = if (iv.isSelected) View.GONE else View.VISIBLE
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
    fun onSwitchCameraClicked(view: View) {
        val iv = view as ImageView
        if (iv.isSelected) {
            iv.isSelected = false
            iv.clearColorFilter()
        } else {
            iv.isSelected = true
            iv.setColorFilter(ContextCompat.getColor(applicationContext,R.color.colorPrimary), PorterDuff.Mode.MULTIPLY)
        }
        // Switches between front and rear cameras.
        mRtcEngine!!.switchCamera()
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