package com.philiprehberger.webhooksignature

/**
 * Parses a Stripe-formatted webhook signature header.
 *
 * Stripe signatures use the format: `"t=<timestamp>,v1=<signature>"`.
 *
 * @param header the `Stripe-Signature` header value
 * @return a pair of (timestamp, signature), or `null` if the header format is invalid
 */
fun parseStripeSignatureHeader(header: String): Pair<Long, String>? {
    val parts = header.split(",").associate { part ->
        val (key, value) = part.split("=", limit = 2).takeIf { it.size == 2 } ?: return null
        key to value
    }
    val timestamp = parts["t"]?.toLongOrNull() ?: return null
    val signature = parts["v1"] ?: return null
    return timestamp to signature
}

/**
 * Parses a GitHub-formatted webhook signature header.
 *
 * GitHub signatures use the format: `"sha256=<hex-signature>"`.
 *
 * @param header the `X-Hub-Signature-256` header value
 * @return the hex-encoded signature string, or `null` if the header format is invalid
 */
fun parseGithubSignatureHeader(header: String): String? {
    val prefix = "sha256="
    if (!header.startsWith(prefix)) return null
    return header.removePrefix(prefix)
}
