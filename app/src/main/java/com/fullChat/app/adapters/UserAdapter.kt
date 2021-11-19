package com.fullChat.app.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.fullChat.app.R
import com.fullChat.app.models.UsersModel

@SuppressLint("SimpleDateFormat")
class UserAdapter(options: FirestoreRecyclerOptions<UsersModel>, val onClickListener: OnClickListener) : FirestoreRecyclerAdapter<UsersModel, UserAdapter.ViewHolder>(options) {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.user_row, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int, model: UsersModel) {
        holder.name.text = model.name
        holder.mail.text = model.email
        holder.itemView.setOnClickListener {
            onClickListener.clickListener(model.userId.toString(),model.name.toString(),model.email.toString())
        }
    }



    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var name: TextView = itemView.findViewById(R.id.name_)
        var mail: TextView = itemView.findViewById(R.id.email_)

    }

    class OnClickListener(val clickListener: (id: String,name: String,email: String) -> Unit) {
    }


}