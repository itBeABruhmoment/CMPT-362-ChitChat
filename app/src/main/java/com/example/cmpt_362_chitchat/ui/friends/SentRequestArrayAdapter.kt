package com.example.cmpt_362_chitchat.ui.friends


import android.app.Activity
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.example.cmpt_362_chitchat.R


class SentRequestArrayAdapter(
    var requests: ArrayList<FriendsActivityViewModel.Companion.FriendRequestEntry>,
    var activity: Activity,
    var viewModel: FriendsActivityViewModel
) : BaseAdapter(){
    override fun getCount(): Int {
        return requests.size
    }

    override fun getItem(index: Int): Any {
        return requests[index]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View = View.inflate(activity, R.layout.fragment_friend_sent_item, null)
        view.isClickable = true
        view.setOnClickListener {
            // get dimensions
            val displayMetrics = DisplayMetrics()
            activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics)
            val height: Int = (displayMetrics.heightPixels * 0.8).toInt()
            val width: Int = (displayMetrics.widthPixels * 0.9).toInt()

            // make dialogue
            val builder: AlertDialog.Builder = AlertDialog.Builder(activity)
            builder.setView(R.layout.fragment_view_profile);
            builder.setTitle("Friend Request");
            val alertDialog = builder.create();
            alertDialog.show();
            alertDialog.getWindow()?.setLayout(width, height);
        }
        view.findViewById<TextView>(R.id.fragment_friend_sent_item_name).text = requests[position].userName
        view.findViewById<ImageButton>(R.id.fragment_friend_sent_item_reject).setOnClickListener() {
            Log.i("FriendsActivity", "remove request click")
            viewModel.removeFriendRequest(requests[position].request)
        }
        return view
    }
}