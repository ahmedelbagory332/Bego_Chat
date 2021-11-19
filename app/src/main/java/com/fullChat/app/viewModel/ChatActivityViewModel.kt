package com.fullChat.app.viewModel

import android.annotation.SuppressLint
import android.app.Application
import android.app.DownloadManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.database.Cursor
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.OpenableColumns
import android.util.Log
import android.widget.FrameLayout
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.fullChat.app.BuildConfig
import com.fullChat.app.R
import com.fullChat.app.models.*
import com.fullChat.app.netWork.ApiClient
import com.fullChat.app.utiles.UserData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import io.agora.rtc.RtcEngine
import io.agora.rtc.video.VideoCanvas
import io.agora.rtc.video.VideoEncoderConfiguration
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class ChatActivityViewModel(private val application: Application) : ViewModel() {

    private val notificationManager = application.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
    private val notificationBuilder = NotificationCompat.Builder(application, "notification channel progress id")
    private val _options = MutableLiveData<FirestoreRecyclerOptions<ChatModel>>()
    private val _chatId = MutableLiveData<String>()
    private val _peeredEmail = MutableLiveData<String>()
    private val _name = MutableLiveData<String>()
    private val _receiverID = MutableLiveData<String>()
    private val _lastSeen = MutableLiveData<String>()
    private var _callStatusForRemoteUser = MutableLiveData<Boolean>()
    private var _callCanceledStatus = MutableLiveData<Boolean>()
    private val _messageText = MutableLiveData<String>()
    private val _isRecordFinished = MutableLiveData<Boolean>()
    private val _appId = MutableLiveData<String>()
    private val _channelName = MutableLiveData<String>()
    private val _token = MutableLiveData<String>()

    var fileUri = MutableLiveData<Uri>()
    var finalFile  = MutableLiveData<ByteArray>()

    private val user = FirebaseAuth.getInstance().currentUser
    private var mStorageRef: StorageReference  = FirebaseStorage.getInstance().getReference("Files")
    private val mStorageRefRecordFiles: StorageReference  = FirebaseStorage.getInstance().getReference("Audio Recorders")
    private var db = FirebaseFirestore.getInstance()

 init {
     if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
         val notificationChannel = NotificationChannel(
                 "notification channel progress id", "uploading progress", NotificationManager.IMPORTANCE_HIGH
         )

         notificationChannel.description = "notification to show uploading progress"
         notificationManager.createNotificationChannel(notificationChannel)
     }
 }



    val options: LiveData<FirestoreRecyclerOptions<ChatModel>>
        get() = _options

    val chatId: LiveData<String>
        get() = _chatId

    val appId: LiveData<String>
        get() = _appId

    val channelName: LiveData<String>
        get() = _channelName

    val token: LiveData<String>
        get() = _token



    val peeredEmail: LiveData<String>
        get() = _peeredEmail

    val name: LiveData<String>
        get() = _name

    val receiverID: LiveData<String>
        get() = _receiverID

    val lastSeen: LiveData<String>
        get() = _lastSeen

    val callStatusForRemoteUser: LiveData<Boolean>
        get() = _callStatusForRemoteUser

    val callCanceledStatus: LiveData<Boolean>
        get() = _callCanceledStatus

    val messageText: LiveData<String>
        get() = _messageText



    fun getMessages(){
         val query: Query = FirebaseFirestore.getInstance().collection("chat").document(chatId.value!!).collection(chatId.value!!).orderBy("timestamp", Query.Direction.ASCENDING)
         _options.value = FirestoreRecyclerOptions.Builder<ChatModel>().setQuery(query, ChatModel::class.java).build()
     }

     fun setChatId(chatId:String){
         _chatId.value = chatId
     }

    fun setPeeredEmail(peeredEmail:String){
        _peeredEmail.value = peeredEmail
    }

    fun setIsRecordFinished(status:Boolean){
        _isRecordFinished.value = status
    }

    fun setName(name:String){
        _name.value = name
    }

    fun setReceiverID(receiverID:String){
        _receiverID.value = receiverID
    }

    fun setMessageText(messageText:String){
        _messageText.value = messageText
    }

    fun setUpAgora(){

        _appId.value = "c877d2d950114b15b12ba0c73c3c3b8d"
        _channelName.value = "bego"
        _token.value = "006c877d2d950114b15b12ba0c73c3c3b8dIACzWoCi3jl5cE7hvhvdgmr+MRKHY0ybXAXTdNK7i+ghdvZoxgkAAAAAEADsTG0XRgSZYQEAAQBFBJlh"
    }

    fun updatePeerDevice(){
        ApiClient().getINSTANCE()?.updatePeerDevice(FirebaseAuth.getInstance().currentUser!!.email!!,peeredEmail.value!!)?.enqueue(object : Callback<UpdateModel> {
            override fun onResponse(call: Call<UpdateModel>, response: Response<UpdateModel>) {

                // if (response.body()!!.response == "0")Toast.makeText(applicationContext,"updated", Toast.LENGTH_LONG).show()

            }

            override fun onFailure(call: Call<UpdateModel>, t: Throwable) {
                //Toast.makeText(application,"error updatePeerDevice \n"+t.message.toString(), Toast.LENGTH_LONG).show()
            }
        })

    }



    fun uploadFile(type:String) {

        if (fileUri.value != null) {
            when (type) {
                "image" -> {

                    val fileReference: StorageReference = mStorageRef.child(getFileName(fileUri.value!!)!!)

                    val uploadTask: UploadTask = fileReference.putBytes(finalFile.value!!)
                    saveFileUrl(uploadTask, fileReference, type,getFileName(fileUri.value!!)!!)

                }
                "gif" -> {

                val fileReference: StorageReference = mStorageRef.child(getFileName(fileUri.value!!)!!)

                    val uploadTask: UploadTask = fileReference.putFile(fileUri.value!!)
                saveFileUrl(uploadTask, fileReference, type,getFileName(fileUri.value!!)!!)

            }
                "document", "pdf", "audio" -> {

                    val fileReference: StorageReference = mStorageRef.child(getFileName(fileUri.value!!)!!)
                    val uploadTask: UploadTask = fileReference.putFile(fileUri.value!!)
                    saveFileUrl(uploadTask, fileReference, type,getFileName(fileUri.value!!)!!)

                }
                "video" -> {

                    val fileReference: StorageReference = mStorageRef.child(getFileName(fileUri.value!!)!!)

                    val uploadTask: UploadTask = fileReference.putFile(fileUri.value!!)
                    saveFileUrl(uploadTask, fileReference, type,getFileName(fileUri.value!!)!!)
                }
                "record" -> {

                    val fileReference: StorageReference = mStorageRefRecordFiles.child(getFileName(fileUri.value!!)!!)

                    val uploadTask: UploadTask = fileReference.putFile(fileUri.value!!)
                    saveFileUrl(uploadTask, fileReference, type,getFileName(fileUri.value!!)!!)
                }
//                else -> {
//
//                    val fileReference: StorageReference = mStorageRef.child(getFileName(fileUri.value!!)!!)
//
//                    val uploadTask: UploadTask = fileReference.putFile(fileUri.value!!)
//                    saveFileUrl(uploadTask, fileReference, type,"")
//                }
            }
        }
        else {
            Toast.makeText(application, "لم يتم اختيار ملف", Toast.LENGTH_SHORT).show()
        }



    }


    private fun saveFileUrl(uploadTask: UploadTask, fileReference: StorageReference, type: String, name: String) {
        // name param for save name file in case we upload document file

        uploadTask.addOnSuccessListener {
            fileReference.downloadUrl.addOnSuccessListener {

                val chatref = db.collection("chat").document(chatId.value!!).collection(chatId.value!!)
                val chat = ChatModel(
                        user!!.uid,
                        receiverID.value!!,
                        user.displayName,
                        name,
                        it.toString(),
                        type
                )
                ApiClient().getINSTANCE()?.sendTextNotification(peeredEmail.value!!, user.email!!, "${user.displayName} send you $type", "Bego Chat")?.enqueue(object : Callback<NotificationModel> {
                    override fun onResponse(call: Call<NotificationModel>, response: Response<NotificationModel>) {
                    }

                    override fun onFailure(call: Call<NotificationModel>, t: Throwable) {
                        // Toast.makeText(application,t.message.toString(), Toast.LENGTH_LONG).show()

                    }
                })

                chatref.add(chat)
                updateLastMessage(type)

            }
        }

                .addOnFailureListener { e ->
                    Toast.makeText(application, e.message, Toast.LENGTH_LONG).show()

                    Toast.makeText(application, "حدث خطا ما", Toast.LENGTH_SHORT).show()
                }
            .addOnProgressListener{

                notificationBuilder
                        .setWhen(System.currentTimeMillis())
                        .setSmallIcon(R.drawable.upload)
                        .setTicker(application.resources.getString(R.string.app_name))
                        .setCategory(NotificationCompat.CATEGORY_PROGRESS)
                        .setContentTitle("Uploading...")
                        .setContentText("Upload in progress")
                        .setAutoCancel(true)
                        .setOngoing(true)
                        .setOnlyAlertOnce(true)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setProgress(it.totalByteCount.toInt(), 0, true)
                notificationManager.notify(101, notificationBuilder.build())

                notificationBuilder.setProgress(it.totalByteCount.toInt(), it.bytesTransferred.toInt(), false)
                        .setAutoCancel(false)
                notificationManager.notify(101, notificationBuilder.build())


            }.addOnCompleteListener{


                    notificationBuilder.setContentTitle("Uploaded")
                    .setContentText("Upload finished")
                    .setProgress(0, 0, false)
                    .setAutoCancel(true)
                    .setOngoing(false)
                    notificationManager.notify(101, notificationBuilder.build())
            }
    }

    private fun updateLastMessage(messageText: String ) {
        val lastMessagesRef = db.collection("lastMessages").document(receiverID.value!!).collection(receiverID.value!!)
        val docRef: Query = db.collection("lastMessages").document(receiverID.value!!).collection(receiverID.value!!).whereEqualTo("chatId",chatId.value!!)
        docRef.get().addOnSuccessListener { documents ->
            val list: MutableList<String> = ArrayList()
            for (document in documents) {

                list.add(document.id)
            }

            if(list.size==0){
                val lastMessageModel =
                    LastMessageModel(
                        chatId.value!!,
                        user!!.uid,
                        user.displayName,
                        user.email,
                        messageText
                    )
                lastMessagesRef.add(lastMessageModel)

            }
            else{

                for (id in list) {
                    db.collection("lastMessages").document(receiverID.value!!).collection(receiverID.value!!).document(id)
                        .update("message",messageText)
                        .addOnSuccessListener {
                            Log.d("ChatActivity", " Updated!")

                        }

                }

            }
        }
            .addOnFailureListener { exception ->
                Toast.makeText(application, "Error getting documents: $exception", Toast.LENGTH_SHORT).show()
            }

    }

    @SuppressLint("SimpleDateFormat")
    fun getLastSeen(){
        db.collection("users").whereEqualTo("userId",receiverID.value!!).addSnapshotListener { value, e ->
            if (e != null) {
                Log.w("TAG", "Listen failed.", e)
                return@addSnapshotListener
            }

            for (document in value!!) {
                 if (document.data["lastSeen"]!=null&& document.data["type"].toString() == "Offline") {
                    val timestamp = document.data["lastSeen"] as com.google.firebase.Timestamp
                    val milliseconds = timestamp.seconds * 1000 + timestamp.nanoseconds / 1000000
                    val sdf = SimpleDateFormat("dd MMM yyyy hh:mm a")
                    val netDate = Date(milliseconds)
                    val date = sdf.format(netDate).toString()

                     _lastSeen.value = date

                }else{
                     _lastSeen.value = document.data["type"].toString()

                 }

            }
        }

    }

    fun getCallStatusForRemoteUser(){

        db.collection("users").whereEqualTo("userId",receiverID.value!!).addSnapshotListener { value, e ->
            if (e != null) {
                Log.w("TAG", "Listen failed.", e)
                return@addSnapshotListener
            }
            for (document in value!!) {
                _callStatusForRemoteUser.value = document.data["callStatus"] as Boolean?


            }

         }
    }

    fun getCallCanceledStatus(){

        db.collection("users").whereEqualTo("userId",FirebaseAuth.getInstance().currentUser!!.uid).addSnapshotListener { value, e ->
            if (e != null) {
                Log.w("TAG", "Listen failed.", e)
                return@addSnapshotListener
            }
            for (document in value!!) {
                _callCanceledStatus.value = document.data["callCanceled"] as Boolean?

            }

            Log.d("TAG", "_callCanceledStatus ${callCanceledStatus.value}")

        }
    }



    fun sendMessage(){
        val chatRef = db.collection("chat").document(chatId.value!!).collection(chatId.value!!)
        val chat = ChatModel(user!!.uid, receiverID.value!!, user.displayName, messageText.value!!, "", "text")
        chatRef.add(chat)
        /////

        updateLastMessage(messageText.value!!)

        /////
        ApiClient().getINSTANCE()?.sendTextNotification(peeredEmail.value!!,user.email!!,user.displayName!!+" : "+messageText.value!!,"Bego Chat")?.enqueue(object : Callback<NotificationModel> {
            override fun onResponse(call: Call<NotificationModel>, response: Response<NotificationModel>) {
                //Toast.makeText(applicationContext,response.body()!!.success, Toast.LENGTH_LONG).show()
                Log.d("TAG", "Notification done ${response.body()}")

            }

            override fun onFailure(call: Call<NotificationModel>, t: Throwable) {
                // Toast.makeText(applicationContext,t.message.toString(), Toast.LENGTH_LONG).show()
                Log.d("TAG", "Notification error ${t.message}")

            }
        })
    }

    fun sendCallNotification(callType: String) {
        //we send remote user id to use to cancel call in BroadcastReceiver for notification
        ApiClient().getINSTANCE()?.sendCallNotification(peeredEmail.value!!,user!!.displayName!!+" is calling you",FirebaseAuth.getInstance().currentUser!!.displayName+callType+FirebaseAuth.getInstance().currentUser!!.uid)?.enqueue(object : Callback<NotificationModel> {
            override fun onResponse(call: Call<NotificationModel>, response: Response<NotificationModel>) {
                //Toast.makeText(applicationContext,response.body()!!.success, Toast.LENGTH_LONG).show()
                Log.d("TAG", "Notification done ${response.body()}")

            }

            override fun onFailure(call: Call<NotificationModel>, t: Throwable) {
                // Toast.makeText(applicationContext,t.message.toString(), Toast.LENGTH_LONG).show()

                Log.d("TAG", "Notification error ${t.message}")

            }
        })
    }
    fun sendMissedCallNotification(){
        ApiClient().getINSTANCE()?.sendTextNotification(peeredEmail.value!!,user?.email!!,"Missed call from ${user.displayName!!}","Missed call")?.enqueue(object : Callback<NotificationModel> {
            override fun onResponse(call: Call<NotificationModel>, response: Response<NotificationModel>) {
                //Toast.makeText(applicationContext,response.body()!!.success, Toast.LENGTH_LONG).show()
                Log.d("TAG", "Notification done ${response.body()}")

            }

            override fun onFailure(call: Call<NotificationModel>, t: Throwable) {
                // Toast.makeText(applicationContext,t.message.toString(), Toast.LENGTH_LONG).show()
                Log.d("TAG", "Notification error ${t.message}")

            }
        })
    }

    fun updateType(offlineType:String, dateType: FieldValue?){
        val docRef: Query = db.collection("users").whereEqualTo("userId",FirebaseAuth.getInstance().currentUser!!.uid)
        docRef.get().addOnSuccessListener { documents ->
            val list: MutableList<String> = ArrayList()
            for (document in documents) {

                list.add(document.id)
            }
            if (dateType == null){
                // mean user is online
                for (id in list) {
                    db.collection("users").document(id).update("type", offlineType)
                        .addOnSuccessListener { Log.d("ChatActivity", "type Updated!") }
                }
            }
            else{
                for (id in list) {
                    db.collection("users").document(id).update("type", offlineType)
                        .addOnSuccessListener { Log.d("ChatActivity", "type Updated!") }
                    db.collection("users").document(id).update("lastSeen", dateType)
                        .addOnSuccessListener { Log.d("ChatActivity", "lastSeen Updated!") }
                }
            }

        }
            .addOnFailureListener { exception ->
                Toast.makeText(application, "Error getting documents: $exception", Toast.LENGTH_SHORT).show()
            }
    }


    fun updateCallStatus(callStatus:Boolean){
        val docRef: Query = db.collection("users").whereEqualTo("userId",FirebaseAuth.getInstance().currentUser!!.uid)
        docRef.get().addOnSuccessListener { task ->

            db.collection("users").document(task.documents[0].id).update("callStatus", callStatus)

        }
            .addOnFailureListener { exception ->
                Toast.makeText(application, "Error getting documents: $exception", Toast.LENGTH_SHORT).show()
            }
    }
    fun updateCallCanceledStatus(status:Boolean){
        val docRef: Query = db.collection("users").whereEqualTo("userId",FirebaseAuth.getInstance().currentUser!!.uid)
        docRef.get().addOnSuccessListener { task ->

            db.collection("users").document(task.documents[0].id).update("callCanceled", status)

        }
                .addOnFailureListener { exception ->
                    Toast.makeText(application, "Error getting documents: $exception", Toast.LENGTH_SHORT).show()
                }
    }

    fun updateToken(){
         FirebaseMessaging.getInstance().token.addOnSuccessListener { instanceIdResult ->
            if (!UserData(application).readToken().equals(instanceIdResult)) {

                UserData(application).writeToken(instanceIdResult)
                ApiClient().getINSTANCE()?.updateToken(
                    FirebaseAuth.getInstance().currentUser!!.email!!, instanceIdResult)?.enqueue(object : Callback<UpdateModel> {
                    override fun onResponse(
                        call: Call<UpdateModel>,
                        response: Response<UpdateModel>
                    ) {

                        //   if (response.body()!!.response == "0")Toast.makeText(applicationContext,"updated", Toast.LENGTH_LONG).show()

                    }

                    override fun onFailure(call: Call<UpdateModel>, t: Throwable) {
                        Toast.makeText(
                            application,
                            t.message.toString(),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                })
            }
        }
    }

    @SuppressLint("Range")
    private fun getFileName(uri: Uri): String? {
        var result: String? = null
        if (uri.scheme == "content") {
            val cursor: Cursor? = application.contentResolver.query(uri, null, null, null, null)
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                }
            } finally {
                cursor!!.close()
            }
        }
        if (result == null) {
            result = uri.path
            val cut = result!!.lastIndexOf('/')
            if (cut != -1) {
                result = result.substring(cut + 1)
            }
        }
        return result
    }

     fun downloadFile(fileUrl:String,fileName:String) {



        // use android:requestLegacyExternalStorage="true" in manifest for getExternalStoragePublicDirectory
        val root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).toString()
        val myDir = File("$root/Bego Chat")
        if (!myDir.exists())
            myDir.mkdirs()
        val list = myDir.listFiles()
        if (list.contains(File("$root/Bego Chat/$fileName")))
        {

            //file already downloaded
             openFile(File("$root/Bego Chat/$fileName"))

        }
        else{
            // Create request for android download manager
            val downloadManager = application.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val request = DownloadManager.Request(Uri.parse(fileUrl))
            request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)

            // set title and description
            request.setTitle("$fileName Downloaded")
            request.setDescription("Start Downloading...")
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)

            //set the local destination for download file to a path within the application's external files directory
            request.setDestinationUri(Uri.fromFile(File(myDir, fileName)))
            request.setMimeType("*/*")
            downloadManager.enqueue(request)


        }

    }

    private fun openFile(url: File) {
        val uri = FileProvider.getUriForFile(application, BuildConfig.APPLICATION_ID.toString(), url)

        val intent = Intent(Intent.ACTION_VIEW)
        intent.setDataAndType(uri, "application/msword")

        if (url.toString().contains(".doc") || url.toString().contains(".docx")) {
            // Word document
            intent.setDataAndType(uri, "application/msword")
        } else if (url.toString().contains(".pdf")) {
            // PDF file
            intent.setDataAndType(uri, "application/pdf")
        } else if (url.toString().contains(".ppt") || url.toString().contains(".pptx")) {
            // Powerpoint file
            intent.setDataAndType(uri, "application/vnd.ms-powerpoint")
        } else if (url.toString().contains(".xls") || url.toString().contains(".xlsx")) {
            // Excel file
            intent.setDataAndType(uri, "application/vnd.ms-excel")
        } else if (url.toString().contains(".zip") || url.toString().contains(".rar")) {
            // WAV audio file
            intent.setDataAndType(uri, "application/x-wav")
        } else if (url.toString().contains(".rtf")) {
            // RTF file
            intent.setDataAndType(uri, "application/rtf")
        } else if (url.toString().contains(".wav") || url.toString().contains(".mp3")) {
            // WAV audio file
            intent.setDataAndType(uri, "audio/x-wav")
        } else if (url.toString().contains(".gif")) {
            // GIF file
            intent.setDataAndType(uri, "image/gif")
        } else if (url.toString().contains(".jpg") || url.toString().contains(".jpeg") || url.toString().contains(".png")) {
            // JPG file
            intent.setDataAndType(uri, "image/jpeg")
        } else if (url.toString().contains(".txt")) {
            // Text file
            intent.setDataAndType(uri, "text/plain")
        } else if (url.toString().contains(".3gp") || url.toString().contains(".mpg") || url.toString().contains(".mpeg") || url.toString().contains(".mpe") || url.toString().contains(".mp4") || url.toString().contains(".avi")) {
            // Video files
            intent.setDataAndType(uri, "video/*")
        } else {
            //if you want you can also define the intent type for any other file
            //additionally use else clause below, to manage other unknown extensions
            //in this case, Android will show all applications installed on the device
            //so you can choose which application to use
            intent.setDataAndType(uri, "*/*")
        }
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        application.startActivity(intent)
    }


     fun resetCallCancelStatus(){
         _callCanceledStatus = MutableLiveData<Boolean>()
     }

}



