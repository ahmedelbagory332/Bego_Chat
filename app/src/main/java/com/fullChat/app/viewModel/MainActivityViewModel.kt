package com.fullChat.app.viewModel

import android.app.Application
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging
import com.fullChat.app.activities.UsersActivity
import com.fullChat.app.models.UpdateModel
import com.fullChat.app.netWork.ApiClient
import com.fullChat.app.utiles.UserData
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivityViewModel(private val application: Application) : ViewModel() {


    private val _signInSuccessful = MutableLiveData<String>()
    private var firebaseAuth: FirebaseAuth  = FirebaseAuth.getInstance()



    val signInSuccessful: LiveData<String>
        get() = _signInSuccessful


     fun signIn(email:String, password:String) {
         UserData(application).writeToken("")

         firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener {task ->
            if (!task.isSuccessful) {
                Log.w("TESTING", "signInWithEmail:failed", task.exception)
                Toast.makeText(application, task.exception!!.localizedMessage, Toast.LENGTH_LONG).show()
            } else {

                    Log.d("TESTING", "sign In Successful:" + task.isSuccessful)
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

                _signInSuccessful.value = "true"
                }
            }
         }




    }



