package com.sterrasec.apkinterceptor.log

enum class LogEntryType {
    SENT,
    RECEIVED,
    BRIDGE_RESULT,
    ERROR,
}

data class LogEntry(
    val type: LogEntryType,
    val title: String,
    val detail: String,
    val timestamp: Long = System.currentTimeMillis(),
    val id: Long = LogRepository.nextId(),
)
