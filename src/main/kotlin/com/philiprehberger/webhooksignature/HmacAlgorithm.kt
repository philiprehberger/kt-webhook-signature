package com.philiprehberger.webhooksignature

/**
 * Supported HMAC algorithms for webhook signature creation and verification.
 *
 * @property javaName the Java Cryptography Architecture standard name for this algorithm
 */
enum class HmacAlgorithm(val javaName: String) {
    /** HMAC with SHA-1 digest */
    SHA1("HmacSHA1"),
    /** HMAC with SHA-256 digest */
    SHA256("HmacSHA256"),
    /** HMAC with SHA-512 digest */
    SHA512("HmacSHA512"),
}
