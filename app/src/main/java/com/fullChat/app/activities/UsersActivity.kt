package com.fullChat.app.activities

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
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
import com.google.firebase.messaging.FirebaseMessaging
import com.fullChat.app.R
import com.fullChat.app.adapters.UserAdapter
import com.fullChat.app.models.UpdateModel
import com.fullChat.app.models.UsersModel
import com.fullChat.app.netWork.ApiClient
import com.fullChat.app.utiles.UserData
import com.fullChat.app.viewModel.SignUpActivityViewModel
import com.fullChat.app.viewModel.UsersActivityViewModel
import com.fullChat.app.viewModel.viewModelFactory.SignUpActivityViewModelFactory
import com.fullChat.app.viewModel.viewModelFactory.UsersActivityViewModelFactory
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class UsersActivity : AppCompatActivity() {

    lateinit var recyclerView: RecyclerView
    lateinit var linearLayoutManager: LinearLayoutManager
    lateinit var userAdapter: UserAdapter
    var chatId : String = ""
    lateinit var usersActivityViewModel: UsersActivityViewModel
    private val user = FirebaseAuth.getInstance().currentUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_users)



        recyclerView = findViewById(R.id.userrecyclerview)
        linearLayoutManager =  LinearLayoutManager(applicationContext)
        recyclerView.layoutManager = linearLayoutManager

        val application = requireNotNull(this).application
        val usersActivityViewModelFactory = UsersActivityViewModelFactory(application)
        usersActivityViewModel = ViewModelProvider(this,usersActivityViewModelFactory).get(UsersActivityViewModel::class.java)


        usersActivityViewModel.refreshToken()
        usersActivityViewModel.getUsers()
        usersActivityViewModel.options.observe(this, Observer {
            userAdapter = UserAdapter(it, UserAdapter.OnClickListener { id: String, name: String, email: String ->

                if (id == user!!.uid.toString())
                    Toast.makeText(this, "you cant send message to yourself", Toast.LENGTH_LONG)
                        .show()
                else {
                    chatId = if (id.hashCode() > user.uid.hashCode())
                        "${id}-${user.uid}"
                    else
                        "${user.uid}-${id}"
                    val intent = Intent(this, ChatActivity::class.java)
                    intent.putExtra("CHAT_ID", chatId)
                    intent.putExtra("receiverID", id)
                    intent.putExtra("Name", name)
                    intent.putExtra("Email", email)
                    startActivity(intent)
                }

            })
             userAdapter.registerAdapterDataObserver(object : AdapterDataObserver() {
                override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                    recyclerView.scrollToPosition(userAdapter.itemCount - 1)
                }
            })


            recyclerView.adapter = userAdapter
            userAdapter.startListening()

        })

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_items, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        super.onOptionsItemSelected(item)
        when (item.title.toString()) {
            "logout" -> {
                UserData(application).writeToken("")
                ApiClient().getINSTANCE()
                    ?.updateToken(FirebaseAuth.getInstance().currentUser!!.email!!, "0")
                    ?.enqueue(object : Callback<UpdateModel> {
                        override fun onResponse(
                            call: Call<UpdateModel>,
                            response: Response<UpdateModel>
                        ) {

                          //  if (response.body()!!.response == "0")
//                                Toast.makeText(
//                                applicationContext,
//                                "updated",
//                                Toast.LENGTH_LONG
//                            ).show()

                        }

                        override fun onFailure(call: Call<UpdateModel>, t: Throwable) {
                            Toast.makeText(applicationContext, t.message.toString(), Toast.LENGTH_LONG)
                                .show()
                        }
                    })
                startActivity(Intent(this@UsersActivity, MainActivity::class.java))
                FirebaseAuth.getInstance().signOut()
                finish()
            }
            "all" -> {
                 val i = Intent(this@UsersActivity, LastMessageActivity::class.java)
                startActivity(i)
             }
        }
        return true
    }




}