package com.sterrasec.apkinterceptor.log

import java.util.concurrent.atomic.AtomicLong
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

object LogRepository {
    private val idSequence = AtomicLong(System.currentTimeMillis())
    private val _logs = MutableStateFlow<List<LogEntry>>(emptyList())
    val logs: StateFlow<List<LogEntry>> = _logs.asStateFlow()

    internal fun nextId(): Long = idSequence.incrementAndGet()

    fun add(entry: LogEntry) {
        _logs.update { current -> listOf(entry) + current }
    }

    fun clear() {
        _logs.value = emptyList()
    }
}
