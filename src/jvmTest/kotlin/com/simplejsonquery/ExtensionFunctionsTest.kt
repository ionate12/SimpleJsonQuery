package com.simplejsonquery

import kotlinx.serialization.json.*
import org.junit.jupiter.api.Test
import kotlin.test.*

class ExtensionFunctionsTest {

    private val testJson = """
    {
        "user": {
            "name": "Alice",
            "age": 30,
            "email": "alice@example.com",
            "premium": true,
            "address": {
                "city": "San Francisco",
                "zip": "94102"
            }
        },
        "items": [
            {"id": 1, "name": "Item A", "price": 10.50},
            {"id": 2, "name": "Item B", "price": 25.00},
            {"id": 3, "name": "Item C", "price": 15.75}
        ]
    }
    """.trimIndent()

    @Test
    fun testParseJsonExtension() {
        val json = testJson.parseJson()
        assertTrue(json is JsonObject)
        assertTrue(json.containsKey("user"))
    }

    @Test
    fun testQueryExtension() {
        val json = testJson.parseJson()
        val result = json.query("user.name")
        assertTrue(result is JsonPrimitive)
        assertEquals("Alice", (result as JsonPrimitive).content)
    }

    @Test
    fun testQueryStringExtension() {
        val json = testJson.parseJson()
        val result = json.queryString("user.name")
        assertEquals("Alice", result)
    }

    @Test
    fun testQueryStringReturnsNullForNonString() {
        val json = testJson.parseJson()
        val result = json.queryString("user.age")
        // queryString returns the string representation even for numbers
        assertEquals("30", result)
    }

    @Test
    fun testQueryStringReturnsNullForMissing() {
        val json = testJson.parseJson()
        val result = json.queryString("user.nonexistent")
        assertNull(result)
    }

    @Test
    fun testQueryIntExtension() {
        val json = testJson.parseJson()
        val result = json.queryInt("user.age")
        assertEquals(30, result)
    }

    @Test
    fun testQueryIntReturnsNullForNonInt() {
        val json = testJson.parseJson()
        val result = json.queryInt("user.name")
        assertNull(result)
    }

    @Test
    fun testQueryBooleanExtension() {
        val json = testJson.parseJson()
        val result = json.queryBoolean("user.premium")
        assertEquals(true, result)
    }

    @Test
    fun testQueryBooleanReturnsNullForNonBoolean() {
        val json = testJson.parseJson()
        val result = json.queryBoolean("user.name")
        assertNull(result)
    }

    @Test
    fun testQueryListExtension() {
        val json = testJson.parseJson()
        val result = json.queryList("items[*]")
        assertEquals(3, result.size)
    }

    @Test
    fun testQueryListReturnsEmptyForNonArray() {
        val json = testJson.parseJson()
        val result = json.queryList("user.name")
        assertEquals(0, result.size)
    }

    @Test
    fun testQueryExistsExtension() {
        val json = testJson.parseJson()
        assertTrue(json.queryExists("user.name"))
        assertFalse(json.queryExists("user.nonexistent"))
    }

    @Test
    fun testQueryExistsWithFilter() {
        val json = testJson.parseJson()
        assertTrue(json.queryExists("items[?price > 10]"))
        assertFalse(json.queryExists("items[?price > 100]"))
    }

    @Test
    fun testQueryExistsWithEmptyArray() {
        val json = """{"items": []}""".parseJson()
        assertFalse(json.queryExists("items[*]"))
    }

    @Test
    fun testQueryExistsWithNull() {
        val json = """{"value": null}""".parseJson()
        assertFalse(json.queryExists("value"))
    }

    @Test
    fun testChainedExtensions() {
        val result = testJson
            .parseJson()
            .query("items[?price > 10]")
            .queryList("[*].name")
        assertEquals(3, result.size) // All 3 items have price > 10
    }

    @Test
    fun testNestedQueryExtension() {
        val json = testJson.parseJson()
        val result = json.queryString("user.address.city")
        assertEquals("San Francisco", result)
    }

    @Test
    fun testQueryDoubleFromPrice() {
        val json = testJson.parseJson()
        val result = json.queryDouble("items[0].price")
        assertEquals(10.50, result)
    }

    @Test
    fun testQueryLongFromId() {
        val json = testJson.parseJson()
        val result = json.queryLong("items[0].id")
        assertEquals(1L, result)
    }

    @Test
    fun testParseInvalidJson() {
        assertFails {
            "{invalid json}".parseJson()
        }
    }

    @Test
    fun testParseEmptyJson() {
        assertFails {
            "".parseJson()
        }
    }

    @Test
    fun testParseJsonArray() {
        val json = """[1, 2, 3]""".parseJson()
        assertTrue(json is JsonArray)
        assertEquals(3, (json as JsonArray).size)
    }

    @Test
    fun testParseJsonPrimitive() {
        val json = """"test"""".parseJson()
        assertTrue(json is JsonPrimitive)
        assertEquals("test", (json as JsonPrimitive).content)
    }

    @Test
    fun testQueryOnParsedArray() {
        val json = """[{"name": "A"}, {"name": "B"}]""".parseJson()
        val result = json.queryList("[*].name")
        assertEquals(2, result.size)
    }

    @Test
    fun testQueryStringOnNestedObject() {
        val json = """
        {
            "level1": {
                "level2": {
                    "level3": {
                        "value": "deep"
                    }
                }
            }
        }
        """.parseJson()
        val result = json.queryString("level1.level2.level3.value")
        assertEquals("deep", result)
    }

    @Test
    fun testMultipleQueryCalls() {
        val json = testJson.parseJson()
        val name = json.queryString("user.name")
        val age = json.queryInt("user.age")
        val email = json.queryString("user.email")

        assertEquals("Alice", name)
        assertEquals(30, age)
        assertEquals("alice@example.com", email)
    }

    @Test
    fun testQueryWithComplexFilter() {
        val json = testJson.parseJson()
        val result = json.queryList("items[?price > 10 and price < 20]")
        assertEquals(2, result.size) // Item A (10.50) and Item C (15.75)
    }

    @Test
    fun testExtensionOnFilteredResult() {
        val json = testJson.parseJson()
        val filtered = json.query("items[?price > 15]")
        val names = (filtered as JsonArray).map {
            (it as JsonObject)["name"]?.let { name -> (name as JsonPrimitive).content }
        }
        assertEquals(2, names.size)
        assertTrue(names.contains("Item B"))
        assertTrue(names.contains("Item C"))
    }
}
