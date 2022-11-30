package com.example.cmpt_362_chitchat.ui.profile

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.example.cmpt_362_chitchat.R


class ProfileAdapter(var context: Context, private var descriptionA: Array<String>, private var dataA: Array<String>) : BaseAdapter(){
    override fun getCount(): Int {
        return descriptionA.size
    }

    override fun getItem(index: Int): Any {
        return descriptionA[index]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val rowView: View = View.inflate(context, R.layout.adapter_profile, null)

        val description = rowView.findViewById(R.id.textDescription) as TextView
        val data = rowView.findViewById(R.id.textData) as TextView

        description.text = descriptionA[position]
        data.text = dataA[position]

        return rowView
    }
}