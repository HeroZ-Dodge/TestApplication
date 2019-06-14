package com.dodge.testapplication

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_main_1.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_1)
        initView()


    }

    private fun initView() {
        my_scroll_layout.recyclerView0.layoutManager = LinearLayoutManager(this)
        my_scroll_layout.recyclerView0.adapter = MyAdapter(this, getData(10))



        my_scroll_layout.recyclerView.layoutManager = LinearLayoutManager(this)
        my_scroll_layout.recyclerView.adapter = MyAdapter(this, getData(50))
    }

    private fun getData(size: Int): List<String> {
        val data = ArrayList<String>()
        for (i in 1..size) {
            data.add("index = $i")
        }
        return data
    }


}
