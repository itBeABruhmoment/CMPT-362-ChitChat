package com.example.cmpt_362_chitchat.ui.chatRoom

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.cmpt_362_chitchat.R
import com.example.cmpt_362_chitchat.data.Message

class MessageAdapter(
    val context: Context,
    private val messageList: ArrayList<Message>,
    private val sender: String,
    private var participants: HashMap<String, String>): RecyclerView.Adapter<ViewHolder>() {

    private val SEND = 0
    private val RECEIVE = 1

    class SendViewHolder(itemView: View): ViewHolder(itemView) {
        val user: TextView = itemView.findViewById(R.id.username)
        val sentMessage: TextView = itemView.findViewById(R.id.sent_message)
    }

    class ReceiveViewHolder(itemView: View): ViewHolder(itemView) {
        val user: TextView = itemView.findViewById(R.id.username)
        val receiveMessage: TextView = itemView.findViewById(R.id.received_message)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        return if(viewType == SEND) {
            val view = LayoutInflater.from(context).inflate(R.layout.layout_sent_message, parent, false)
            SendViewHolder(view)
        } else {
            val view = LayoutInflater.from(context).inflate(R.layout.layout_receive_message, parent, false)
            ReceiveViewHolder(view)
        }

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val viewHolder: ViewHolder?
        val message = messageList[position]

        if (holder.javaClass == SendViewHolder::class.java) {
            viewHolder = holder as SendViewHolder
            viewHolder.user.text = participants[message.sendID]
            viewHolder.sentMessage.text = message.message
        }
        else {
            viewHolder = holder as ReceiveViewHolder
            viewHolder.user.text = participants[message.sendID]
            viewHolder.receiveMessage.text = message.message
        }
    }

    override fun getItemViewType(position: Int): Int {
        val message = messageList[position]
        return if (sender == message.sendID) {
            SEND
        } else {
            RECEIVE
        }
    }

    override fun getItemCount(): Int {
        return messageList.size
    }

    fun replace(newParticipants: HashMap<String, String>?) {
        if (newParticipants != null) {
            participants = newParticipants
            notifyDataSetChanged()
        }
    }

}