# Publishing to Maven Central Guide

## Prerequisites

1. **Sonatype Account**: Register at https://central.sonatype.com/
2. **Namespace Verification**: Verify ownership of `io.github.ionate12`
   - Go to https://central.sonatype.com/publishing/namespaces
   - Add your GitHub account to verify the `io.github.ionate12` namespace

## Setup Steps

### 1. Export Your GPG Key

Run this command in your terminal:
```bash
gpg --export-secret-keys --armor 6C6D015D60BB43EB | pbcopy
```

This copies your private key to clipboard. Save it securely - you'll need it for signing.

### 2. Get Your GPG Key Password

You'll need the password you set when creating the GPG key.

### 3. Create gradle.properties

Create or edit `~/.gradle/gradle.properties` (in your home directory, NOT in the project):

```properties
# Sonatype Credentials
ossrhUsername=YOUR_SONATYPE_USERNAME
ossrhPassword=YOUR_SONATYPE_PASSWORD

# GPG Signing
signingKey=-----BEGIN PGP PRIVATE KEY BLOCK-----\n...[paste your key here]...\n-----END PGP PRIVATE KEY BLOCK-----
signingPassword=YOUR_GPG_PASSWORD
```

**Important**: Replace newlines in the GPG key with `\n`. The entire key should be on one line.

### 4. Test Local Build

Before publishing, test that signing works:
```bash
./gradlew clean build
./gradlew publishToMavenLocal
```

Check `~/.m2/repository/io/github/ionate12/simple-json-query/` to verify artifacts were created.

### 5. Publish to Maven Central

When ready to publish:
```bash
./gradlew publishAllPublicationsToSonatypeRepository
```

This will:
- Build all platform artifacts (JVM, JS, Native)
- Sign all artifacts with your GPG key
- Upload to Sonatype Central Portal
- Automatically publish (due to `publishingType=AUTOMATIC`)

### 6. Verify Publication

After 10-30 minutes, check:
- https://central.sonatype.com/artifact/io.github.ionate12/simple-json-query

Your library will be available at:
```kotlin
dependencies {
    implementation("io.github.ionate12:simple-json-query:1.0.1")
}
```

## Platform-Specific Artifacts

Maven Central will host these artifacts:
- `simple-json-query` - Main multiplatform artifact
- `simple-json-query-jvm` - JVM only
- `simple-json-query-js` - JavaScript
- `simple-json-query-linuxx64` - Linux
- `simple-json-query-macosx64` - macOS Intel
- `simple-json-query-macosarm64` - macOS ARM
- `simple-json-query-iosarm64` - iOS Device
- `simple-json-query-iosx64` - iOS Simulator Intel
- `simple-json-query-iossimulatorarm64` - iOS Simulator ARM

## Troubleshooting

### 404 Error
Make sure your Sonatype namespace is verified and you have publishing rights.

### Signing Errors
Ensure your GPG key is properly formatted with `\n` for newlines.

### Missing Artifacts
All targets must build successfully. Check with:
```bash
./gradlew build --info
```

## Updating README

Once published, update the README.md installation instructions from JitPack to Maven Central:

```kotlin
dependencies {
    implementation("io.github.ionate12:simple-json-query:1.0.1")
}
```
