package com.fullChat.app.viewModel

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.messaging.FirebaseMessaging
import com.fullChat.app.models.LastMessageModel
import com.fullChat.app.models.UpdateModel
import com.fullChat.app.models.UsersModel
import com.fullChat.app.netWork.ApiClient
import com.fullChat.app.utiles.UserData
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LastMessageActivityViewModel(private val application: Application) : ViewModel() {



    private val _options = MutableLiveData<FirestoreRecyclerOptions<LastMessageModel>>()
    private val user = FirebaseAuth.getInstance().currentUser



    val options: LiveData<FirestoreRecyclerOptions<LastMessageModel>>
        get() = _options




     fun getUsers(){
         val query: Query = FirebaseFirestore.getInstance().collection("lastMessages").document(user!!.uid).collection(user.uid)
         _options.value = FirestoreRecyclerOptions.Builder<LastMessageModel>().setQuery(query, LastMessageModel::class.java).build()
     }


    }



