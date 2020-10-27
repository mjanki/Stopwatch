package org.mycompany.stopwatch

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

sealed class Status {
    class New: Status()
    class Paused : Status()
    class Started : Status()
}

class StopWatch {
    private val statusFlow = MutableStateFlow<Status>(Status.New())
    fun getStatusFlow(): StateFlow<Status> = statusFlow

    private val currentValueFlow = MutableStateFlow(0L)
    fun getCurrentValueFlow(): StateFlow<Long> = currentValueFlow

    fun setValue(value: Long) {
        currentValueFlow.value = value
    }

    fun incrementValue() {
        currentValueFlow.value = currentValueFlow.value + 1
    }

    fun start() {
        statusFlow.value = Status.Started()
    }

    fun pause() {
        statusFlow.value = Status.Paused()
    }
}