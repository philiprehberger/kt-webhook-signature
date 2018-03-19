# Changelog
## 0.1.3 (2026-03-20)- Standardize README: fix title, badges, version sync, remove Requirements section

All notable changes to this library will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [0.1.2] - 2026-03-18

- Upgrade to Kotlin 2.0.21 and Gradle 8.12
- Enable explicitApi() for stricter public API surface
- Add issueManagement to POM metadata

## [Unreleased]

## [0.1.1] - 2026-03-18

- Fix CI badge and gradlew permissions

## [0.1.0] - 2026-03-17

### Added
- `WebhookSigner` class with `sign` and `verify` methods
- Support for HMAC-SHA1, HMAC-SHA256, and HMAC-SHA512 algorithms
- Timing-safe signature comparison via `MessageDigest.isEqual`
- Timestamp tolerance for replay attack prevention
- `WebhookSigner.stripe()` and `WebhookSigner.github()` factory methods
- `parseStripeSignatureHeader()` and `parseGithubSignatureHeader()` header parsers
