package com.example.bottomsheet

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

//        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)

        if (savedInstanceState == null)
            showBottomDialog()

        fragmentContainer.setOnClickListener {
            showBottomDialog()
        }
    }

    private fun showBottomDialog() {
        BottomDialog.newInstance().show(supportFragmentManager, "SDS")
    }
}
