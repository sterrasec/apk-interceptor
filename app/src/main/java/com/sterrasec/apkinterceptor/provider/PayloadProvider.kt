package com.sterrasec.apkinterceptor.provider

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import android.os.ParcelFileDescriptor
import java.io.File
import java.io.FileNotFoundException

class PayloadProvider : ContentProvider() {
    override fun onCreate(): Boolean = true

    override fun openFile(uri: Uri, mode: String): ParcelFileDescriptor {
        if (mode != "r") {
            throw FileNotFoundException("Read-only provider")
        }

        val encodedPath = uri.encodedPath ?: throw FileNotFoundException("No path")
        val path = uri.path ?: throw FileNotFoundException("No path")
        if (encodedPath != ALLOWED_PATH || path != ALLOWED_PATH) {
            throw FileNotFoundException("Rejected path")
        }

        val appContext = context ?: throw FileNotFoundException("No context")
        val payloadsDir = File(appContext.filesDir, PAYLOADS_DIR).canonicalFile
        val file = File(payloadsDir, PAYLOAD_FILE).canonicalFile
        if (file.parentFile != payloadsDir) {
            throw FileNotFoundException("Path traversal blocked")
        }
        if (!file.isFile) {
            throw FileNotFoundException("Payload not saved yet")
        }

        return ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
    }

    override fun getType(uri: Uri): String? {
        return if (uri.encodedPath == ALLOWED_PATH && uri.path == ALLOWED_PATH) {
            "text/html"
        } else {
            null
        }
    }

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?,
    ): Cursor? = null

    override fun insert(uri: Uri, values: ContentValues?): Uri? = null

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int = 0

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?,
    ): Int = 0

    companion object {
        const val PAYLOADS_DIR = "payloads"
        const val PAYLOAD_FILE = "current.html"
        const val ALLOWED_PATH = "/current.html"

        fun contentUri(packageName: String): Uri =
            Uri.parse("content://$packageName.payload$ALLOWED_PATH")
    }
}
