package com.fullChat.app.receiver

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity
import androidx.lifecycle.ViewModelProvider
import com.fullChat.app.activities.VideoCallActivity
import com.fullChat.app.viewModel.ChatActivityViewModel
import com.fullChat.app.viewModel.viewModelFactory.ChatActivityViewModelFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.util.ArrayList

class CancelCall : BroadcastReceiver() {
    private var db = FirebaseFirestore.getInstance()

    override fun onReceive(context: Context?, intent: Intent?) {
        val notificationManager =  context!!.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val callerID:String = intent!!.getStringExtra("callerID").toString()
        val id:Int = intent.getIntExtra("id",0)
         val docRef: Query = db.collection("users").whereEqualTo("userId", callerID)
        docRef.get().addOnSuccessListener { documents ->
            val list: MutableList<String> = ArrayList()
            for (document in documents) {

                list.add(document.id)
            }
            for (documentId in list) {
                 db.collection("users").document(documentId).update("callCanceled", true)
                    .addOnSuccessListener { Log.d("ChatActivity", "callStatus Updated!") }
            }

        }
            .addOnFailureListener { exception ->
                Toast.makeText(context, "Error getting documents: $exception", Toast.LENGTH_SHORT).show()
            }

        notificationManager.cancel(id)

      }


}