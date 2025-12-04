package com.simplejsonquery

// SimpleJsonQuery (SJQ) - A lightweight JSON query language for Kotlin
// Designed for non-developers to query JSON data via RemoteConfig
// Similar to JMESPath but simpler

import kotlinx.serialization.json.*

/**
 * SimpleJsonQuery (SJQ) - A simple query language for JSON
 *
 * SYNTAX REFERENCE:
 * ================
 *
 * FIELD ACCESS:
 *   user.name                    → Get nested field
 *   user.address.city            → Deep nesting
 *
 * ARRAY OPERATIONS:
 *   items[0]                     → First element
 *   items[-1]                    → Last element
 *   items[*]                     → All elements (wildcard)
 *
 * FILTERING:
 *   users[?age > 18]             → Filter by condition
 *   users[?status = "active"]    → String comparison
 *   users[?premium = true]       → Boolean comparison
 *
 * COMPARISON OPERATORS:
 *   =, !=, >, <, >=, <=          → Standard comparisons
 *
 * LOGICAL OPERATORS:
 *   and, or, not                 → Combine conditions
 *
 * NULL CHECKS:
 *   field = null                 → Field value is null
 *   field != null                → Field value is not null
 *   field is null                → Same as = null
 *   field is not null            → Same as != null
 *
 * EMPTY CHECKS:
 *   field is empty               → String/array/object is empty or null
 *   field is not empty           → Has content
 *
 * TYPE CHECKS:
 *   field is object              → Value is JSON object
 *   field is array               → Value is JSON array
 *   field is string              → Value is string
 *   field is number              → Value is numeric
 *   field is boolean             → Value is true/false
 *   field is not <type>          → Negated type check
 *
 * EXISTENCE CHECKS:
 *   field exists                 → Field key exists in object
 *   field not_exists             → Field key doesn't exist
 *
 * MEMBERSHIP CHECKS:
 *   field in ["a", "b", "c"]     → Value is in list
 *   field not_in ["a", "b"]      → Value is not in list
 *
 * RANGE CHECKS:
 *   field between 10 and 20      → Value in range (inclusive)
 *
 * STRING OPERATIONS:
 *   field contains "text"        → Contains substring (case-insensitive)
 *   field startswith "prefix"    → Starts with string
 *   field endswith "suffix"      → Ends with string
 *   field matches "regex"        → Matches regular expression
 *
 * LENGTH CHECKS:
 *   field length > 5             → String/array/object length comparison
 *   field length = 10            → Exact length match
 *   field length >= 2            → Minimum length
 *
 * FIELD SELECTION:
 *   user{name, email}            → Select specific fields only
 *
 * COMBINED EXAMPLES:
 *   store.products[?price < 100 and inStock = true].name
 *   users[?age between 18 and 65 and status in ["active", "pending"]]
 *   items[?description is not empty and tags length >= 1]
 */
class SimpleJsonQuery {

    companion object {
        /**
         * Execute a query against JSON data
         * @param json The JSON element to query
         * @param query The SJQ query string
         * @return The result as JsonElement, or JsonNull if not found
         */
        fun query(json: JsonElement, query: String): JsonElement {
            if (query.isBlank()) return json
            val parser = QueryParser(query.trim())
            val ast = parser.parse()
            return QueryExecutor.execute(json, ast)
        }

        /**
         * Execute a query and return as String
         */
        fun queryString(json: JsonElement, query: String): String? {
            val result = query(json, query)
            return if (result is JsonPrimitive) result.contentOrNull else null
        }

        /**
         * Execute a query and return as Int
         */
        fun queryInt(json: JsonElement, query: String): Int? {
            val result = query(json, query)
            return if (result is JsonPrimitive) result.intOrNull else null
        }

        /**
         * Execute a query and return as Long
         */
        fun queryLong(json: JsonElement, query: String): Long? {
            val result = query(json, query)
            return if (result is JsonPrimitive) result.longOrNull else null
        }

        /**
         * Execute a query and return as Double
         */
        fun queryDouble(json: JsonElement, query: String): Double? {
            val result = query(json, query)
            return if (result is JsonPrimitive) result.doubleOrNull else null
        }

        /**
         * Execute a query and return as Boolean
         */
        fun queryBoolean(json: JsonElement, query: String): Boolean? {
            val result = query(json, query)
            return if (result is JsonPrimitive) result.booleanOrNull else null
        }

        /**
         * Execute a query and return as List
         */
        fun queryList(json: JsonElement, query: String): List<JsonElement> {
            val result = query(json, query)
            return if (result is JsonArray) result.toList() else emptyList()
        }

        /**
         * Check if a query matches any results
         */
        fun exists(json: JsonElement, query: String): Boolean {
            val result = query(json, query)
            return when (result) {
                is JsonNull -> false
                is JsonArray -> result.isNotEmpty()
                else -> true
            }
        }
    }
}

// ============ AST Node Definitions ============

sealed class QueryNode

data class FieldAccess(val name: String) : QueryNode()
data class ArrayIndex(val index: Int) : QueryNode()
object ArrayWildcard : QueryNode()
data class Filter(val condition: Condition) : QueryNode()
data class SelectFields(val fields: List<String>) : QueryNode()
data class PathNode(val segments: List<QueryNode>) : QueryNode()

// Condition nodes for filtering
sealed class Condition

data class Comparison(
    val field: String,
    val operator: ComparisonOp,
    val value: JsonPrimitive
) : Condition()

data class ContainsCondition(
    val field: String,
    val substring: String
) : Condition()

data class StartsWithCondition(
    val field: String,
    val prefix: String
) : Condition()

data class EndsWithCondition(
    val field: String,
    val suffix: String
) : Condition()

// Null checks
data class IsNullCondition(val field: String) : Condition()
data class IsNotNullCondition(val field: String) : Condition()

// Empty checks (for strings, arrays, objects)
data class IsEmptyCondition(val field: String) : Condition()
data class IsNotEmptyCondition(val field: String) : Condition()

// Type checks
data class IsObjectCondition(val field: String) : Condition()
data class IsArrayCondition(val field: String) : Condition()
data class IsStringCondition(val field: String) : Condition()
data class IsNumberCondition(val field: String) : Condition()
data class IsBooleanCondition(val field: String) : Condition()

// Existence check (field exists in object)
data class ExistsCondition(val field: String) : Condition()
data class NotExistsCondition(val field: String) : Condition()

// In array check (value in list)
data class InCondition(val field: String, val values: List<JsonPrimitive>) : Condition()
data class NotInCondition(val field: String, val values: List<JsonPrimitive>) : Condition()

// Range check
data class BetweenCondition(val field: String, val min: Number, val max: Number) : Condition()

// Regex match
data class MatchesCondition(val field: String, val pattern: String) : Condition()

// Length check
data class LengthCondition(val field: String, val operator: ComparisonOp, val length: Int) : Condition()

data class LogicalAnd(val left: Condition, val right: Condition) : Condition()
data class LogicalOr(val left: Condition, val right: Condition) : Condition()
data class LogicalNot(val condition: Condition) : Condition()

enum class ComparisonOp {
    EQUALS,
    NOT_EQUALS,
    GREATER,
    LESS,
    GREATER_EQUALS,
    LESS_EQUALS
}

// ============ Query Parser ============

class QueryParser(private val input: String) {
    private var pos = 0

    fun parse(): PathNode {
        val segments = mutableListOf<QueryNode>()

        while (pos < input.length) {
            skipWhitespace()
            if (pos >= input.length) break

            when {
                input[pos] == '.' -> {
                    pos++ // skip dot
                    segments.add(parseFieldName())
                }
                input[pos] == '[' -> {
                    segments.add(parseBracket())
                }
                input[pos] == '{' -> {
                    segments.add(parseSelectFields())
                }
                segments.isEmpty() -> {
                    segments.add(parseFieldName())
                }
                else -> break
            }
        }

        return PathNode(segments)
    }

    private fun parseFieldName(): FieldAccess {
        val start = pos
        while (pos < input.length && (input[pos].isLetterOrDigit() || input[pos] == '_' || input[pos] == '-')) {
            pos++
        }
        if (start == pos) {
            throw QueryParseException("Expected field name at position $pos")
        }
        return FieldAccess(input.substring(start, pos))
    }

    private fun parseBracket(): QueryNode {
        pos++ // skip '['
        skipWhitespace()

        if (pos >= input.length) {
            throw QueryParseException("Unexpected end of input in bracket")
        }

        return when {
            input[pos] == '*' -> {
                pos++ // skip '*'
                skipWhitespace()
                expect(']')
                ArrayWildcard
            }
            input[pos] == '?' -> {
                pos++ // skip '?'
                val condition = parseCondition()
                skipWhitespace()
                expect(']')
                Filter(condition)
            }
            input[pos].isDigit() || input[pos] == '-' -> {
                val index = parseInteger()
                skipWhitespace()
                expect(']')
                ArrayIndex(index)
            }
            else -> throw QueryParseException("Unexpected character in bracket: ${input[pos]} at position $pos")
        }
    }

    private fun parseCondition(): Condition {
        skipWhitespace()
        var left = parseSingleCondition()

        while (true) {
            skipWhitespace()
            when {
                matchKeyword("and") -> {
                    skipWhitespace()
                    val right = parseSingleCondition()
                    left = LogicalAnd(left, right)
                }
                matchKeyword("or") -> {
                    skipWhitespace()
                    val right = parseSingleCondition()
                    left = LogicalOr(left, right)
                }
                else -> break
            }
        }

        return left
    }

    private fun parseSingleCondition(): Condition {
        skipWhitespace()

        // Handle parentheses for grouping
        if (pos < input.length && input[pos] == '(') {
            pos++ // skip '('
            val condition = parseCondition()
            skipWhitespace()
            expect(')')
            return condition
        }

        // Handle NOT
        if (matchKeyword("not")) {
            skipWhitespace()
            return LogicalNot(parseSingleCondition())
        }

        val field = parseFieldPath()
        skipWhitespace()

        // Check for various operators
        return when {
            // Null checks: field = null, field != null, field is null, field is not null
            matchKeyword("is") -> {
                skipWhitespace()
                val negated = matchKeyword("not")
                if (negated) skipWhitespace()

                when {
                    matchKeyword("null") -> if (negated) IsNotNullCondition(field) else IsNullCondition(field)
                    matchKeyword("empty") -> if (negated) IsNotEmptyCondition(field) else IsEmptyCondition(field)
                    matchKeyword("object") -> if (negated) LogicalNot(IsObjectCondition(field)) else IsObjectCondition(field)
                    matchKeyword("array") -> if (negated) LogicalNot(IsArrayCondition(field)) else IsArrayCondition(field)
                    matchKeyword("string") -> if (negated) LogicalNot(IsStringCondition(field)) else IsStringCondition(field)
                    matchKeyword("number") -> if (negated) LogicalNot(IsNumberCondition(field)) else IsNumberCondition(field)
                    matchKeyword("boolean") || matchKeyword("bool") -> if (negated) LogicalNot(IsBooleanCondition(field)) else IsBooleanCondition(field)
                    else -> throw QueryParseException("Unknown type check after 'is' at position $pos")
                }
            }

            // Existence checks: field exists, field not exists
            matchKeyword("exists") -> ExistsCondition(field)
            matchKeyword("notexists") || matchKeyword("not_exists") -> NotExistsCondition(field)

            // String operators
            matchKeyword("contains") -> {
                skipWhitespace()
                val value = parseStringValue()
                ContainsCondition(field, value)
            }
            matchKeyword("startswith") || matchKeyword("starts_with") -> {
                skipWhitespace()
                val value = parseStringValue()
                StartsWithCondition(field, value)
            }
            matchKeyword("endswith") || matchKeyword("ends_with") -> {
                skipWhitespace()
                val value = parseStringValue()
                EndsWithCondition(field, value)
            }
            matchKeyword("matches") -> {
                skipWhitespace()
                val pattern = parseStringValue()
                MatchesCondition(field, pattern)
            }

            // In/not in array: field in ["a", "b", "c"]
            matchKeyword("in") -> {
                skipWhitespace()
                val values = parseValueList()
                InCondition(field, values)
            }
            matchKeyword("notin") || matchKeyword("not_in") -> {
                skipWhitespace()
                val values = parseValueList()
                NotInCondition(field, values)
            }

            // Between: field between 10 and 20
            matchKeyword("between") -> {
                skipWhitespace()
                val min = parseNumberValue()
                skipWhitespace()
                if (!matchKeyword("and")) {
                    throw QueryParseException("Expected 'and' in between clause at position $pos")
                }
                skipWhitespace()
                val max = parseNumberValue()
                BetweenCondition(field, min, max)
            }

            // Length check: field.length > 5 or length(field) > 5
            matchKeyword("length") -> {
                skipWhitespace()
                val op = parseOperator()
                skipWhitespace()
                val len = parseInteger()
                LengthCondition(field, op, len)
            }

            // Standard comparison operators
            else -> {
                val op = parseOperator()
                skipWhitespace()
                val value = parseValue()

                // Handle "= null" as IsNullCondition
                if (value is JsonNull) {
                    when (op) {
                        ComparisonOp.EQUALS -> IsNullCondition(field)
                        ComparisonOp.NOT_EQUALS -> IsNotNullCondition(field)
                        else -> Comparison(field, op, value)
                    }
                } else {
                    Comparison(field, op, value)
                }
            }
        }
    }

    private fun parseValueList(): List<JsonPrimitive> {
        expect('[')
        val values = mutableListOf<JsonPrimitive>()

        do {
            skipWhitespace()
            if (pos < input.length && input[pos] == ']') break
            values.add(parseValue())
            skipWhitespace()
        } while (match(","))

        expect(']')
        return values
    }

    private fun parseNumberValue(): Number {
        val numStr = parseNumberString()
        return if (numStr.contains('.')) numStr.toDouble() else numStr.toLong()
    }

    private fun parseFieldPath(): String {
        val parts = mutableListOf<String>()
        parts.add(parseSimpleField())

        while (pos < input.length && input[pos] == '.') {
            val nextPos = pos + 1
            if (nextPos < input.length && (input[nextPos].isLetter() || input[nextPos] == '_')) {
                pos++
                parts.add(parseSimpleField())
            } else {
                break
            }
        }

        return parts.joinToString(".")
    }

    private fun parseSimpleField(): String {
        val start = pos
        while (pos < input.length && (input[pos].isLetterOrDigit() || input[pos] == '_' || input[pos] == '-')) {
            pos++
        }
        if (start == pos) {
            throw QueryParseException("Expected field name at position $pos")
        }
        return input.substring(start, pos)
    }

    private fun parseOperator(): ComparisonOp {
        skipWhitespace()
        return when {
            match("!=") -> ComparisonOp.NOT_EQUALS
            match(">=") -> ComparisonOp.GREATER_EQUALS
            match("<=") -> ComparisonOp.LESS_EQUALS
            match("=") -> ComparisonOp.EQUALS
            match(">") -> ComparisonOp.GREATER
            match("<") -> ComparisonOp.LESS
            else -> throw QueryParseException("Unknown operator at position $pos")
        }
    }

    private fun parseValue(): JsonPrimitive {
        skipWhitespace()

        if (pos >= input.length) {
            throw QueryParseException("Unexpected end of input, expected value")
        }

        return when {
            input[pos] == '"' -> JsonPrimitive(parseStringValue('"'))
            input[pos] == '\'' -> JsonPrimitive(parseStringValue('\''))
            matchKeyword("true") -> JsonPrimitive(true)
            matchKeyword("false") -> JsonPrimitive(false)
            matchKeyword("null") -> JsonNull
            input[pos].isDigit() || input[pos] == '-' -> {
                val numStr = parseNumberString()
                if (numStr.contains('.')) {
                    JsonPrimitive(numStr.toDouble())
                } else {
                    JsonPrimitive(numStr.toLong())
                }
            }
            else -> throw QueryParseException("Cannot parse value at position $pos: '${input.substring(pos, minOf(pos + 10, input.length))}'")
        }
    }

    private fun parseStringValue(quote: Char = '"'): String {
        expect(quote)
        val builder = StringBuilder()

        while (pos < input.length && input[pos] != quote) {
            if (input[pos] == '\\' && pos + 1 < input.length) {
                pos++ // skip backslash
                when (input[pos]) {
                    'n' -> builder.append('\n')
                    't' -> builder.append('\t')
                    'r' -> builder.append('\r')
                    '"' -> builder.append('"')
                    '\'' -> builder.append('\'')
                    '\\' -> builder.append('\\')
                    else -> builder.append(input[pos])
                }
            } else {
                builder.append(input[pos])
            }
            pos++
        }

        expect(quote)
        return builder.toString()
    }

    private fun parseInteger(): Int {
        return parseNumberString().toInt()
    }

    private fun parseNumberString(): String {
        val start = pos
        if (pos < input.length && input[pos] == '-') pos++
        while (pos < input.length && input[pos].isDigit()) {
            pos++
        }
        if (pos < input.length && input[pos] == '.') {
            pos++
            while (pos < input.length && input[pos].isDigit()) {
                pos++
            }
        }
        if (start == pos) {
            throw QueryParseException("Expected number at position $pos")
        }
        return input.substring(start, pos)
    }

    private fun parseSelectFields(): SelectFields {
        pos++ // skip '{'
        val fields = mutableListOf<String>()

        do {
            skipWhitespace()
            if (pos < input.length && input[pos] == '}') break
            fields.add(parseSimpleField())
            skipWhitespace()
        } while (match(","))

        expect('}')
        return SelectFields(fields)
    }

    private fun skipWhitespace() {
        while (pos < input.length && input[pos].isWhitespace()) {
            pos++
        }
    }

    private fun expect(char: Char) {
        if (pos >= input.length || input[pos] != char) {
            val found = if (pos >= input.length) "end of input" else "'${input[pos]}'"
            throw QueryParseException("Expected '$char' but found $found at position $pos")
        }
        pos++
    }

    private fun match(str: String): Boolean {
        if (pos + str.length <= input.length && input.substring(pos, pos + str.length) == str) {
            pos += str.length
            return true
        }
        return false
    }

    private fun matchKeyword(keyword: String): Boolean {
        if (pos + keyword.length > input.length) return false

        val remaining = input.substring(pos)
        if (remaining.lowercase().startsWith(keyword.lowercase())) {
            val nextCharIndex = keyword.length
            if (remaining.length == keyword.length ||
                !remaining[nextCharIndex].isLetterOrDigit() && remaining[nextCharIndex] != '_') {
                pos += keyword.length
                return true
            }
        }
        return false
    }
}

class QueryParseException(message: String) : Exception(message)

// ============ Query Executor ============

object QueryExecutor {

    fun execute(json: JsonElement, path: PathNode): JsonElement {
        var current: JsonElement = json

        for (segment in path.segments) {
            current = when (segment) {
                is FieldAccess -> executeFieldAccess(current, segment.name)
                is ArrayIndex -> executeArrayIndex(current, segment.index)
                is ArrayWildcard -> executeWildcard(current)
                is Filter -> executeFilter(current, segment.condition)
                is SelectFields -> executeSelectFields(current, segment.fields)
                is PathNode -> execute(current, segment)
            }
        }

        return current
    }

    private fun executeFieldAccess(json: JsonElement, name: String): JsonElement {
        return when (json) {
            is JsonObject -> json[name] ?: JsonNull
            is JsonArray -> {
                // Apply to each element in array
                val results = json.mapNotNull { element ->
                    when (element) {
                        is JsonObject -> element[name]?.takeIf { it !is JsonNull }
                        else -> null
                    }
                }
                JsonArray(results)
            }
            else -> JsonNull
        }
    }

    private fun executeArrayIndex(json: JsonElement, index: Int): JsonElement {
        if (json !is JsonArray) return JsonNull
        val actualIndex = if (index < 0) json.size + index else index
        return if (actualIndex in json.indices) json[actualIndex] else JsonNull
    }

    private fun executeWildcard(json: JsonElement): JsonElement {
        return when (json) {
            is JsonArray -> json
            is JsonObject -> JsonArray(json.values.toList())
            else -> JsonArray(emptyList())
        }
    }

    private fun executeFilter(json: JsonElement, condition: Condition): JsonElement {
        if (json !is JsonArray) {
            // If single object, wrap in array and filter
            return if (evaluateCondition(json, condition)) {
                JsonArray(listOf(json))
            } else {
                JsonArray(emptyList())
            }
        }

        return JsonArray(json.filter { element ->
            evaluateCondition(element, condition)
        })
    }

    private fun evaluateCondition(json: JsonElement, condition: Condition): Boolean {
        return when (condition) {
            is Comparison -> evaluateComparison(json, condition)
            is ContainsCondition -> evaluateContains(json, condition)
            is StartsWithCondition -> evaluateStartsWith(json, condition)
            is EndsWithCondition -> evaluateEndsWith(json, condition)

            // Null checks
            is IsNullCondition -> evaluateIsNull(json, condition.field)
            is IsNotNullCondition -> !evaluateIsNull(json, condition.field)

            // Empty checks
            is IsEmptyCondition -> evaluateIsEmpty(json, condition.field)
            is IsNotEmptyCondition -> !evaluateIsEmpty(json, condition.field)

            // Type checks
            is IsObjectCondition -> evaluateIsType(json, condition.field, ::checkIsObject)
            is IsArrayCondition -> evaluateIsType(json, condition.field, ::checkIsArray)
            is IsStringCondition -> evaluateIsType(json, condition.field, ::checkIsString)
            is IsNumberCondition -> evaluateIsType(json, condition.field, ::checkIsNumber)
            is IsBooleanCondition -> evaluateIsType(json, condition.field, ::checkIsBoolean)

            // Existence checks
            is ExistsCondition -> evaluateExists(json, condition.field)
            is NotExistsCondition -> !evaluateExists(json, condition.field)

            // In checks
            is InCondition -> evaluateIn(json, condition.field, condition.values)
            is NotInCondition -> !evaluateIn(json, condition.field, condition.values)

            // Range check
            is BetweenCondition -> evaluateBetween(json, condition)

            // Regex match
            is MatchesCondition -> evaluateMatches(json, condition)

            // Length check
            is LengthCondition -> evaluateLength(json, condition)

            // Logical operators
            is LogicalAnd -> evaluateCondition(json, condition.left) && evaluateCondition(json, condition.right)
            is LogicalOr -> evaluateCondition(json, condition.left) || evaluateCondition(json, condition.right)
            is LogicalNot -> !evaluateCondition(json, condition.condition)
        }
    }

    // ===== Null Check =====
    private fun evaluateIsNull(json: JsonElement, field: String): Boolean {
        val fieldValue = getNestedField(json, field)
        return fieldValue is JsonNull
    }

    // ===== Empty Check =====
    private fun evaluateIsEmpty(json: JsonElement, field: String): Boolean {
        val fieldValue = getNestedField(json, field)
        return when (fieldValue) {
            is JsonNull -> true
            is JsonArray -> fieldValue.isEmpty()
            is JsonObject -> fieldValue.isEmpty()
            is JsonPrimitive -> {
                val content = fieldValue.contentOrNull
                content == null || content.isEmpty()
            }
        }
    }

    // ===== Type Checks =====
    private fun evaluateIsType(json: JsonElement, field: String, typeCheck: (JsonElement) -> Boolean): Boolean {
        val fieldValue = getNestedField(json, field)
        if (fieldValue is JsonNull) return false
        return typeCheck(fieldValue)
    }

    // Type checking functions
    private fun checkIsObject(element: JsonElement): Boolean = element is JsonObject
    private fun checkIsArray(element: JsonElement): Boolean = element is JsonArray
    private fun checkIsString(element: JsonElement): Boolean = element is JsonPrimitive && element.isString
    private fun checkIsNumber(element: JsonElement): Boolean = element is JsonPrimitive && (element.doubleOrNull != null || element.longOrNull != null)
    private fun checkIsBoolean(element: JsonElement): Boolean = element is JsonPrimitive && element.booleanOrNull != null

    // ===== Existence Check =====
    private fun evaluateExists(json: JsonElement, field: String): Boolean {
        if (json !is JsonObject) return false

        val parts = field.split(".")
        var current: JsonElement = json

        for (part in parts) {
            when (current) {
                is JsonObject -> {
                    if (!current.containsKey(part)) return false
                    current = current[part] ?: return false
                }
                else -> return false
            }
        }
        return true
    }

    // ===== In Check =====
    private fun evaluateIn(json: JsonElement, field: String, values: List<JsonPrimitive>): Boolean {
        val fieldValue = getNestedField(json, field)
        if (fieldValue !is JsonPrimitive) return false

        return values.any { value ->
            compareValues(fieldValue, ComparisonOp.EQUALS, value)
        }
    }

    // ===== Between Check =====
    private fun evaluateBetween(json: JsonElement, condition: BetweenCondition): Boolean {
        val fieldValue = getNestedField(json, condition.field)
        if (fieldValue !is JsonPrimitive) return false

        val numValue = fieldValue.doubleOrNull ?: return false
        val min = condition.min.toDouble()
        val max = condition.max.toDouble()

        return numValue >= min && numValue <= max
    }

    // ===== Regex Match =====
    private fun evaluateMatches(json: JsonElement, condition: MatchesCondition): Boolean {
        val fieldValue = getNestedField(json, condition.field)
        if (fieldValue !is JsonPrimitive) return false

        val strValue = fieldValue.contentOrNull ?: return false
        return try {
            Regex(condition.pattern).containsMatchIn(strValue)
        } catch (e: Exception) {
            false
        }
    }

    // ===== Length Check =====
    private fun evaluateLength(json: JsonElement, condition: LengthCondition): Boolean {
        val fieldValue = getNestedField(json, condition.field)

        val length = when (fieldValue) {
            is JsonNull -> return false
            is JsonArray -> fieldValue.size
            is JsonObject -> fieldValue.size
            is JsonPrimitive -> fieldValue.contentOrNull?.length ?: return false
        }

        return when (condition.operator) {
            ComparisonOp.EQUALS -> length == condition.length
            ComparisonOp.NOT_EQUALS -> length != condition.length
            ComparisonOp.GREATER -> length > condition.length
            ComparisonOp.LESS -> length < condition.length
            ComparisonOp.GREATER_EQUALS -> length >= condition.length
            ComparisonOp.LESS_EQUALS -> length <= condition.length
        }
    }

    private fun evaluateComparison(json: JsonElement, comparison: Comparison): Boolean {
        val fieldValue = getNestedField(json, comparison.field)
        if (fieldValue is JsonNull && comparison.value !is JsonNull) return false

        return compareValues(fieldValue, comparison.operator, comparison.value)
    }

    private fun evaluateContains(json: JsonElement, condition: ContainsCondition): Boolean {
        val fieldValue = getNestedField(json, condition.field)
        if (fieldValue !is JsonPrimitive) return false

        val strValue = fieldValue.contentOrNull ?: return false
        return strValue.contains(condition.substring, ignoreCase = true)
    }

    private fun evaluateStartsWith(json: JsonElement, condition: StartsWithCondition): Boolean {
        val fieldValue = getNestedField(json, condition.field)
        if (fieldValue !is JsonPrimitive) return false

        val strValue = fieldValue.contentOrNull ?: return false
        return strValue.startsWith(condition.prefix, ignoreCase = true)
    }

    private fun evaluateEndsWith(json: JsonElement, condition: EndsWithCondition): Boolean {
        val fieldValue = getNestedField(json, condition.field)
        if (fieldValue !is JsonPrimitive) return false

        val strValue = fieldValue.contentOrNull ?: return false
        return strValue.endsWith(condition.suffix, ignoreCase = true)
    }

    private fun getNestedField(json: JsonElement, path: String): JsonElement {
        var current = json
        for (part in path.split(".")) {
            current = when (current) {
                is JsonObject -> current[part] ?: return JsonNull
                else -> return JsonNull
            }
        }
        return current
    }

    private fun compareValues(left: JsonElement, op: ComparisonOp, right: JsonPrimitive): Boolean {
        if (left !is JsonPrimitive) return false

        // Handle null comparisons
        if (left is JsonNull || right is JsonNull) {
            return when (op) {
                ComparisonOp.EQUALS -> left is JsonNull && right is JsonNull
                ComparisonOp.NOT_EQUALS -> !(left is JsonNull && right is JsonNull)
                else -> false
            }
        }

        // Boolean comparison
        val leftBool = left.booleanOrNull
        val rightBool = right.booleanOrNull
        if (leftBool != null && rightBool != null) {
            return when (op) {
                ComparisonOp.EQUALS -> leftBool == rightBool
                ComparisonOp.NOT_EQUALS -> leftBool != rightBool
                else -> false
            }
        }

        // Numeric comparison (try this before string)
        val leftNum = left.doubleOrNull
        val rightNum = right.doubleOrNull
        if (leftNum != null && rightNum != null) {
            return when (op) {
                ComparisonOp.EQUALS -> leftNum == rightNum
                ComparisonOp.NOT_EQUALS -> leftNum != rightNum
                ComparisonOp.GREATER -> leftNum > rightNum
                ComparisonOp.LESS -> leftNum < rightNum
                ComparisonOp.GREATER_EQUALS -> leftNum >= rightNum
                ComparisonOp.LESS_EQUALS -> leftNum <= rightNum
            }
        }

        // String comparison
        val leftStr = left.contentOrNull
        val rightStr = right.contentOrNull
        if (leftStr != null && rightStr != null) {
            return when (op) {
                ComparisonOp.EQUALS -> leftStr.equals(rightStr, ignoreCase = false)
                ComparisonOp.NOT_EQUALS -> !leftStr.equals(rightStr, ignoreCase = false)
                ComparisonOp.GREATER -> leftStr > rightStr
                ComparisonOp.LESS -> leftStr < rightStr
                ComparisonOp.GREATER_EQUALS -> leftStr >= rightStr
                ComparisonOp.LESS_EQUALS -> leftStr <= rightStr
            }
        }

        return false
    }

    private fun executeSelectFields(json: JsonElement, fields: List<String>): JsonElement {
        return when (json) {
            is JsonObject -> {
                buildJsonObject {
                    fields.forEach { field ->
                        json[field]?.let { put(field, it) }
                    }
                }
            }
            is JsonArray -> {
                JsonArray(json.map { element ->
                    executeSelectFields(element, fields)
                })
            }
            else -> JsonNull
        }
    }
}

// ============ Extension Functions ============

/**
 * Query this JSON element using SJQ syntax
 */
fun JsonElement.query(query: String): JsonElement = SimpleJsonQuery.query(this, query)

/**
 * Query and get result as String
 */
fun JsonElement.queryString(query: String): String? = SimpleJsonQuery.queryString(this, query)

/**
 * Query and get result as Int
 */
fun JsonElement.queryInt(query: String): Int? = SimpleJsonQuery.queryInt(this, query)

/**
 * Query and get result as Long
 */
fun JsonElement.queryLong(query: String): Long? = SimpleJsonQuery.queryLong(this, query)

/**
 * Query and get result as Double
 */
fun JsonElement.queryDouble(query: String): Double? = SimpleJsonQuery.queryDouble(this, query)

/**
 * Query and get result as Boolean
 */
fun JsonElement.queryBoolean(query: String): Boolean? = SimpleJsonQuery.queryBoolean(this, query)

/**
 * Query and get result as List
 */
fun JsonElement.queryList(query: String): List<JsonElement> = SimpleJsonQuery.queryList(this, query)

/**
 * Check if query matches any results
 */
fun JsonElement.queryExists(query: String): Boolean = SimpleJsonQuery.exists(this, query)

/**
 * Parse string to JsonElement
 */
fun String.parseJson(): JsonElement = Json.parseToJsonElement(this)
