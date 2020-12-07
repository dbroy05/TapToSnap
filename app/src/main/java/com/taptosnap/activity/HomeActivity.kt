package com.taptosnap.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.taptosnap.R
import com.taptosnap.viewmodel.SnapViewModel
import kotlinx.android.synthetic.main.activity_home.*


class HomeActivity : AppCompatActivity() {
    private val mViewModel: SnapViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        start_button.setOnClickListener {
            progressbar.visibility = View.VISIBLE
            mViewModel.getSpanItems().observe(this){
                it.let {
                    progressbar.visibility = View.GONE
                    startActivity(Intent(this,MainActivity::class.java))
                }
            }
        }


    }
}