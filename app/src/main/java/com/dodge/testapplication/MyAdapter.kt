package com.dodge.testapplication

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

/**
 *  Created by linzheng on 2019/6/12.
 */

class MyAdapter(val context: Context, val data: List<String>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.item_layout, p0, false)
        return MyViewHolder(view)
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(p0: RecyclerView.ViewHolder, p1: Int) {


        println("adapter index = $p1")
    }


    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {


        fun bindView() {


        }

    }


}



