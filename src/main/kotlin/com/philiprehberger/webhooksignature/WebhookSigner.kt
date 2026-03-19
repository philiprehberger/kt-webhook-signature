package com.philiprehberger.webhooksignature

import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

/**
 * Creates and verifies HMAC-based webhook signatures.
 *
 * Supports SHA-1, SHA-256, and SHA-512 algorithms with timing-safe comparison
 * and optional timestamp tolerance for replay attack prevention.
 *
 * ```kotlin
 * val signer = WebhookSigner("my-secret", HmacAlgorithm.SHA256)
 * val timestamp = System.currentTimeMillis() / 1000
 * val signature = signer.sign("payload", timestamp)
 * val valid = signer.verify("payload", signature, timestamp)
 * ```
 *
 * @property secret the shared secret key
 * @property algorithm the HMAC algorithm to use (default: [HmacAlgorithm.SHA256])
 */
public class WebhookSigner(
    private val secret: String,
    private val algorithm: HmacAlgorithm = HmacAlgorithm.SHA256,
) {
    /**
     * Signs a [payload] with the given [timestamp] and returns the hex-encoded HMAC signature.
     *
     * The signed content is `"$timestamp.$payload"`, ensuring the timestamp is bound to the payload.
     *
     * @param payload the webhook payload body
     * @param timestamp the Unix timestamp in seconds
     * @return the hex-encoded HMAC signature
     */
    public fun sign(payload: String, timestamp: Long): String {
        val content = "$timestamp.$payload"
        val mac = Mac.getInstance(algorithm.javaName)
        mac.init(SecretKeySpec(secret.toByteArray(), algorithm.javaName))
        val hash = mac.doFinal(content.toByteArray())
        return hash.joinToString("") { "%02x".format(it) }
    }

    /**
     * Verifies a webhook [signature] against the given [payload] and [timestamp].
     *
     * Uses timing-safe comparison to prevent timing attacks. Optionally checks
     * that the [timestamp] is within [tolerance] of the current time.
     *
     * @param payload the webhook payload body
     * @param signature the hex-encoded HMAC signature to verify
     * @param timestamp the Unix timestamp in seconds
     * @param tolerance the maximum age of the timestamp; [Duration.ZERO] disables the check (default: 5 minutes)
     * @return `true` if the signature is valid and the timestamp is within tolerance
     */
    public fun verify(
        payload: String,
        signature: String,
        timestamp: Long,
        tolerance: Duration = 5.minutes,
    ): Boolean {
        // Check timestamp tolerance
        if (tolerance > Duration.ZERO) {
            val now = System.currentTimeMillis() / 1000
            val diff = kotlin.math.abs(now - timestamp)
            if (diff > tolerance.inWholeSeconds) {
                return false
            }
        }

        val expected = sign(payload, timestamp)
        return timingSafeEquals(expected.toByteArray(), signature.toByteArray())
    }

    public companion object {
        /**
         * Creates a [WebhookSigner] configured for Stripe webhook verification.
         *
         * Stripe uses HMAC-SHA256 with the `whsec_` prefixed secret.
         *
         * @param secret the Stripe webhook signing secret (including `whsec_` prefix)
         * @return a [WebhookSigner] configured for Stripe
         */
        public fun stripe(secret: String): WebhookSigner =
            WebhookSigner(secret, HmacAlgorithm.SHA256)

        /**
         * Creates a [WebhookSigner] configured for GitHub webhook verification.
         *
         * GitHub uses HMAC-SHA256 for webhook signatures.
         *
         * @param secret the GitHub webhook secret
         * @return a [WebhookSigner] configured for GitHub
         */
        public fun github(secret: String): WebhookSigner =
            WebhookSigner(secret, HmacAlgorithm.SHA256)
    }
}
