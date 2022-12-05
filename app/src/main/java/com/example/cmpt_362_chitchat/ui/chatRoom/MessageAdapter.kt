package com.example.cmpt_362_chitchat.ui.chatRoom

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import com.example.cmpt_362_chitchat.R
import com.example.cmpt_362_chitchat.data.Message
import com.google.firebase.storage.FirebaseStorage
import java.io.File

class MessageAdapter(
    val context: Context,
    private val messageList: ArrayList<Message>,
    private val sender: String,
    private var participants: HashMap<String, String>): RecyclerView.Adapter<ViewHolder>() {

    private val SEND = 0
    private val SEND_IMAGE = 1
    private val RECEIVE = 2
    private val RECEIVE_IMAGE = 3

    class SendViewHolder(itemView: View): ViewHolder(itemView) {
        val user: TextView = itemView.findViewById(R.id.username)
        val sentMessage: TextView = itemView.findViewById(R.id.sent_message)
    }

    class ImageSendViewHolder(itemView: View): ViewHolder(itemView) {
        val user: TextView = itemView.findViewById(R.id.username)
        val sentImage: ImageView = itemView.findViewById(R.id.sent_image)
        val sentMessage: TextView = itemView.findViewById(R.id.sent_message)
    }

    class ReceiveViewHolder(itemView: View): ViewHolder(itemView) {
        val user: TextView = itemView.findViewById(R.id.username)
        val receivedMessage: TextView = itemView.findViewById(R.id.received_message)
    }
    class ImageReceiveViewHolder(itemView: View): ViewHolder(itemView) {
        val user: TextView = itemView.findViewById(R.id.username)
        val receivedImage: ImageView = itemView.findViewById(R.id.received_image)
        val receivedMessage: TextView = itemView.findViewById(R.id.received_message)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        return when (viewType) {
            SEND -> {
                val view = LayoutInflater.from(context).inflate(R.layout.layout_sent_message, parent, false)
                SendViewHolder(view)
            }
            SEND_IMAGE -> {
                val view = LayoutInflater.from(context).inflate(R.layout.sent_image, parent, false)
                ImageSendViewHolder(view)
            }
            RECEIVE -> {
                val view = LayoutInflater.from(context).inflate(R.layout.layout_receive_message, parent, false)
                ReceiveViewHolder(view)
            }
            else -> {
                val view = LayoutInflater.from(context).inflate(R.layout.received_image, parent, false)
                ImageReceiveViewHolder(view)
            }
        }

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val viewHolder: ViewHolder?
        val message = messageList[position]

        when (holder.javaClass) {
            SendViewHolder::class.java -> {
                viewHolder = holder as SendViewHolder
                viewHolder.user.text = participants[message.sendID]
                viewHolder.sentMessage.text = message.message
            }
            ImageSendViewHolder::class.java -> {
                viewHolder = holder as ImageSendViewHolder
                viewHolder.user.text = participants[message.sendID]
                getImageFromDatabase(message.sendID.toString(), message.imageName.toString(), viewHolder.sentImage)
                if (message.message == "")
                    viewHolder.sentMessage.visibility = View.GONE
                else
                    viewHolder.sentMessage.text = message.message
            }
            ImageReceiveViewHolder::class.java -> {
                viewHolder = holder as ImageReceiveViewHolder
                viewHolder.user.text = participants[message.sendID]
                getImageFromDatabase(message.sendID.toString(), message.imageName.toString(), viewHolder.receivedImage)
                if (message.message == "")
                    viewHolder.receivedMessage.visibility = View.GONE
                else
                    viewHolder.receivedMessage.text = message.message
            }
            else -> {
                viewHolder = holder as ReceiveViewHolder
                viewHolder.user.text = participants[message.sendID]
                viewHolder.receivedMessage.text = message.message
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        val message = messageList[position]
        val imageName = message.imageName
        return if (sender == message.sendID) {
            if(imageName == "") {
                SEND
            }
            else {
                SEND_IMAGE
            }
        } else {
            if(imageName == "") {
                RECEIVE
            }
            else {
                RECEIVE_IMAGE
            }
        }
    }

    override fun getItemCount(): Int {
        return messageList.size
    }

    private fun getImageFromDatabase(sendUid: String, imageName: String, imageView: ImageView) {
        val storageRef = FirebaseStorage.getInstance().reference.child("SentImages/$sendUid/$imageName")
        val type = imageName.split(".")[1]
        val localFile = File.createTempFile(imageName, type)
        storageRef.getFile(localFile).addOnSuccessListener {
            val uri = Uri.fromFile(localFile)

            Glide.with(context).load(uri).into(imageView)
            println("DEBUG: photo successfully loaded")
        }.addOnFailureListener {
            println("DEBUG: photo was not able to load")
        }
    }

    fun replace(newParticipants: HashMap<String, String>?) {
        if (newParticipants != null) {
            participants = newParticipants
            notifyDataSetChanged()
        }
    }

}