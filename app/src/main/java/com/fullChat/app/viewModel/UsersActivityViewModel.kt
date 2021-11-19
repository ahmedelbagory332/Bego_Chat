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
import com.fullChat.app.models.UpdateModel
import com.fullChat.app.models.UsersModel
import com.fullChat.app.netWork.ApiClient
import com.fullChat.app.utiles.UserData
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class UsersActivityViewModel(private val application: Application) : ViewModel() {



    private val _options = MutableLiveData<FirestoreRecyclerOptions<UsersModel>>()
    private val user = FirebaseAuth.getInstance().currentUser



    val options: LiveData<FirestoreRecyclerOptions<UsersModel>>
        get() = _options




     fun getUsers(){
         val query:Query = FirebaseFirestore.getInstance().collection("users")
         _options.value = FirestoreRecyclerOptions.Builder<UsersModel>().setQuery(query, UsersModel::class.java).build()
     }

    fun refreshToken(){
        if (user!=null){
            FirebaseMessaging.getInstance().token.addOnSuccessListener { instanceIdResult ->
                if (!UserData(application).readToken().equals(instanceIdResult)) {

                    UserData(application).writeToken(instanceIdResult)

                    ApiClient().getINSTANCE()?.updateToken(FirebaseAuth.getInstance().currentUser!!.email!!, instanceIdResult)?.enqueue(object :
                        Callback<UpdateModel> {
                        override fun onResponse(call: Call<UpdateModel>, response: Response<UpdateModel>) {

                            // if (response.body()!!.response == "0")Toast.makeText(applicationContext,"updated", Toast.LENGTH_LONG).show()

                        }

                        override fun onFailure(call: Call<UpdateModel>, t: Throwable) {
                            Toast.makeText(application, "error refreshToken\n"+t.message.toString(), Toast.LENGTH_LONG).show()
                        }
                    })
                }
            }

        }

    }

    }



