package com.philiprehberger.webhooksignature

import java.security.MessageDigest

/**
 * Compares two byte arrays in constant time to prevent timing attacks.
 *
 * Uses [MessageDigest.isEqual] which is guaranteed to compare in constant time.
 *
 * @param a the first byte array
 * @param b the second byte array
 * @return `true` if the arrays are equal, `false` otherwise
 */
internal fun timingSafeEquals(a: ByteArray, b: ByteArray): Boolean =
    MessageDigest.isEqual(a, b)
