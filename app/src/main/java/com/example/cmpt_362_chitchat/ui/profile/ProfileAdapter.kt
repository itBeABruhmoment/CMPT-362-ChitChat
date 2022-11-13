package com.example.cmpt_362_chitchat.ui.profile

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.example.cmpt_362_chitchat.R


class ProfileAdapter(private val context: Activity, private val descriptionA: Array<String>, private val dataA: Array<String>)
    : ArrayAdapter<String>(context, R.layout.adapter_profile, descriptionA) {

    override fun getView(position: Int, view: View?, parent: ViewGroup): View {
        val inflater = context.layoutInflater
        val rowView = inflater.inflate(R.layout.adapter_profile, null, true)

        val description = rowView.findViewById(R.id.textDescription) as TextView
        val data = rowView.findViewById(R.id.textData) as TextView

        description.text = descriptionA[position]
        data.text = dataA[position]

        return rowView
    }
}