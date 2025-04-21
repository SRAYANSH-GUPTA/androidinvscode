package com.example.androidinvs

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class SecondActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_second)

        // Set up the crash button
        findViewById<Button>(R.id.btnCrash).setOnClickListener {
            // Simulate a crash
            throw RuntimeException("This is a simulated crash from")
        }
    }
}
