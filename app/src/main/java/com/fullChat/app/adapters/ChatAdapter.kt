package com.fullChat.app.adapters


import android.Manifest
import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.net.Uri
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.fullChat.app.R
import com.fullChat.app.models.ChatModel
import com.google.common.reflect.Reflection.getPackageName
import com.google.firebase.auth.FirebaseAuth
import java.io.File
import java.text.SimpleDateFormat
import java.util.*


@SuppressLint("SimpleDateFormat")
class ChatAdapter(
    val context: Context,
    options: FirestoreRecyclerOptions<ChatModel>,
    val msgClick: MessageClickListener,
    val btnClickPlayVideo: PlayVideoButtonClickListener,
) : FirestoreRecyclerAdapter<ChatModel, ChatAdapter.ViewHolder>(options) {

    private lateinit var user:FirebaseAuth
    private var currentPos:Double = 0.0
    private var isRecordClicked = true
    private var isPaused = false
    private var length = 0
    private var currentPosition = -1



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val view:View= LayoutInflater.from(parent.context).inflate(
            R.layout.message_row,
            parent,
            false
        )
        user = FirebaseAuth.getInstance()

        return ViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(messageViewHolder: ViewHolder, position: Int, messages: ChatModel) {

        val messageSenderId: String = user.currentUser!!.uid
        val fromUserID: String? = messages.from
        val fromMessageType: String? = messages.type
        val root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).toString()
        val mediaPlayer:MediaPlayer = MediaPlayer()


        messageViewHolder.linearLayoutSenderPicture.visibility = View.GONE
        messageViewHolder.linearLayoutSenderVideo.visibility = View.GONE
        messageViewHolder.linearLayoutSenderMessageText.visibility = View.GONE
        messageViewHolder.linearLayoutSenderFile.visibility = View.GONE
        messageViewHolder.linearLayoutSenderRecord.visibility = View.GONE

        messageViewHolder.linearLayoutReceiverPicture.visibility = View.GONE
        messageViewHolder.linearLayoutReceiverVideo.visibility = View.GONE
        messageViewHolder.linearLayoutReceiverMessageText.visibility = View.GONE
        messageViewHolder.linearLayoutReceiverFile.visibility = View.GONE
        messageViewHolder.linearLayoutReceiverRecord.visibility = View.GONE

        messageViewHolder.itemView.setOnClickListener {
            // in case user send file the message will contain the file name
            msgClick.messageClick(
                messages.type.toString(),
                messages.file.toString(),
                messages.message!!
            )
        }
        messageViewHolder.playVideoReceiver.setOnClickListener {
            btnClickPlayVideo.playVideoButtonClick(messages.file.toString())
        }
        messageViewHolder.playVideoSender.setOnClickListener {
            btnClickPlayVideo.playVideoButtonClick(messages.file.toString())
        }

        messageViewHolder.playRecordSender.setOnClickListener {

            if ((ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)&& (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)){
                val root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).toString()
                val myDir = File("$root/Bego Chat/Bego Recorders")
                if (!myDir.exists())
                    myDir.mkdirs()
                val list = myDir.listFiles()
                if (list.contains(File("$root/Bego Chat/Bego Recorders/${getItem(position).message}")))
                {

                    //file already downloaded
                    playRecordSender(
                        "$root/Bego Chat/Bego Recorders/${getItem(position).message}",
                        messageViewHolder,
                        mediaPlayer,
                        position
                    )

                }
                else{
                    // Create request for android download manager
                    val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                    val request = DownloadManager.Request(Uri.parse(getItem(position).file))
                    request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)

                    // set title
                    request.setTitle("Downloading...")

                    //set the local destination for download file to a path within the application's external files directory
                    request.setDestinationUri(Uri.fromFile(File(myDir, getItem(position).message)))
                    request.setMimeType("*/*")
                    downloadManager.enqueue(request)
                    playRecordSender(
                        "${getItem(position).file}",
                        messageViewHolder,
                        mediaPlayer,
                        position
                    )


                }
            }
            else{

                AlertDialog.Builder(it.context)
                    .setTitle("Storage permission needed")
                    .setMessage("For better playing records give application permission to play records directly from phone not from server")
                    .setPositiveButton("OK") { _, _ ->
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        val uri = Uri.fromParts("package", context.packageName, null)
                        intent.data = uri
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(intent)
                    }
                    .setNegativeButton("No thanks") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .create()
                    .show()
                playRecordSender("${getItem(position).file}", messageViewHolder, mediaPlayer, position)
            }




        }


        messageViewHolder.playRecordReceiver.setOnClickListener {
            if ((ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)&& (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)) {
                val root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).toString()
                val myDir = File("$root/Bego Chat/Bego Recorders")
                if (!myDir.exists())
                    myDir.mkdirs()
                val list = myDir.listFiles()
                if (list.contains(File("$root/Bego Chat/Bego Recorders/${getItem(position).message}")))
                {

                    //file already downloaded
                    playRecordReceiver(
                        "$root/Bego Chat/Bego Recorders/${getItem(position).message}",
                        messageViewHolder,
                        mediaPlayer,
                        position
                    )

                }
                else{
                    // Create request for android download manager
                    val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                    val request = DownloadManager.Request(Uri.parse(getItem(position).file))
                    request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)

                    // set title
                    request.setTitle("Downloading...")
                    //set the local destination for download file to a path within the application's external files directory
                    request.setDestinationUri(Uri.fromFile(File(myDir, getItem(position).message)))
                    request.setMimeType("*/*")
                    downloadManager.enqueue(request)
                    playRecordReceiver(
                        "${getItem(position).file}",
                        messageViewHolder,
                        mediaPlayer,
                        position
                    )


                }
            }else{

                AlertDialog.Builder(it.context)
                    .setTitle("Storage permission needed")
                    .setMessage("For better playing records give application permission to play records directly from phone not from server")
                    .setPositiveButton("OK") { _, _ ->
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        val uri = Uri.fromParts("package", context.packageName, null)
                        intent.data = uri
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(intent)
                    }
                    .setNegativeButton("No thanks") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .create()
                    .show()
                playRecordReceiver("${getItem(position).file}", messageViewHolder, mediaPlayer, position)
            }



        }



        if (fromMessageType.equals("text")) {
                    if (fromUserID.equals(messageSenderId)) {
                        messageViewHolder.linearLayoutSenderMessageText.visibility = View.VISIBLE
                        if (messages.timestamp!=null) {
                            val timestamp = messages.timestamp as com.google.firebase.Timestamp
                            val milliseconds = timestamp.seconds * 1000 + timestamp.nanoseconds / 1000000
                            val sdf = SimpleDateFormat("dd MMM yyyy hh:mm a")
                            val netDate = Date(milliseconds)
                            val date = sdf.format(netDate).toString()
                            messageViewHolder.senderMessageText.text = messages.message
                            messageViewHolder.senderMessageTime.text = date

                        }
                    }
                    else {
                         messageViewHolder.linearLayoutReceiverMessageText.visibility = View.VISIBLE
                        if (messages.timestamp!=null) {
                            val timestamp = messages.timestamp as com.google.firebase.Timestamp
                            val milliseconds = timestamp.seconds * 1000 + timestamp.nanoseconds / 1000000
                            val sdf = SimpleDateFormat("dd MMM yyyy hh:mm a")
                            val netDate = Date(milliseconds)
                            val date = sdf.format(netDate).toString()
                            messageViewHolder.receiverMessageText.text = messages.message
                            messageViewHolder.receiverMessageTime.text = date

                        }
                    }
        }
        else if (fromMessageType.equals("image")||fromMessageType.equals("gif")) {
                    if (fromUserID.equals(messageSenderId)) {
                        messageViewHolder.linearLayoutSenderPicture.visibility = View.VISIBLE
                        Glide.with(context).load(messages.file).diskCacheStrategy(DiskCacheStrategy.DATA).into(
                            messageViewHolder.messageSenderPicture
                        )

                        if (messages.timestamp!=null) {
                            val timestamp = messages.timestamp as com.google.firebase.Timestamp
                            val milliseconds = timestamp.seconds * 1000 + timestamp.nanoseconds / 1000000
                            val sdf = SimpleDateFormat("dd MMM yyyy hh:mm a")
                            val netDate = Date(milliseconds)
                            val date = sdf.format(netDate).toString()
                            messageViewHolder.messageSenderPictureTime.text =  date

                        }
                    }
                    else {
                        messageViewHolder.linearLayoutReceiverPicture.visibility = View.VISIBLE
                        Glide.with(context).load(messages.file).diskCacheStrategy(DiskCacheStrategy.DATA).into(
                            messageViewHolder.messageReceiverPicture
                        )

                        if (messages.timestamp!=null) {
                            val timestamp = messages.timestamp as com.google.firebase.Timestamp
                            val milliseconds = timestamp.seconds * 1000 + timestamp.nanoseconds / 1000000
                            val sdf = SimpleDateFormat("dd MMM yyyy hh:mm a")
                            val netDate = Date(milliseconds)
                            val date = sdf.format(netDate).toString()
                            messageViewHolder.messageReceiverPictureTime.text =  date

                        }
                    }
        }
        else if (fromMessageType.equals("video")) {
            if (fromUserID.equals(messageSenderId)) {
                messageViewHolder.linearLayoutSenderVideo.visibility = View.VISIBLE

                messageViewHolder.videoSender.setVideoURI(Uri.parse(messages.file))
                messageViewHolder.videoSender.seekTo(1)
                if (messages.timestamp!=null) {
                    val timestamp = messages.timestamp as com.google.firebase.Timestamp
                    val milliseconds = timestamp.seconds * 1000 + timestamp.nanoseconds / 1000000
                    val sdf = SimpleDateFormat("dd MMM yyyy hh:mm a")
                    val netDate = Date(milliseconds)
                    val date = sdf.format(netDate).toString()
                    messageViewHolder.videoSenderTime.text =  date

                }
            }
            else {
                messageViewHolder.linearLayoutReceiverVideo.visibility = View.VISIBLE

                 messageViewHolder.videoReceiver.setVideoURI(Uri.parse(messages.file))
                messageViewHolder.videoReceiver.seekTo(1)
                if (messages.timestamp!=null) {
                    val timestamp = messages.timestamp as com.google.firebase.Timestamp
                    val milliseconds = timestamp.seconds * 1000 + timestamp.nanoseconds / 1000000
                    val sdf = SimpleDateFormat("dd MMM yyyy hh:mm a")
                    val netDate = Date(milliseconds)
                    val date = sdf.format(netDate).toString()
                    messageViewHolder.videoReceiverTime.text =  date

                }
            }
        }
        else if (fromMessageType.equals("document")||fromMessageType.equals("pdf")||fromMessageType.equals(
                "audio"
            )) {
            if (fromUserID.equals(messageSenderId)) {
                messageViewHolder.linearLayoutSenderFile.visibility = View.VISIBLE
                if (messages.timestamp!=null) {
                    val timestamp = messages.timestamp as com.google.firebase.Timestamp
                    val milliseconds = timestamp.seconds * 1000 + timestamp.nanoseconds / 1000000
                    val sdf = SimpleDateFormat("dd MMM yyyy hh:mm a")
                    val netDate = Date(milliseconds)
                    val date = sdf.format(netDate).toString()
                    messageViewHolder.fileSenderTime.text =  date
                }
                messageViewHolder.senderFileName.text =  messages.message

            }
            else {
                messageViewHolder.linearLayoutReceiverFile.visibility = View.VISIBLE
                if (messages.timestamp!=null) {
                    val timestamp = messages.timestamp as com.google.firebase.Timestamp
                    val milliseconds = timestamp.seconds * 1000 + timestamp.nanoseconds / 1000000
                    val sdf = SimpleDateFormat("dd MMM yyyy hh:mm a")
                    val netDate = Date(milliseconds)
                    val date = sdf.format(netDate).toString()
                    messageViewHolder.fileReceiverTime.text =  date
                }
                messageViewHolder.receiverFileName.text =  messages.message

            }
        }
        else if (fromMessageType.equals("record")) {
            if (fromUserID.equals(messageSenderId)) {

                 messageViewHolder.linearLayoutSenderRecord.visibility = View.VISIBLE
                messageViewHolder.senderTotal.text = getAudioFileLength(
                    "$root/Bego Chat/Bego Recorders/${messages.message}",
                    true
                )
                if (messages.timestamp!=null) {
                    val timestamp = messages.timestamp as com.google.firebase.Timestamp
                    val milliseconds = timestamp.seconds * 1000 + timestamp.nanoseconds / 1000000
                    val sdf = SimpleDateFormat("dd MMM yyyy hh:mm a")
                    val netDate = Date(milliseconds)
                    val date = sdf.format(netDate).toString()
                    messageViewHolder.recordSenderTime.text = date

                }
             }
            else {
                messageViewHolder.linearLayoutReceiverRecord.visibility = View.VISIBLE
                messageViewHolder.receiverTotal.text = getAudioFileLength(
                    "$root/Bego Chat/Bego Recorders/${messages.message}",
                    true
                )
                if (messages.timestamp!=null) {
                    val timestamp = messages.timestamp as com.google.firebase.Timestamp
                    val milliseconds = timestamp.seconds * 1000 + timestamp.nanoseconds / 1000000
                    val sdf = SimpleDateFormat("dd MMM yyyy hh:mm a")
                    val netDate = Date(milliseconds)
                    val date = sdf.format(netDate).toString()
                    messageViewHolder.recordReceiverTime.text = date

                }
            }
        }




    }

    private fun playRecordSender(
        file: String,
        messageViewHolder: ViewHolder,
        mediaPlayer: MediaPlayer,
        position: Int
    ) {


        if (isRecordClicked) {

            if (isPaused) {
                if (currentPosition == position)
                // this mean we clicked on same record
                    mediaPlayer.seekTo(length) // in case we paused record and resumed it again
                else {

                    messageViewHolder.senderSeekBar.progress = 0
                    mediaPlayer.reset()
                    mediaPlayer.setDataSource(file)
                    mediaPlayer.prepare()
                }

            } else {

                mediaPlayer.setDataSource(file)
                mediaPlayer.prepare()
            }

            messageViewHolder.playRecordSender.setImageResource(R.drawable.ic_baseline_pause_circle_outline_24)
            mediaPlayer.setOnCompletionListener {
                messageViewHolder.playRecordSender.setImageResource(R.drawable.play)
                it.reset()
                isPaused = false
                isRecordClicked = true
            }
            messageViewHolder.senderSeekBar.setOnSeekBarChangeListener(object :
                OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {

                }

                override fun onStartTrackingTouch(seekBar: SeekBar) {}
                override fun onStopTrackingTouch(seekBar: SeekBar) {
                    currentPos = seekBar.progress.toDouble()
                    mediaPlayer.seekTo(currentPos.toInt())
                }
            })
            currentPos = mediaPlayer.currentPosition.toDouble()
            messageViewHolder.senderCurrent.text = timerConversion(currentPos.toLong())
            messageViewHolder.senderSeekBar.max = mediaPlayer.duration
            val handler = Handler(Looper.getMainLooper())

            val runnable: Runnable = object : Runnable {
                override fun run() {
                    try {
                        currentPos = mediaPlayer.currentPosition.toDouble()
                        messageViewHolder.senderCurrent.text = timerConversion(currentPos.toLong())
                        messageViewHolder.senderSeekBar.progress = currentPos.toInt()
                        handler.postDelayed(this, 500)
                    } catch (ed: IllegalStateException) {
                        ed.printStackTrace()
                    }
                }
            }
            handler.postDelayed(runnable, 500)


            mediaPlayer.start()
            isRecordClicked = false

        }
        else {
            mediaPlayer.pause()
            length = mediaPlayer.currentPosition
            messageViewHolder.playRecordSender.setImageResource(R.drawable.play)
            isRecordClicked = true
            isPaused = true
            currentPosition = position
        }



    }
    private fun playRecordReceiver(
        file: String,
        messageViewHolder: ViewHolder,
        mediaPlayer: MediaPlayer,
        position: Int
    ) {


        if (isRecordClicked) {

            if (isPaused) {
                if (currentPosition == position)
                // this mean we clicked on same record
                    mediaPlayer.seekTo(length) // in case we paused record and resumed it again
                else {

                    messageViewHolder.receiverSeekBar.progress = 0
                    mediaPlayer.reset()
                    mediaPlayer.setDataSource(file)
                    mediaPlayer.prepare()
                }

            } else {

                mediaPlayer.setDataSource(file)
                mediaPlayer.prepare()
            }

            messageViewHolder.playRecordReceiver.setImageResource(R.drawable.ic_baseline_pause_circle_outline_24)
            mediaPlayer.setOnCompletionListener {
                messageViewHolder.playRecordReceiver.setImageResource(R.drawable.play)
                it.reset()
                isPaused = false
                isRecordClicked = true
            }
            messageViewHolder.receiverSeekBar.setOnSeekBarChangeListener(object :
                OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {

                }

                override fun onStartTrackingTouch(seekBar: SeekBar) {}
                override fun onStopTrackingTouch(seekBar: SeekBar) {
                    currentPos = seekBar.progress.toDouble()
                    mediaPlayer.seekTo(currentPos.toInt())
                }
            })
            currentPos = mediaPlayer.currentPosition.toDouble()
            messageViewHolder.receiverCurrent.text = timerConversion(currentPos.toLong())
            messageViewHolder.receiverSeekBar.max = mediaPlayer.duration
            val handler = Handler(Looper.getMainLooper())

            val runnable: Runnable = object : Runnable {
                override fun run() {
                    try {
                        currentPos = mediaPlayer.currentPosition.toDouble()
                        messageViewHolder.receiverCurrent.text = timerConversion(currentPos.toLong())
                        messageViewHolder.receiverSeekBar.progress = currentPos.toInt()
                        handler.postDelayed(this, 500)
                    } catch (ed: IllegalStateException) {
                        ed.printStackTrace()
                    }
                }
            }
            handler.postDelayed(runnable, 500)


            mediaPlayer.start()
            isRecordClicked = false

        }
        else {
            mediaPlayer.pause()
            length = mediaPlayer.currentPosition
            messageViewHolder.playRecordReceiver.setImageResource(R.drawable.play)
            isRecordClicked = true
            isPaused = true
            currentPosition = position
        }



    }


    override fun onDataChanged() {
        // do your thing
        if (itemCount == 0) Toast.makeText(context, "No Messages", Toast.LENGTH_SHORT).show()
    }



    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)/*,View.OnClickListener*/ {

        val linearLayoutSenderPicture: LinearLayout = itemView.findViewById(R.id.message_sender_image_view_linearLayout)
        val linearLayoutReceiverPicture: LinearLayout = itemView.findViewById(R.id.message_receiver_image_view_linearLayout)
        val linearLayoutSenderVideo: LinearLayout = itemView.findViewById(R.id.sender_video_linearLayout)
        val linearLayoutReceiverVideo: LinearLayout = itemView.findViewById(R.id.receiver_video_linearLayout)
        val linearLayoutSenderMessageText: LinearLayout = itemView.findViewById(R.id.sender_messsage_text_linearLayout)
        val linearLayoutReceiverMessageText: LinearLayout = itemView.findViewById(R.id.receiver_message_text_linearLayout)
        val linearLayoutSenderFile: LinearLayout = itemView.findViewById(R.id.linear_layout_sender_file)
        val linearLayoutReceiverFile: LinearLayout = itemView.findViewById(R.id.linear_layout_receiver_file)
        val linearLayoutSenderRecord: LinearLayout = itemView.findViewById(R.id.sender_record_linearLayout)
        val linearLayoutReceiverRecord: LinearLayout = itemView.findViewById(R.id.receiver_record_linearLayout)

        val senderMessageText: TextView = itemView.findViewById(R.id.sender_messsage_text)
        val  senderMessageTime: TextView = itemView.findViewById(R.id.sender_text_time)
        val receiverMessageText: TextView =  itemView.findViewById(R.id.receiver_message_text)
        val  receiverMessageTime: TextView = itemView.findViewById(R.id.receiver_text_time)
        val messageReceiverPicture: ImageView = itemView.findViewById(R.id.message_receiver_image_view)
        val  messageSenderPicture: ImageView = itemView.findViewById(R.id.message_sender_image_view)
        val  messageSenderPictureTime: TextView = itemView.findViewById(R.id.message_sender_image_view_time)
        val  messageReceiverPictureTime: TextView = itemView.findViewById(R.id.message_receiver_image_view_time)
        val  videoSender: VideoView = itemView.findViewById(R.id.sender_video)
        val  videoReceiver: VideoView = itemView.findViewById(R.id.receiver_video)
        val  videoSenderTime: TextView = itemView.findViewById(R.id.sender_video_time)
        val  videoReceiverTime: TextView = itemView.findViewById(R.id.receiver_video_time)

        val  playVideoSender: ImageButton = itemView.findViewById(R.id.sender_play_button)
        val  playVideoReceiver: ImageButton = itemView.findViewById(R.id.receiver_play_button)
        val  senderFileName: TextView = itemView.findViewById(R.id.sender_file_name)
        val  receiverFileName: TextView = itemView.findViewById(R.id.receiver_file_name)
        val  fileSenderTime: TextView = itemView.findViewById(R.id.sender_file_time)
        val  fileReceiverTime: TextView = itemView.findViewById(R.id.receiver_file_time)
        val  recordSenderTime: TextView = itemView.findViewById(R.id.sender_record_time)
        val  recordReceiverTime: TextView = itemView.findViewById(R.id.receiver_record_time)

        val senderCurrent: TextView = itemView.findViewById(R.id.sender_current)
        val receiverCurrent: TextView =  itemView.findViewById(R.id.receiver_current)
        val senderTotal: TextView = itemView.findViewById(R.id.sender_total)
        val receiverTotal: TextView =  itemView.findViewById(R.id.receiver_total)
        val receiverSeekBar: SeekBar =  itemView.findViewById(R.id.receiver_seekbar)
        val senderSeekBar: SeekBar =  itemView.findViewById(R.id.sender_seekbar)
        val playRecordSender: ImageView = itemView.findViewById(R.id.sender_play_record)
        val playRecordReceiver: ImageView = itemView.findViewById(R.id.receiver_play_record)


    }




    class MessageClickListener(val messageClick: (type: String, fileUrl: String, fileName: String) -> Unit)
    class PlayVideoButtonClickListener(val playVideoButtonClick: (file: String) -> Unit)


    private fun getAudioFileLength(path: String?, stringFormat: Boolean): String? {
        val stringBuilder = StringBuilder()
        try {
            val uri = Uri.parse(path)
            val mmr = MediaMetadataRetriever()
            mmr.setDataSource(context, uri)
            val duration: String = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)!!
            val millSecond = duration.toInt()
            if (millSecond < 0) return 0.toString() // if some error then we say duration is zero
            if (!stringFormat) return millSecond.toString()
            val hours: Int
            val minutes: Int
            var seconds = millSecond / 1000
            hours = seconds / 3600
            minutes = seconds / 60 % 60
            seconds %= 60
            if (hours in 1..9) stringBuilder.append("0").append(hours).append(":") else if (hours > 0) stringBuilder.append(
                hours
            ).append(":")
            if (minutes < 10) stringBuilder.append("0").append(minutes).append(":") else stringBuilder.append(
                minutes
            ).append(":")
            if (seconds < 10) stringBuilder.append("0").append(seconds) else stringBuilder.append(
                seconds
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return stringBuilder.toString()
    }

    //time conversion
    fun timerConversion(value: Long): String? {
        val audioTime: String
        val dur = value.toInt()
        val hrs = dur / 3600000
        val mns = dur / 60000 % 60000
        val scs = dur % 60000 / 1000
        audioTime = if (hrs > 0) {
            String.format("%02d:%02d:%02d", hrs, mns, scs)
        } else {
            String.format("%02d:%02d", mns, scs)
        }
        return audioTime
    }




}