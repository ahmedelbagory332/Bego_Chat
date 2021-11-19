package com.fullChat.app.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.AdapterDataObserver
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.fullChat.app.R
import com.fullChat.app.adapters.LastMessageAdapter
import com.fullChat.app.adapters.UserAdapter
import com.fullChat.app.models.LastMessageModel
import com.fullChat.app.models.UsersModel
import com.fullChat.app.viewModel.LastMessageActivityViewModel
import com.fullChat.app.viewModel.UsersActivityViewModel
import com.fullChat.app.viewModel.viewModelFactory.LastMessageActivityViewModelFactory
import com.fullChat.app.viewModel.viewModelFactory.UsersActivityViewModelFactory


class LastMessageActivity : AppCompatActivity() {

    lateinit var recyclerView: RecyclerView
    lateinit var linearLayoutManager: LinearLayoutManager
    lateinit var lastMessageAdapter: LastMessageAdapter
    lateinit var lastMessageActivityViewModel: LastMessageActivityViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_last_message)



        recyclerView = findViewById(R.id.userrecyclerview)
        linearLayoutManager =  LinearLayoutManager(applicationContext)
        recyclerView.layoutManager = linearLayoutManager


        val application = requireNotNull(this).application
        val lastMessageActivityViewModelFactory = LastMessageActivityViewModelFactory(application)
        lastMessageActivityViewModel = ViewModelProvider(this,lastMessageActivityViewModelFactory).get(LastMessageActivityViewModel::class.java)

        lastMessageActivityViewModel.getUsers()
        lastMessageActivityViewModel.options.observe(this, Observer {
            lastMessageAdapter = LastMessageAdapter(it, LastMessageAdapter.OnClickListener { name: String,peeredEmail: String,chatId: String,receiverID: String ->

                val intent = Intent(this, ChatActivity::class.java)
                intent.putExtra("CHAT_ID", chatId)
                intent.putExtra("receiverID", receiverID)
                intent.putExtra("Name", name)
                intent.putExtra("Email", peeredEmail)
                startActivity(intent)


            })
            lastMessageAdapter.registerAdapterDataObserver(object : AdapterDataObserver() {
                override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                    recyclerView.scrollToPosition(lastMessageAdapter.itemCount - 1)
                }
            })
            recyclerView.adapter = lastMessageAdapter
            lastMessageAdapter.startListening()
        })

    }

}