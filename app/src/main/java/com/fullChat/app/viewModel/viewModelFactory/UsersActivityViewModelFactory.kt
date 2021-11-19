package com.fullChat.app.viewModel.viewModelFactory

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.fullChat.app.viewModel.SignUpActivityViewModel
import com.fullChat.app.viewModel.UsersActivityViewModel

@Suppress("UNCHECKED_CAST")
class UsersActivityViewModelFactory(private val application: Application) : ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UsersActivityViewModel::class.java)) {
            return UsersActivityViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }


}