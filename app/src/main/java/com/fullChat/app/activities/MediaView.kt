package com.fullChat.app.activities

import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.widget.MediaController
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.fullChat.app.R
import com.github.chrisbanes.photoview.PhotoView


class MediaView : AppCompatActivity() {
    lateinit var  videoView: VideoView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_images_view)
        val mediaController: MediaController = MediaController(this)
        val photoView = findViewById<PhotoView>(R.id.photo_view)
        videoView = findViewById(R.id.video_view)
        photoView.visibility = View.GONE
        videoView.visibility = View.GONE

        val imageLink:String? = intent.getStringExtra("image").toString()
        val videoLink:String? = intent.getStringExtra("video").toString()

        if (intent.hasExtra("image")){
            photoView.visibility = View.VISIBLE
            Glide.with(applicationContext).load(imageLink).diskCacheStrategy(DiskCacheStrategy.DATA).into(photoView)
        }
        else{
            videoView.visibility = View.VISIBLE
            videoView.setMediaController(mediaController)
            mediaController.setAnchorView(videoView)
            videoView.setVideoURI(Uri.parse(videoLink))
            videoView.seekTo(1)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            mediaController.addOnUnhandledKeyEventListener { _: View?, event: KeyEvent ->
                //Handle BACK button
                if (event.keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP) {
                    finish()
                }
                true
            }
        }
    }

    override fun dispatchKeyEvent(event: KeyEvent?): Boolean {
        if (event!!.keyCode == KeyEvent.KEYCODE_BACK) {
            finish()
        }
        return super.dispatchKeyEvent(event)
    }

    override fun onResume() {
        super.onResume()
        videoView.seekTo( 1)


    }
}