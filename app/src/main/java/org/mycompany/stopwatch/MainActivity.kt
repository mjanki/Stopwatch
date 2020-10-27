package org.mycompany.stopwatch

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    lateinit var mainVM: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mainVM = ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(application))[MainViewModel::class.java]

        mainVM.secondsLiveData.observe(
                this,
                {
                    tvStopWatch.text = it.toString()
                }
        )

        mainVM.isStartedLiveData.observe(
                this,
                {
                    bStartPause.text = if (it) { "Pause" } else { "Start" }
                }
        )

        bStartPause.setOnClickListener {
            mainVM.handleStartPause()
        }

        bRestart.setOnClickListener {
            mainVM.handleRestart()
        }
    }
}