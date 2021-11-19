package com.fullChat.app.activities

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentResolver
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.PorterDuff
import android.media.MediaRecorder
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.AdapterDataObserver
import com.fullChat.app.R
import com.fullChat.app.adapters.ChatAdapter
import com.fullChat.app.utiles.FilePath
import com.fullChat.app.viewModel.ChatActivityViewModel
import com.fullChat.app.viewModel.viewModelFactory.ChatActivityViewModelFactory
import com.google.firebase.firestore.FieldValue
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import id.zelory.compressor.Compressor
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class ChatActivity : AppCompatActivity() {

    lateinit var recyclerView: RecyclerView
    private lateinit var linearLayoutManager: LinearLayoutManager
    private lateinit var textsend: EditText
    private lateinit var btSend: ImageButton
    private lateinit var btRecord: ImageButton
    private lateinit var btSendFile: ImageButton
    lateinit var chatadapter: ChatAdapter
    private lateinit var toolbar:Toolbar
    lateinit var chatActivityViewModel: ChatActivityViewModel
    private  val requestPickFileCode = 288
    private var urlOfFile : String = ""
    private var nameOfFile : String = ""
    private var isRecording:Boolean = true
    private var isNewRecord:Boolean = true
    private var recordFileName = ""
    private var myAudioRecorder:MediaRecorder? = null



    @SuppressLint("SimpleDateFormat", "WrongConstant")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        toolbar = findViewById(R.id.toolbar)
        textsend = findViewById(R.id.textsend)
        btSend = findViewById(R.id.send)
        btSendFile = findViewById(R.id.sendFile)
        btRecord = findViewById(R.id.record)
        recyclerView = findViewById(R.id.chatrecyclerview)
        linearLayoutManager =  LinearLayoutManager(applicationContext)
        linearLayoutManager.stackFromEnd = true
        setSupportActionBar(toolbar)

         val application = requireNotNull(this).application
        val chatActivityViewModelFactory = ChatActivityViewModelFactory(application)
        chatActivityViewModel = ViewModelProvider(this,chatActivityViewModelFactory).get(ChatActivityViewModel::class.java)

        chatActivityViewModel.setReceiverID(intent.getStringExtra("receiverID").toString())
        chatActivityViewModel.setChatId(intent.getStringExtra("CHAT_ID").toString())
        chatActivityViewModel.setPeeredEmail(intent.getStringExtra("Email").toString())
        chatActivityViewModel.setName(intent.getStringExtra("Name").toString())
        chatActivityViewModel.updatePeerDevice()
        chatActivityViewModel.getMessages()
        chatActivityViewModel.getLastSeen()
        chatActivityViewModel.getCallStatusForRemoteUser()
        chatActivityViewModel.updateToken()



        chatActivityViewModel.options.observe(this, Observer {
            chatadapter = ChatAdapter(application, it,
                    ChatAdapter.MessageClickListener{ type: String, file: String, fileName: String ->
                           urlOfFile = file
                           nameOfFile = fileName
                           if ((type=="image"||type=="gif") && file.isNotEmpty())
                          {
                              val intent = Intent(this, MediaView::class.java)
                               intent.putExtra("image", file)
                               startActivity(intent)
                           }
                           else if (type=="video" && file.isNotEmpty()){
                               val intent = Intent(this, MediaView::class.java)
                               intent.putExtra("video", file)
                               startActivity(intent)
                           }
                           else if ((type=="document"||type=="pdf"||type=="audio")&& file.isNotEmpty()){
                               when {
                                   ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED -> {
                                   chatActivityViewModel.downloadFile(file,fileName)
                                   }

                                   ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) -> {
                                       permissionExplanation("Storage permission needed","This permission is needed to allow us help you to download the file.", "write files")
                                   }
                                   else -> {
                                       askWriteStoragePermission()
                                   }

                               }

                           }
            },ChatAdapter.PlayVideoButtonClickListener{ file ->
                val intent = Intent(this, MediaView::class.java)
                intent.putExtra("video", file)
                startActivity(intent)
             })


            chatadapter.registerAdapterDataObserver(object : AdapterDataObserver() {
                override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                    recyclerView.scrollToPosition(chatadapter.itemCount - 1)
                }
            })
            recyclerView.adapter = chatadapter
            recyclerView.layoutManager = linearLayoutManager
            chatadapter.startListening()
        })





        chatActivityViewModel.name.observe(this, Observer {
            supportActionBar!!.title = it

        })
        chatActivityViewModel.lastSeen.observe(this, Observer {
            supportActionBar!!.subtitle = it

        })
        chatActivityViewModel.updateType("Online",null)






        btSend.setOnClickListener{
         if (textsend.text.toString().isNotEmpty()){
             val  messageText =  textsend.text.toString()
             chatActivityViewModel.setMessageText(messageText)
             chatActivityViewModel.sendMessage()
         }
            textsend.setText("")

        }
        btSendFile.setOnClickListener{
            when {
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED -> {
                    chooseMediaType()
                }

                ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE) -> {
                    permissionExplanation("Storage permission needed","This permission is needed to allow us help you to Upload the file.", "read files")
                }
                else -> {
                    askReadStoragePermission()
                }

            }


        }
        btRecord.setOnClickListener{
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                if (myAudioRecorder==null) {
                    myAudioRecorder = MediaRecorder()
                    myAudioRecorder!!.setAudioSource(MediaRecorder.AudioSource.MIC)
                    myAudioRecorder!!.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                    myAudioRecorder!!.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB)
                }

                val root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).toString()
                val myDir = File("$root/Bego Chat/Bego Recorders")
                if (!myDir.exists())
                    myDir.mkdirs()

                if (isNewRecord) {
                    /* isNewRecord
                                // عشان لما الميثود تستدعي للمره التانيه عشان نوقف التسجيل متغيرش اسم التسجيل لاسم جديد فيحصل لغبطه لما يجي يترفع
                                و نغير القيمه دي لما التسجيل يترفع عشان لما نيجى نرفع واحد جديد
                                */

                    val formatter: SimpleDateFormat = SimpleDateFormat("yyyy_MM_dd_hh_mm_ss", Locale.ROOT)
                    val now: Date = Date()
                    recordFileName = "Recording ${formatter.format(now)}.3gp" //file name
                }
                myAudioRecorder!!.setOutputFile("$root/Bego Chat/Bego Recorders/$recordFileName")

                if (isRecording) {

                    btRecord.setColorFilter(ContextCompat.getColor(applicationContext,R.color.colorPrimary), PorterDuff.Mode.MULTIPLY)
                    isRecording = false
                    isNewRecord = false
                    try {
                        myAudioRecorder!!.prepare()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                    myAudioRecorder!!.start()
                    Toast.makeText(applicationContext, "Recording started", Toast.LENGTH_LONG).show()

                } else{
                    btRecord.clearColorFilter()
                    Log.d("TAG", "ahmedelbagory: ${"Recording saved as  ${"$root/Bego Chat/Bego Recorders/$recordFileName"}"}")
                    myAudioRecorder!!.stop()
                    myAudioRecorder!!.release()
                    Toast.makeText(
                        applicationContext,
                        "sending Audio Recorder....",
                        Toast.LENGTH_LONG
                    ).show()

                    chatActivityViewModel.fileUri.value = Uri.fromFile( File("$root/Bego Chat/Bego Recorders/$recordFileName"))
                    chatActivityViewModel.uploadFile("record")
                    isNewRecord = true
                    isRecording = true
                    myAudioRecorder = null

                }
            }
            else {
                permissionExplanation("mic permission needed","This permission is needed to allow us help you to make calls and send voice messages.", "mic")
            }


        }

        textsend.addTextChangedListener(object : TextWatcher {
                     override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                        if (!TextUtils.isEmpty(s)) {
                            //Set the value of typing field to true.
                            chatActivityViewModel.updateType("Typing...",null)
                        } else {
                            // Set to false
                            chatActivityViewModel.updateType("Online",null)
                        }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}
        })


    }

    private var writePermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                if (isGranted) {
                    Toast.makeText(this, "Write Permission Granted", Toast.LENGTH_SHORT).show();

                } else {
                    permissionExplanation("Storage permission needed","This permission is needed to allow us help you to download the file.", "write files")
                }

            }
    private var readPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                Toast.makeText(this, "Read Permission Granted", Toast.LENGTH_SHORT).show();

            } else {
                permissionExplanation("Storage permission needed","This permission is needed to allow us help you to Upload the file.", "read files")
            }

        }
    private var recordPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                Toast.makeText(this, "mic Permission Granted", Toast.LENGTH_SHORT).show();

            } else {
                permissionExplanation("mic permission needed","This permission is needed to allow us help you to send voice messages","mic")

            }

        }
    private var cameraPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                Toast.makeText(this, "Camera Permission Granted", Toast.LENGTH_SHORT).show();
                permissionExplanation("mic permission needed","This permission is needed to allow us help you to make calls","mic")

            } else {
                permissionExplanation("Camera permission needed","This permission is needed to allow us help you to make calls","camera")

            }

        }


    private fun askWriteStoragePermission() {
        writePermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    }
    private fun askReadStoragePermission() {
        readPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
    }
    private fun askRecordAudioPermission() {
        recordPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
    }
    private fun askCameraPermission() {
        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
    }

    private fun permissionExplanation(title:String,msg:String, permissionType: String) {
        AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(msg)
                .setPositiveButton("OK") { _, _ ->
                    when(permissionType){
                        "camera" -> {
                            askCameraPermission()
                        }
                        "mic" ->{
                            askRecordAudioPermission()
                        }
                        "read files" ->{
                            askReadStoragePermission()
                        }
                        "write files" ->{
                            askWriteStoragePermission()
                        }
                    }
                }
                .setNegativeButton("No thanks") { dialog, _ ->
                    dialog.dismiss()
                }
                .create()
                .show()
    }

      private fun chooseMediaType(){

          val intent = Intent()
          intent.type = "*/*"
          intent.action = Intent.ACTION_GET_CONTENT
          startActivityForResult(intent, requestPickFileCode)

      }

    private fun cropRequest(imageUri: Uri) {
        CropImage.activity(imageUri)
            .setGuidelines(CropImageView.Guidelines.ON)
            .setMultiTouchEnabled(true)
            .start(this)
    }



    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        //RESULT FROM SELECTED File
        if (requestCode == requestPickFileCode && resultCode == Activity.RESULT_OK) {
            val fileUri: Uri = CropImage.getPickImageResultUri(this, data)
            val cr: ContentResolver = this.contentResolver
            val fileType: String = cr.getType(fileUri)!!
               // check file type
            if(fileType.contains("image/jpg")||fileType.contains("image/jpeg")||fileType.contains("image/png")) {
              //photo
                    cropRequest(fileUri)
                }
            else if (fileType.contains("video/mp4")||fileType.contains("video/3gp")) {
               //video
                chatActivityViewModel.fileUri.value = fileUri
                chatActivityViewModel.uploadFile("video")

            }
            else if (fileType.contains("officedocument")){

                chatActivityViewModel.fileUri.value = fileUri
                chatActivityViewModel.uploadFile("document")
            }
            else if (fileType.contains("application/pdf")){
                // pdf file
                chatActivityViewModel.fileUri.value = fileUri
                chatActivityViewModel.uploadFile("pdf")
            }
            else if (fileType.contains("audio")){
                // audio file
                chatActivityViewModel.fileUri.value = fileUri
                chatActivityViewModel.uploadFile("audio")
            }
            else if (fileType.contains("gif")){
                // gif file
                chatActivityViewModel.fileUri.value = fileUri
                chatActivityViewModel.uploadFile("gif")
            }
            else{
                Log.d("TBEGO", "onActivityResult: $fileType ")

            }


        }

        //RESULT FROM CROPING ACTIVITY
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)
            if (resultCode == Activity.RESULT_OK) {

                chatActivityViewModel.fileUri.value = result.uri
                //Glide.with(this).load(mImageUri).into(ImageView_profile)
                val realPath: String = FilePath.getPath(this, chatActivityViewModel.fileUri.value)
                val actualFile = File(realPath)
                try {
                    val compressedImage: Bitmap = Compressor(this).compressToBitmap(actualFile)
                    val baos = ByteArrayOutputStream()
                    compressedImage.compress(Bitmap.CompressFormat.JPEG, 50, baos)
                    chatActivityViewModel.finalFile.value = baos.toByteArray()
                    chatActivityViewModel.uploadFile("image")
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }





    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.call_items, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        super.onOptionsItemSelected(item)
        if ((ContextCompat.checkSelfPermission(this@ChatActivity,Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
            &&(ContextCompat.checkSelfPermission(this@ChatActivity,Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED)) {
            when (item.title.toString()) {
                "video Call" -> {

                    if (chatActivityViewModel.callStatusForRemoteUser.value!!){
                        Toast.makeText(this ,"user busy now",Toast.LENGTH_LONG).show()

                    }
                    else{
                        chatActivityViewModel.sendCallNotification("video call")
                        val i = Intent(this, VideoCallActivity::class.java)
                        i.putExtra("peeredEmail", intent.getStringExtra("Email").toString())
                        startActivity(i)
                    }


                }
                "Voice Call" -> {
                    if (chatActivityViewModel.callStatusForRemoteUser.value!!){
                        Toast.makeText(this ,"user busy now",Toast.LENGTH_LONG).show()

                    }
                    else{
                        chatActivityViewModel.sendCallNotification("voice call")
                        val i = Intent(this, VoiceCallActivity::class.java)
                        i.putExtra("peeredEmail", intent.getStringExtra("Email").toString())
                        i.putExtra("callerName", intent.getStringExtra("Name").toString())
                        startActivity(i)
                    }
                }
            }
        }else{
            permissionExplanation("Camera permission needed","This permission is needed to allow us help you to make calls","camera")
        }

        return true
    }











    override fun onStop() {
        super.onStop()
        chatActivityViewModel.updateType("Offline", FieldValue.serverTimestamp())
        chatActivityViewModel.updatePeerDevice()
        chatActivityViewModel.setPeeredEmail("0")
        chatadapter.stopListening()
        myAudioRecorder = null


    }

    override fun onDestroy() {
        super.onDestroy()
        chatActivityViewModel.updateType("Offline", FieldValue.serverTimestamp())
        chatActivityViewModel.setPeeredEmail("0")
        chatActivityViewModel.updatePeerDevice()
        chatadapter.stopListening()
        myAudioRecorder = null


    }


    override fun onPause() {
        super.onPause()
        chatActivityViewModel.updateType("Offline", FieldValue.serverTimestamp())
        chatActivityViewModel.setPeeredEmail("0")
        chatActivityViewModel.updatePeerDevice()
        chatadapter.stopListening()
        btRecord.clearColorFilter()
        if (!isRecording)
            Toast.makeText(
                    applicationContext,
                    "Recorder canceled",
                    Toast.LENGTH_LONG
            ).show()
        isRecording = true
        isNewRecord = true


    }

    override fun onResume() {
        super.onResume()
        chatadapter.startListening()
        chatadapter.notifyDataSetChanged()

        chatActivityViewModel.updateType("Online",null)
        chatActivityViewModel.setPeeredEmail(intent.getStringExtra("Email").toString())
        chatActivityViewModel.updatePeerDevice()



    }




}