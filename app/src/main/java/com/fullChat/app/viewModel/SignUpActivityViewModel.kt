package com.fullChat.app.viewModel

import android.app.Application
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import com.fullChat.app.models.RegisterDeviceModel
import com.fullChat.app.models.UsersModel
import com.fullChat.app.netWork.ApiClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SignUpActivityViewModel(private val application: Application) : ViewModel() {



    private val _signUpSuccessful = MutableLiveData<String>()
    private var firebaseAuth: FirebaseAuth  = FirebaseAuth.getInstance()
    private var db = FirebaseFirestore.getInstance()
    private var userref = db.collection("users")



    val signUpSuccessful: LiveData<String>
        get() = _signUpSuccessful


     fun signUp(firstName:String,lastName:String,email:String, password:String) {

         firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener {task ->
                     if (!task.isSuccessful) {
                         Toast.makeText(application,task.exception!!.localizedMessage, Toast.LENGTH_SHORT).show()
                     } else {
                         Toast.makeText(application, "Signed up Success, Please login", Toast.LENGTH_SHORT).show()
                         userProfile(firstName,lastName,email)
                     }

            }
         }


    private fun userProfile(firstName: String,lastName:String, email: String) {
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {

            val profileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName("$firstName $lastName")
                .build()
            user.updateProfile(profileUpdates).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("TESTING", "User profile updated.")
                    FirebaseMessaging.getInstance().token.addOnSuccessListener{ instanceIdResult ->

                        ApiClient().getINSTANCE()?.registerDevice(email, instanceIdResult)?.enqueue(object : Callback<RegisterDeviceModel> {
                            override fun onResponse(call: Call<RegisterDeviceModel>, response: Response<RegisterDeviceModel>) {

                                Toast.makeText(application,response.body()!!.message,Toast.LENGTH_LONG).show()
                                _signUpSuccessful.value = "true"
                                //  finish()

                            }

                            override fun onFailure(call: Call<RegisterDeviceModel>, t: Throwable) {
                                Toast.makeText(application,t.message.toString(),Toast.LENGTH_LONG).show()
                            }
                        })
                    }
                }

            }
            val person = UsersModel("$firstName $lastName", email, user.uid, "offline",false,
                callCanceled = false
            )
            userref.add(person)
        }
    }


    }



