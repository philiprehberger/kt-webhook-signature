package com.philiprehberger.webhooksignature

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class WebhookSignerTest {

    private val secret = "test-secret-key"
    private val signer = WebhookSigner(secret, HmacAlgorithm.SHA256)

    @Test
    fun `sign produces hex string`() {
        val signature = signer.sign("payload", 1000L)
        assertTrue(signature.isNotEmpty())
        assertTrue(signature.all { it in '0'..'9' || it in 'a'..'f' })
    }

    @Test
    fun `sign is deterministic`() {
        val sig1 = signer.sign("payload", 1000L)
        val sig2 = signer.sign("payload", 1000L)
        assertEquals(sig1, sig2)
    }

    @Test
    fun `verify round-trip succeeds`() {
        val now = System.currentTimeMillis() / 1000
        val signature = signer.sign("hello world", now)
        assertTrue(signer.verify("hello world", signature, now))
    }

    @Test
    fun `verify detects tampered payload`() {
        val now = System.currentTimeMillis() / 1000
        val signature = signer.sign("original payload", now)
        assertFalse(signer.verify("tampered payload", signature, now))
    }

    @Test
    fun `verify detects tampered signature`() {
        val now = System.currentTimeMillis() / 1000
        val signature = signer.sign("payload", now)
        val tampered = signature.replaceRange(0, 4, "0000")
        assertFalse(signer.verify("payload", tampered, now))
    }

    @Test
    fun `verify rejects expired timestamp`() {
        val oldTimestamp = (System.currentTimeMillis() / 1000) - 600 // 10 minutes ago
        val signature = signer.sign("payload", oldTimestamp)
        assertFalse(signer.verify("payload", signature, oldTimestamp, tolerance = 5.minutes))
    }

    @Test
    fun `verify accepts timestamp within tolerance`() {
        val now = System.currentTimeMillis() / 1000
        val signature = signer.sign("payload", now)
        assertTrue(signer.verify("payload", signature, now, tolerance = 5.minutes))
    }

    @Test
    fun `verify with zero tolerance skips timestamp check`() {
        val oldTimestamp = 1L // Very old
        val signature = signer.sign("payload", oldTimestamp)
        assertTrue(signer.verify("payload", signature, oldTimestamp, tolerance = Duration.ZERO))
    }

    @Test
    fun `different secrets produce different signatures`() {
        val signer1 = WebhookSigner("secret1")
        val signer2 = WebhookSigner("secret2")
        val sig1 = signer1.sign("payload", 1000L)
        val sig2 = signer2.sign("payload", 1000L)
        assertNotEquals(sig1, sig2)
    }

    @Test
    fun `different algorithms produce different signatures`() {
        val sha256 = WebhookSigner(secret, HmacAlgorithm.SHA256)
        val sha512 = WebhookSigner(secret, HmacAlgorithm.SHA512)
        val sig256 = sha256.sign("payload", 1000L)
        val sig512 = sha512.sign("payload", 1000L)
        assertNotEquals(sig256, sig512)
    }

    @Test
    fun `stripe factory creates SHA256 signer`() {
        val stripeSigner = WebhookSigner.stripe("whsec_test")
        val now = System.currentTimeMillis() / 1000
        val sig = stripeSigner.sign("payload", now)
        assertTrue(stripeSigner.verify("payload", sig, now))
    }

    @Test
    fun `github factory creates SHA256 signer`() {
        val ghSigner = WebhookSigner.github("gh-secret")
        val now = System.currentTimeMillis() / 1000
        val sig = ghSigner.sign("payload", now)
        assertTrue(ghSigner.verify("payload", sig, now))
    }

    @Test
    fun `timing-safe comparison works for equal arrays`() {
        val a = "hello".toByteArray()
        val b = "hello".toByteArray()
        assertTrue(timingSafeEquals(a, b))
    }

    @Test
    fun `timing-safe comparison works for different arrays`() {
        val a = "hello".toByteArray()
        val b = "world".toByteArray()
        assertFalse(timingSafeEquals(a, b))
    }

    @Test
    fun `parseStripeSignatureHeader parses valid header`() {
        val result = parseStripeSignatureHeader("t=1234567890,v1=abc123")
        assertNotNull(result)
        assertEquals(1234567890L, result.first)
        assertEquals("abc123", result.second)
    }

    @Test
    fun `parseStripeSignatureHeader returns null for invalid header`() {
        assertNull(parseStripeSignatureHeader("invalid"))
    }

    @Test
    fun `parseGithubSignatureHeader parses valid header`() {
        val result = parseGithubSignatureHeader("sha256=abc123def456")
        assertEquals("abc123def456", result)
    }

    @Test
    fun `parseGithubSignatureHeader returns null for invalid header`() {
        assertNull(parseGithubSignatureHeader("sha1=abc123"))
    }
}
