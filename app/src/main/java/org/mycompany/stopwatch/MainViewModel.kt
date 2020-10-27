package org.mycompany.stopwatch

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.*

class MainViewModel(application: Application): AndroidViewModel(application) {
    private val sharedPreferences = application.getSharedPreferences("APP_WIDE", 0)
    private val stopWatch = StopWatch()

    val secondsLiveData = stopWatch.getCurrentValueFlow().asLiveData(Dispatchers.IO)

    val isStartedLiveData = liveData {
        stopWatch.getStatusFlow().collectLatest {
            emit(it is Status.Started)
        }
    }

    private var job: Job? = null

    init {
        viewModelScope.launch(Dispatchers.IO) {
            stopWatch.getStatusFlow().collect {
                when (it) {
                    is Status.New -> handleNew()
                    is Status.Paused -> handlePaused()
                    is Status.Started -> handleStarted()
                }
            }
        }
    }

    fun handleStartPause() {
        if (stopWatch.getStatusFlow().value is Status.Started) {
            stopWatch.pause()
        } else {
            stopWatch.start()
        }
    }

    fun handleRestart() {
        stopWatch.pause()
        stopWatch.setValue(0)
        deleteData()
    }

    private fun handleNew() {
        val value = sharedPreferences.getLong("value", stopWatch.getCurrentValueFlow().value)
        val isRunning = sharedPreferences.getBoolean("running", false)
        val lastUpdated = sharedPreferences.getLong("lastUpdated", 0)

        if (stopWatch.getCurrentValueFlow().value == 0L && value != 0L && lastUpdated != 0L) {
            val offset = if (isRunning) { (Date().time - lastUpdated) / 1000 } else { 0 }
            stopWatch.setValue(value + offset)
        }

        if (isRunning) {
            stopWatch.start()
        } else {
            stopWatch.pause()
        }
    }

    private fun handleStarted() {
        if (job == null) {
            stopWatch.start()
            saveIsRunning()

            job = viewModelScope.launch(Dispatchers.IO) {
                while (stopWatch.getStatusFlow().value is Status.Started) {
                    stopWatch.incrementValue()
                    saveValueAndTime()

                    delay(1000)
                }
            }
        }
    }

    private fun handlePaused() {
        job?.cancel()
        job = null
        saveIsRunning()
    }

    private fun saveIsRunning() {
        val editor = sharedPreferences.edit()
        editor.putBoolean("running", stopWatch.getStatusFlow().value is Status.Started)
        editor.apply()
    }

    private fun saveValueAndTime() {
        val editor = sharedPreferences.edit()
        editor.putLong("value", stopWatch.getCurrentValueFlow().value)
        editor.putLong("lastUpdated", Date().time)
        editor.apply()
    }

    private fun deleteData() {
        val editor = sharedPreferences.edit()
        editor.remove("running")
        editor.remove("value")
        editor.remove("lastUpdated")
        editor.apply()
    }
}