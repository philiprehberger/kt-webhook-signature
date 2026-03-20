# webhook-signature

[![CI](https://github.com/philiprehberger/kt-webhook-signature/actions/workflows/publish.yml/badge.svg)](https://github.com/philiprehberger/kt-webhook-signature/actions/workflows/publish.yml)
[![Maven Central](https://img.shields.io/maven-central/v/com.philiprehberger/webhook-signature)](https://central.sonatype.com/artifact/com.philiprehberger/webhook-signature)
[![License](https://img.shields.io/github/license/philiprehberger/kt-webhook-signature)](LICENSE)

HMAC webhook signature creation and verification with timing-safe comparison.

## Installation

### Gradle (Kotlin DSL)

```kotlin
dependencies {
    implementation("com.philiprehberger:webhook-signature:0.1.3")
}
```

### Gradle (Groovy)

```groovy
dependencies {
    implementation 'com.philiprehberger:webhook-signature:0.1.0'
}
```

### Maven

```xml
<dependency>
    <groupId>com.philiprehberger</groupId>
    <artifactId>webhook-signature</artifactId>
    <version>0.1.3</version>
</dependency>
```

## Usage

```kotlin
import com.philiprehberger.webhooksignature.*

val signer = WebhookSigner("my-secret", HmacAlgorithm.SHA256)
val timestamp = System.currentTimeMillis() / 1000

// Sign a payload
val signature = signer.sign("""{"event":"order.created"}""", timestamp)

// Verify a payload
val valid = signer.verify("""{"event":"order.created"}""", signature, timestamp)
```

### Provider-Specific Signers

```kotlin
// Stripe
val stripeSigner = WebhookSigner.stripe("whsec_your_secret")

// GitHub
val githubSigner = WebhookSigner.github("your_webhook_secret")
```

### Parsing Provider Headers

```kotlin
// Stripe: "t=1234567890,v1=abc123..."
val (timestamp, sig) = parseStripeSignatureHeader(request.getHeader("Stripe-Signature"))!!

// GitHub: "sha256=abc123..."
val sig = parseGithubSignatureHeader(request.getHeader("X-Hub-Signature-256"))!!
```

### Custom Tolerance

```kotlin
import kotlin.time.Duration.Companion.minutes

// Reject signatures older than 10 minutes
signer.verify(payload, signature, timestamp, tolerance = 10.minutes)

// Disable timestamp checking
signer.verify(payload, signature, timestamp, tolerance = Duration.ZERO)
```

## API

| Function / Class | Description |
|------------------|-------------|
| `WebhookSigner(secret, algorithm)` | Create a signer with HMAC algorithm |
| `WebhookSigner.sign(payload, timestamp)` | Sign a payload, returns hex signature |
| `WebhookSigner.verify(payload, signature, timestamp, tolerance)` | Verify a signature with timing-safe comparison |
| `WebhookSigner.stripe(secret)` | Factory for Stripe-compatible signer |
| `WebhookSigner.github(secret)` | Factory for GitHub-compatible signer |
| `HmacAlgorithm` | Enum: `SHA1`, `SHA256`, `SHA512` |
| `parseStripeSignatureHeader(header)` | Parse Stripe signature header |
| `parseGithubSignatureHeader(header)` | Parse GitHub signature header |

## Development

```bash
./gradlew test       # Run tests
./gradlew check      # Run all checks
./gradlew build      # Build JAR
```

## License

MIT
