# Simple JSON Query (SJQ)

A lightweight JSON query language for Kotlin Multiplatform - similar to JMESPath but simpler. Designed for non-developers to query JSON data via RemoteConfig or other JSON sources.

## Platform Support

✅ **Pure Kotlin** - Works on all Kotlin platforms:
- **JVM** (Java 11+)
- **JavaScript** (Browser & Node.js)
- **Native** (iOS, macOS, Linux, Windows)

The library uses only Kotlin stdlib and kotlinx.serialization - no platform-specific dependencies!

## Installation

### Maven Central (Recommended)

The library is available on Maven Central. No additional repositories needed!

**Gradle (Kotlin DSL)**
```kotlin
dependencies {
    implementation("io.github.ionate12:simple-json-query:1.0.1")
}
```

**Gradle (Groovy DSL)**
```groovy
dependencies {
    implementation 'io.github.ionate12:simple-json-query:1.0.1'
}
```

**Maven**
```xml
<dependency>
    <groupId>io.github.ionate12</groupId>
    <artifactId>simple-json-query</artifactId>
    <version>1.0.1</version>
</dependency>
```

### Kotlin Multiplatform

For multiplatform projects, add the dependency to your `commonMain` source set:

```kotlin
kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("io.github.ionate12:simple-json-query:1.0.1")
            }
        }
    }
}
```

**Platform-specific artifacts:**
- JVM: `io.github.ionate12:simple-json-query-jvm:1.0.1`
- JS: `io.github.ionate12:simple-json-query-js:1.0.1`
- Native: `io.github.ionate12:simple-json-query-linuxx64:1.0.1`, `io.github.ionate12:simple-json-query-macosx64:1.0.1`, etc.

## Quick Start

```kotlin
import com.simplejsonquery.*
import kotlinx.serialization.json.*

val jsonData = """
{
    "store": {
        "products": [
            {"id": 1, "name": "MacBook Pro", "price": 2499.99, "inStock": true},
            {"id": 2, "name": "iPhone 15", "price": 999.99, "inStock": false},
            {"id": 3, "name": "AirPods Pro", "price": 249.99, "inStock": true}
        ]
    }
}
""".parseJson()

// Get all product names
val names = jsonData.query("store.products[*].name")
// Result: ["MacBook Pro", "iPhone 15", "AirPods Pro"]

// Filter products in stock
val inStock = jsonData.query("store.products[?inStock = true]")
// Result: [{"id": 1, ...}, {"id": 3, ...}]

// Get names of products under $1000
val affordable = jsonData.query("store.products[?price < 1000].name")
// Result: ["iPhone 15", "AirPods Pro"]

// Type-safe queries
val productName = jsonData.queryString("store.products[0].name")  // "MacBook Pro"
val productPrice = jsonData.queryDouble("store.products[0].price")  // 2499.99
val isInStock = jsonData.queryBoolean("store.products[0].inStock")  // true
```

## Syntax Reference

### Field Access
```
user.name                    → Get nested field
user.address.city            → Deep nesting
```

### Array Operations
```
items[0]                     → First element
items[-1]                    → Last element
items[*]                     → All elements (wildcard)
```

### Filtering
```
users[?age > 18]             → Filter by condition
users[?status = "active"]    → String comparison
users[?premium = true]       → Boolean comparison
```

### Comparison Operators
```
=, !=, >, <, >=, <=          → Standard comparisons
```

### Logical Operators
```
and, or, not                 → Combine conditions
```

### Null Checks
```
field = null                 → Field value is null
field != null                → Field value is not null
field is null                → Same as = null
field is not null            → Same as != null
```

### Empty Checks
```
field is empty               → String/array/object is empty or null
field is not empty           → Has content
```

### Type Checks
```
field is object              → Value is JSON object
field is array               → Value is JSON array
field is string              → Value is string
field is number              → Value is numeric
field is boolean             → Value is true/false
field is not <type>          → Negated type check
```

### Existence Checks
```
field exists                 → Field key exists in object
field not_exists             → Field key doesn't exist
```

### Membership Checks
```
field in ["a", "b", "c"]     → Value is in list
field not_in ["a", "b"]      → Value is not in list
```

### Range Checks
```
field between 10 and 20      → Value in range (inclusive)
```

### String Operations
```
field contains "text"        → Contains substring (case-insensitive)
field startswith "prefix"    → Starts with string
field endswith "suffix"      → Ends with string
field matches "regex"        → Matches regular expression
```

### Length Checks
```
field length > 5             → String/array/object length comparison
field length = 10            → Exact length match
field length >= 2            → Minimum length
```

### Field Selection
```
user{name, email}            → Select specific fields only
```

## Advanced Examples

### Complex Filtering
```kotlin
// Multiple conditions
val result = jsonData.query(
    "store.products[?price < 1000 and inStock = true].name"
)

// Range queries
val midRange = jsonData.query(
    "store.products[?price between 200 and 1500]"
)

// Type validation
val validProducts = jsonData.query(
    "store.products[?description is not empty and tags is not empty]"
)

// Pattern matching
val appleProducts = jsonData.query(
    "store.products[?name matches \"^(Mac|iPhone|Air)\"]"
)
```

### Validation & Existence
```kotlin
// Check if results exist
val hasExpensive = jsonData.queryExists("store.products[?price > 2000]")

// Field existence
val withEmail = jsonData.query("users[?email exists]")

// Non-null validation
val activeUsers = jsonData.query(
    "users[?email is not null and status = \"active\"]"
)
```

### Combined Operations
```kotlin
// Filter, select fields, and extract
val result = jsonData.query(
    "store.products[?inStock = true]{name, price}"
)

// Chained operations
val premiumUsers = jsonData.query(
    "users[?age >= 18 and premium = true].email"
)
```

## API Reference

### Main Class: SimpleJsonQuery

```kotlin
// Execute query and get JsonElement
fun query(json: JsonElement, query: String): JsonElement

// Type-safe query methods
fun queryString(json: JsonElement, query: String): String?
fun queryInt(json: JsonElement, query: String): Int?
fun queryLong(json: JsonElement, query: String): Long?
fun queryDouble(json: JsonElement, query: String): Double?
fun queryBoolean(json: JsonElement, query: String): Boolean?
fun queryList(json: JsonElement, query: String): List<JsonElement>

// Check existence
fun exists(json: JsonElement, query: String): Boolean
```

### Extension Functions

```kotlin
// Query extensions on JsonElement
fun JsonElement.query(query: String): JsonElement
fun JsonElement.queryString(query: String): String?
fun JsonElement.queryInt(query: String): Int?
fun JsonElement.queryBoolean(query: String): Boolean?
fun JsonElement.queryList(query: String): List<JsonElement>
fun JsonElement.queryExists(query: String): Boolean

// Parse JSON string
fun String.parseJson(): JsonElement
```

## Use Cases

### Remote Config
```kotlin
// Query feature flags
val isFeatureEnabled = remoteConfig
    .parseJson()
    .queryBoolean("features.newUI.enabled")

// Get configuration values
val apiEndpoint = remoteConfig
    .parseJson()
    .queryString("endpoints.api.url")
```

### Data Validation
```kotlin
// Validate user data
val validUsers = userData.query(
    "users[?email is not null and email length > 0 and age >= 18]"
)

// Find incomplete records
val incomplete = data.query(
    "records[?requiredField is empty or requiredField not_exists]"
)
```

### Analytics & Reporting
```kotlin
// Get active premium users
val premiumActive = analytics.query(
    "users[?premium = true and lastActive is not null].userId"
)

// Filter by date range (using between)
val recentOrders = orders.query(
    "orders[?timestamp between 1700000000 and 1700100000]"
)
```

## Publishing to Maven Central

This library is published to Maven Central for better Kotlin Multiplatform support.

See [PUBLISH.md](PUBLISH.md) for detailed publishing instructions.

Quick overview:
1. Register at https://central.sonatype.com/
2. Verify namespace ownership for `io.github.ionate12`
3. Set up GPG signing key
4. Configure credentials in `~/.gradle/gradle.properties`
5. Run `./gradlew publishAllPublicationsToSonatypeRepository`

The library will be available within 10-30 minutes at:
```kotlin
implementation("io.github.ionate12:simple-json-query:1.0.1")
```

## Building Locally

```bash
# Build the library
./gradlew build

# Run tests
./gradlew test

# Publish to local Maven repository
./gradlew publishToMavenLocal
```

## Requirements

**All Platforms:**
- Kotlin 1.9.22 or higher
- kotlinx.serialization 1.6.2 or higher

**JVM Only:**
- Java 11 or higher (for JVM target)

**Pure Kotlin:**
- No platform-specific dependencies
- Works on JVM, JS, and all Kotlin/Native targets

## License

MIT License - see LICENSE file for details

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## Support

For issues and questions, please open an issue on GitHub.
