package com.fullChat.app.activities

import android.Manifest
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.fullChat.app.BuildConfig
import com.fullChat.app.R
import com.fullChat.app.viewModel.MainActivityViewModel
import com.fullChat.app.viewModel.viewModelFactory.MainActivityViewModelFactory
import com.google.firebase.auth.FirebaseAuth
import java.io.File


class MainActivity : AppCompatActivity() {

    lateinit  var email: EditText
    lateinit  var password:EditText
    private lateinit  var btLogin:Button
    private lateinit  var btSignup:Button
    lateinit  var firebaseAuth: FirebaseAuth
    lateinit var mainActivityViewModel: MainActivityViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val user = FirebaseAuth.getInstance().currentUser
        if (user !=null) startActivity(Intent(this, UsersActivity::class.java))

        email = findViewById(R.id.username)
        password = findViewById(R.id.password)
        btLogin = findViewById(R.id.login)
        btSignup = findViewById(R.id.signup)
        firebaseAuth = FirebaseAuth.getInstance()

        val application = requireNotNull(this).application
        val mainActivityViewModelFactory = MainActivityViewModelFactory(application)
        mainActivityViewModel = ViewModelProvider(this,mainActivityViewModelFactory).get(MainActivityViewModel::class.java)


        btLogin.setOnClickListener{

            if (email.text.toString().trim().isEmpty()) {
                Toast.makeText(this, "Type Email", Toast.LENGTH_SHORT).show()
            }else if (!Patterns.EMAIL_ADDRESS.matcher(email.text.toString().trim()).matches()) {
                Toast.makeText(this, "Please enter a valid email address", Toast.LENGTH_SHORT).show()
            }else if (password.text.toString().trim().isEmpty()) {
                Toast.makeText(this, "Type Password", Toast.LENGTH_SHORT).show()
            } else{
                mainActivityViewModel.signIn(email.text.toString().trim(),password.text.toString().trim())
            }

        }



        mainActivityViewModel.signInSuccessful.observe(this, Observer {
            if (it == "true"){

                val i = Intent(this, UsersActivity::class.java)
                startActivity(i)
                finish()
            }
        })


        btSignup.setOnClickListener {
            val i = Intent(this, SignUpActivity::class.java)
            startActivity(i)
            finish()

        }





    }






    }




