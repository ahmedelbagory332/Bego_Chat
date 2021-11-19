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
import com.fullChat.app.models.LastMessageModel
import com.fullChat.app.models.UsersModel

@SuppressLint("SimpleDateFormat")
class LastMessageAdapter(options: FirestoreRecyclerOptions<LastMessageModel>, val onClickListener: OnClickListener) : FirestoreRecyclerAdapter<LastMessageModel, LastMessageAdapter.ViewHolder>(options) {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.last_message_row, parent, false)

        return ViewHolder(
            view
        )
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int, model: LastMessageModel) {
        holder.name.text = model.Name + " : "
        when {
            model.message.equals("image") -> holder.message.text = "sent you image"
            model.message.equals("gif") -> holder.message.text = "sent you gif"
            model.message.equals("video") -> holder.message.text = "sent you video"
            model.message.equals("document") -> holder.message.text = "sent you document"
            model.message.equals("pdf") -> holder.message.text = "sent you pdf"
            model.message.equals("audio") -> holder.message.text = "sent you audio file"
            model.message.equals("record") -> holder.message.text = "sent you voice message"
            else -> holder.message.text = model.message
        }

        holder.itemView.setOnClickListener {
            onClickListener.clickListener(model.Name.toString(),model.peeredEmail.toString(),model.chatId.toString(),model.receiverID.toString())
        }
    }



    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var name: TextView = itemView.findViewById(R.id.name_)
        var message: TextView = itemView.findViewById(R.id.message_)

    }

    class OnClickListener(val clickListener: (name: String,peeredEmail: String,chatId: String,receiverID: String) -> Unit) {
    }


}