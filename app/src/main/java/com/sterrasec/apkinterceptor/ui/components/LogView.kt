package com.sterrasec.apkinterceptor.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.sterrasec.apkinterceptor.log.LogEntry
import com.sterrasec.apkinterceptor.log.LogEntryType
import java.text.DateFormat
import java.util.Date

@Composable
fun LogView(
    entries: List<LogEntry>,
    modifier: Modifier = Modifier,
    filter: ((LogEntry) -> Boolean)? = null,
    onClear: () -> Unit,
) {
    val visibleEntries = entries.filter { filter?.invoke(it) ?: true }
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text("Assessment Log", style = MaterialTheme.typography.titleMedium)
            Button(onClick = onClear) {
                Text("Clear")
            }
        }
        if (visibleEntries.isEmpty()) {
            Text("No log entries.")
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(visibleEntries, key = { it.id }) { entry ->
                    LogEntryCard(entry)
                }
            }
        }
    }
}

@Composable
private fun LogEntryCard(entry: LogEntry) {
    var expanded by remember(entry.id) { mutableStateOf(false) }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = entry.type.name,
                color = entryColor(entry.type),
                style = MaterialTheme.typography.labelMedium,
            )
            Text(
                text = DateFormat.getDateTimeInstance().format(Date(entry.timestamp)),
                style = MaterialTheme.typography.labelSmall,
            )
            Text(entry.title)
            if (expanded) {
                Text(entry.detail, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

private fun entryColor(type: LogEntryType): Color = when (type) {
    LogEntryType.SENT -> Color(0xFF1565C0)
    LogEntryType.RECEIVED -> Color(0xFF2E7D32)
    LogEntryType.BRIDGE_RESULT -> Color(0xFF6A1B9A)
    LogEntryType.ERROR -> Color(0xFFC62828)
}
