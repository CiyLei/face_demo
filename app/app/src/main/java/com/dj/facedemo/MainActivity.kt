package com.dj.facedemo

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.TextView

class MainActivity : AppCompatActivity() {
    companion object {
        var encode = ArrayList<Double>();
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        println(
            "--------- ${
                Settings.System.getString(
                    contentResolver,
                    Settings.Secure.ANDROID_ID
                )
            }"
        )
        findViewById<Button>(R.id.btnDetect).setOnClickListener {
            startActivity(Intent(this, DetectActivity::class.java))
        }
        findViewById<Button>(R.id.btnDistance).setOnClickListener {
            startActivity(Intent(this, DistanceActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        findViewById<TextView>(R.id.tvEncode).text = "人脸特征码：$encode"
    }
}