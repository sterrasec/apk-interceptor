package com.sterrasec.apkinterceptor.provider

import android.content.Context
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import java.io.File
import java.io.FileNotFoundException

@RunWith(RobolectricTestRunner::class)
class PayloadProviderTest {

    private lateinit var provider: PayloadProvider
    private lateinit var context: Context

    @Before
    fun setUp() {
        provider = Robolectric.setupContentProvider(PayloadProvider::class.java)
        context = ApplicationProvider.getApplicationContext()
    }

    private fun savePayload(content: String = "<html><body>ok</body></html>") {
        val dir = File(context.filesDir, PayloadProvider.PAYLOADS_DIR)
        dir.mkdirs()
        File(dir, PayloadProvider.PAYLOAD_FILE).writeText(content)
    }

    private fun uri(path: String): Uri = Uri.parse("content://${context.packageName}.payload$path")

    @Test
    fun servesWhitelistedPayloadWhenSaved() {
        savePayload()
        provider.openFile(uri("/current.html"), "r").use { pfd ->
            assertNotNull(pfd)
        }
    }

    @Test(expected = FileNotFoundException::class)
    fun rejectsOtherFileName() {
        savePayload()
        provider.openFile(uri("/secret.html"), "r")
    }

    @Test(expected = FileNotFoundException::class)
    fun rejectsParentTraversalPath() {
        savePayload()
        provider.openFile(uri("/current.html/../current.html"), "r")
    }

    @Test(expected = FileNotFoundException::class)
    fun rejectsEncodedTraversalPath() {
        savePayload()
        // %2e%2e decodes to "..": encodedPath must not match the whitelist either.
        provider.openFile(uri("/%2e%2e/current.html"), "r")
    }

    @Test(expected = FileNotFoundException::class)
    fun rejectsNonReadMode() {
        savePayload()
        provider.openFile(uri("/current.html"), "w")
    }

    @Test(expected = FileNotFoundException::class)
    fun rejectsWhenPayloadNotSaved() {
        provider.openFile(uri("/current.html"), "r")
    }

    @Test
    fun getTypeReturnsHtmlForAllowedPathOnly() {
        assertEquals("text/html", provider.getType(uri("/current.html")))
        assertNull(provider.getType(uri("/secret.html")))
    }

    @Test
    fun contentUriUsesPackageAndAllowedPath() {
        assertEquals(
            "content://com.example.payload/current.html",
            PayloadProvider.contentUri("com.example").toString(),
        )
    }

    @Test
    fun unusedCrudOperationsAreNoOps() {
        assertNull(provider.query(uri("/current.html"), null, null, null, null))
        assertNull(provider.insert(uri("/current.html"), null))
        assertEquals(0, provider.delete(uri("/current.html"), null, null))
        assertEquals(0, provider.update(uri("/current.html"), null, null, null))
    }
}
