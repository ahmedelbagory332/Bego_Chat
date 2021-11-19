package com.fullChat.app.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import com.fullChat.app.netWork.ApiClient
import com.fullChat.app.R
import com.fullChat.app.utiles.UserData
import com.fullChat.app.models.RegisterDeviceModel
import com.fullChat.app.models.UsersModel
import com.fullChat.app.viewModel.MainActivityViewModel
import com.fullChat.app.viewModel.SignUpActivityViewModel
import com.fullChat.app.viewModel.viewModelFactory.MainActivityViewModelFactory
import com.fullChat.app.viewModel.viewModelFactory.SignUpActivityViewModelFactory
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


@Suppress("NAME_SHADOWING")
class SignUpActivity : AppCompatActivity(), View.OnClickListener {

    lateinit  var firstName: EditText
    lateinit  var lastName:EditText
    lateinit  var email:EditText
    lateinit  var password:EditText
    lateinit  var confirmPassword:EditText
    lateinit  var signUp: Button
    lateinit  var cancel:Button
    lateinit var signUpActivityViewModel: SignUpActivityViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        firstName=findViewById(R.id.editTextFirstname)
        lastName=findViewById(R.id.editTextLastname)
        email=findViewById(R.id.editTextEmail)
        password=findViewById(R.id.editTextPassword)
        confirmPassword=findViewById(R.id.editTextConfirmPassword)

        cancel=findViewById(R.id.buttonCancel)
        signUp=findViewById(R.id.buttonSignUp)


        signUp.setOnClickListener(this)
        cancel.setOnClickListener(this)

        val application = requireNotNull(this).application
        val signUpActivityViewModelFactory = SignUpActivityViewModelFactory(application)
        signUpActivityViewModel = ViewModelProvider(this,signUpActivityViewModelFactory).get(SignUpActivityViewModel::class.java)


        signUpActivityViewModel.signUpSuccessful.observe(this, Observer {
            if (it == "true"){
                startActivity(Intent(this, UsersActivity::class.java))
                finish()
            }
        })


    }

    override fun onClick(view: View) {
        if (view.id == R.id.buttonCancel) {
            finish()
        } else {

            when {
                firstName.text.toString().trim().isEmpty() -> {
                    Toast.makeText(this, "check your data", Toast.LENGTH_SHORT).show()
                }
                lastName.text.toString().trim().isEmpty() -> {
                    Toast.makeText(this, "check your data", Toast.LENGTH_SHORT).show()
                }
                email.text.toString().trim().isEmpty() -> {
                    Toast.makeText(this, "check your data", Toast.LENGTH_SHORT).show()
                }
                !Patterns.EMAIL_ADDRESS.matcher(email.text.toString().trim()).matches() -> {
                    Toast.makeText(this, "Please enter a valid email address", Toast.LENGTH_SHORT).show()
                }
                password.text.toString().trim().length<6 -> {
                    Toast.makeText(this, "Password should consist of minimum 6 characters", Toast.LENGTH_SHORT).show()
                }
                confirmPassword.text.toString().trim()!=password.text.toString().trim() -> {
                    Toast.makeText(this, "Passwords Do not match ", Toast.LENGTH_SHORT).show()
                }
                else -> {
                    signUpActivityViewModel.signUp(firstName.text.toString().trim(),lastName.text.toString().trim(),email.text.toString().trim(),password.text.toString().trim())
                }
            }
        }
    }


}