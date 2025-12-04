package com.simplejsonquery

import kotlinx.serialization.json.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.*

class SimpleJsonQueryTest {

    companion object {
        // Comprehensive test data
        val testJson = """
        {
            "store": {
                "name": "TechMart",
                "location": "San Francisco",
                "rating": null,
                "products": [
                    {
                        "id": 1,
                        "name": "MacBook Pro",
                        "price": 2499.99,
                        "category": "laptops",
                        "inStock": true,
                        "tags": ["apple", "professional"],
                        "description": "Powerful laptop for professionals",
                        "specs": {
                            "cpu": "M3 Max",
                            "ram": 64
                        }
                    },
                    {
                        "id": 2,
                        "name": "Programming Book",
                        "price": 49.99,
                        "category": "books",
                        "inStock": true,
                        "tags": ["education"],
                        "description": ""
                    },
                    {
                        "id": 3,
                        "name": "iPhone 15",
                        "price": 999.99,
                        "category": "phones",
                        "inStock": false,
                        "tags": ["apple", "mobile"],
                        "description": null
                    },
                    {
                        "id": 4,
                        "name": "Dell XPS",
                        "price": 1299.99,
                        "category": "laptops",
                        "inStock": true,
                        "tags": [],
                        "description": "Windows laptop"
                    },
                    {
                        "id": 5,
                        "name": "AirPods Pro",
                        "price": 249.99,
                        "category": "accessories",
                        "inStock": true,
                        "tags": ["apple", "audio"]
                    }
                ],
                "customers": [
                    {
                        "name": "Alice Johnson",
                        "age": 28,
                        "premium": true,
                        "email": "alice@example.com",
                        "phone": "555-0101"
                    },
                    {
                        "name": "Bob Smith",
                        "age": 17,
                        "premium": false,
                        "email": "bob@example.com",
                        "phone": null
                    },
                    {
                        "name": "Charlie Brown",
                        "age": 35,
                        "premium": true,
                        "email": "charlie@example.com"
                    },
                    {
                        "name": "Diana Prince",
                        "age": 42,
                        "premium": false,
                        "email": "diana@example.com",
                        "metadata": {
                            "vip": true
                        }
                    }
                ],
                "settings": {
                    "theme": "dark",
                    "notifications": true,
                    "emptyObject": {},
                    "emptyArray": []
                }
            },
            "metadata": {
                "version": "1.0",
                "lastUpdated": "2024-01-15"
            }
        }
        """.trimIndent().parseJson()
    }

    // ===== FIELD ACCESS TESTS =====

    @Test
    fun testSimpleFieldAccess() {
        val result = testJson.queryString("store.name")
        assertEquals("TechMart", result)
    }

    @Test
    fun testNestedFieldAccess() {
        val result = testJson.queryString("store.location")
        assertEquals("San Francisco", result)
    }

    @Test
    fun testDeepNestedFieldAccess() {
        val result = testJson.queryString("metadata.version")
        assertEquals("1.0", result)
    }

    @Test
    fun testNonExistentField() {
        val result = testJson.query("store.nonexistent")
        assertTrue(result is JsonNull)
    }

    @Test
    fun testNullField() {
        val result = testJson.query("store.rating")
        assertTrue(result is JsonNull)
    }

    // ===== ARRAY OPERATIONS TESTS =====

    @Test
    fun testArrayFirstElement() {
        val result = testJson.queryString("store.products[0].name")
        assertEquals("MacBook Pro", result)
    }

    @Test
    fun testArrayLastElement() {
        val result = testJson.queryString("store.products[-1].name")
        assertEquals("AirPods Pro", result)
    }

    @Test
    fun testArraySpecificIndex() {
        val result = testJson.queryInt("store.products[2].id")
        assertEquals(3, result)
    }

    @Test
    fun testArrayOutOfBounds() {
        val result = testJson.query("store.products[99]")
        assertTrue(result is JsonNull)
    }

    @Test
    fun testArrayWildcard() {
        val result = testJson.queryList("store.products[*].name")
        assertEquals(5, result.size)
        assertEquals("MacBook Pro", (result[0] as JsonPrimitive).content)
        assertEquals("AirPods Pro", (result[4] as JsonPrimitive).content)
    }

    @Test
    fun testArrayWildcardWithNestedField() {
        val result = testJson.queryList("store.customers[*].email")
        assertEquals(4, result.size)
    }

    // ===== COMPARISON OPERATORS TESTS =====

    @Test
    fun testFilterEquals() {
        val result = testJson.queryList("store.products[?category = \"laptops\"]")
        assertEquals(2, result.size)
    }

    @Test
    fun testFilterNotEquals() {
        val result = testJson.queryList("store.products[?category != \"books\"]")
        assertEquals(4, result.size)
    }

    @Test
    fun testFilterGreaterThan() {
        val result = testJson.queryList("store.products[?price > 1000]")
        assertEquals(2, result.size)
    }

    @Test
    fun testFilterLessThan() {
        val result = testJson.queryList("store.products[?price < 500]")
        assertEquals(2, result.size)
    }

    @Test
    fun testFilterGreaterOrEqual() {
        val result = testJson.queryList("store.products[?price >= 999.99]")
        assertEquals(3, result.size)
    }

    @Test
    fun testFilterLessOrEqual() {
        val result = testJson.queryList("store.products[?price <= 249.99]")
        assertEquals(2, result.size)
    }

    @Test
    fun testFilterBoolean() {
        val result = testJson.queryList("store.products[?inStock = true]")
        assertEquals(4, result.size)
    }

    // ===== LOGICAL OPERATORS TESTS =====

    @Test
    fun testLogicalAnd() {
        val result = testJson.queryList("store.products[?category = \"laptops\" and inStock = true]")
        assertEquals(2, result.size)
    }

    @Test
    fun testLogicalOr() {
        val result = testJson.queryList("store.products[?category = \"books\" or category = \"accessories\"]")
        assertEquals(2, result.size)
    }

    @Test
    fun testComplexLogicalExpression() {
        val result = testJson.queryList("store.products[?price < 1000 and inStock = true]")
        assertEquals(2, result.size)
    }

    @Test
    fun testMultipleAndConditions() {
        val result = testJson.queryList("store.customers[?age >= 18 and premium = true]")
        assertEquals(2, result.size)
    }

    @Test
    fun testMultipleOrConditions() {
        val result = testJson.queryList("store.customers[?age < 20 or age > 40]")
        assertEquals(2, result.size)
    }

    // ===== NULL CHECKS TESTS =====

    @Test
    fun testIsNull() {
        val result = testJson.queryList("store.products[?description is null]")
        assertEquals(2, result.size) // iPhone 15 (explicit null) and AirPods Pro (no field)
    }

    @Test
    fun testIsNotNull() {
        val result = testJson.queryList("store.products[?description is not null]")
        assertEquals(3, result.size) // MacBook Pro, Programming Book (empty string), Dell XPS
    }

    @Test
    fun testEqualsNull() {
        val result = testJson.queryList("store.products[?description = null]")
        assertEquals(2, result.size) // Same as is null
    }

    @Test
    fun testNotEqualsNull() {
        val result = testJson.queryList("store.products[?description != null]")
        assertEquals(3, result.size) // Same as is not null
    }

    @Test
    fun testCustomersWithNullPhone() {
        val result = testJson.queryList("store.customers[?phone is null]")
        assertEquals(3, result.size) // Bob (explicit null), Charlie (no field), Diana (no field)
    }

    @Test
    fun testCustomersWithoutNullPhone() {
        val result = testJson.queryList("store.customers[?phone is not null]")
        assertEquals(1, result.size) // Only Alice has a non-null phone
    }

    // ===== EMPTY CHECKS TESTS =====

    @Test
    fun testIsEmpty() {
        val result = testJson.queryList("store.products[?description is empty]")
        assertEquals(3, result.size) // Programming Book (empty string), iPhone 15 (null), AirPods Pro (no field)
    }

    @Test
    fun testIsNotEmpty() {
        val result = testJson.queryList("store.products[?description is not empty]")
        assertEquals(2, result.size) // MacBook Pro, Dell XPS
    }

    @Test
    fun testEmptyTags() {
        val result = testJson.queryList("store.products[?tags is empty]")
        assertEquals(1, result.size)
    }

    @Test
    fun testNonEmptyTags() {
        val result = testJson.queryList("store.products[?tags is not empty]")
        assertEquals(4, result.size)
    }

    @Test
    fun testEmptyObject() {
        val result = testJson.query("store.settings.emptyObject")
        assertTrue(result is JsonObject)
        assertEquals(0, (result as JsonObject).size)
    }

    // ===== TYPE CHECKS TESTS =====

    @Test
    fun testIsObject() {
        val result = testJson.queryList("store.customers[?metadata is object]")
        assertEquals(1, result.size)
    }

    @Test
    fun testIsNotObject() {
        val result = testJson.queryList("store.customers[?metadata is not object]")
        assertEquals(3, result.size)
    }

    @Test
    fun testIsArray() {
        val result = testJson.queryList("store.products[?tags is array]")
        assertEquals(5, result.size)
    }

    @Test
    fun testIsString() {
        val result = testJson.queryList("store.products[?name is string]")
        assertEquals(5, result.size)
    }

    @Test
    fun testIsNumber() {
        val result = testJson.queryList("store.products[?price is number]")
        assertEquals(5, result.size)
    }

    @Test
    fun testIsBoolean() {
        val result = testJson.queryList("store.customers[?premium is boolean]")
        assertEquals(4, result.size)
    }

    // ===== EXISTENCE CHECKS TESTS =====

    @Test
    fun testFieldExists() {
        val result = testJson.queryList("store.customers[?phone exists]")
        assertEquals(2, result.size) // Alice (has phone), Bob (has phone field even though null)
    }

    @Test
    fun testFieldNotExists() {
        val result = testJson.queryList("store.customers[?phone not_exists]")
        assertEquals(2, result.size) // Charlie and Diana don't have phone field
    }

    @Test
    fun testMetadataExists() {
        val result = testJson.queryList("store.customers[?metadata exists]")
        assertEquals(1, result.size)
    }

    @Test
    fun testNestedFieldExists() {
        val result = testJson.queryList("store.products[?specs exists]")
        assertEquals(1, result.size)
    }

    // ===== MEMBERSHIP CHECKS (IN/NOT IN) TESTS =====

    @Test
    fun testInOperator() {
        val result = testJson.queryList("store.products[?category in [\"laptops\", \"phones\"]]")
        assertEquals(3, result.size)
    }

    @Test
    fun testNotInOperator() {
        val result = testJson.queryList("store.products[?category not_in [\"laptops\", \"phones\"]]")
        assertEquals(2, result.size)
    }

    @Test
    fun testInWithNumbers() {
        val result = testJson.queryList("store.products[?id in [1, 3, 5]]")
        assertEquals(3, result.size)
    }

    @Test
    fun testInWithBooleans() {
        val result = testJson.queryList("store.customers[?premium in [true]]")
        assertEquals(2, result.size)
    }

    // ===== RANGE CHECKS (BETWEEN) TESTS =====

    @Test
    fun testBetween() {
        val result = testJson.queryList("store.products[?price between 200 and 1000]")
        assertEquals(2, result.size)
    }

    @Test
    fun testBetweenAge() {
        val result = testJson.queryList("store.customers[?age between 20 and 40]")
        assertEquals(2, result.size)
    }

    @Test
    fun testBetweenInclusive() {
        val result = testJson.queryList("store.products[?price between 49.99 and 249.99]")
        assertEquals(2, result.size)
    }

    // ===== STRING OPERATIONS TESTS =====

    @Test
    fun testContains() {
        val result = testJson.queryList("store.products[?name contains \"pro\"]")
        assertEquals(3, result.size) // MacBook Pro, iPhone (no), AirPods Pro, Programming
    }

    @Test
    fun testContainsCaseInsensitive() {
        val result = testJson.queryList("store.products[?name contains \"PRO\"]")
        assertEquals(3, result.size)
    }

    @Test
    fun testStartsWith() {
        val result = testJson.queryList("store.products[?name startswith \"Mac\"]")
        assertEquals(1, result.size)
    }

    @Test
    fun testEndsWith() {
        val result = testJson.queryList("store.products[?name endswith \"Pro\"]")
        assertEquals(2, result.size)
    }

    @Test
    fun testEmailContains() {
        val result = testJson.queryList("store.customers[?email contains \"example\"]")
        assertEquals(4, result.size)
    }

    // ===== REGEX MATCHING TESTS =====

    @Test
    fun testMatchesRegex() {
        val result = testJson.queryList("store.products[?name matches \"^(Mac|Air)\"]")
        assertEquals(2, result.size)
    }

    @Test
    fun testMatchesEndPattern() {
        val result = testJson.queryList("store.products[?name matches \"Pro$\"]")
        assertEquals(2, result.size)
    }

    @Test
    fun testMatchesEmailPattern() {
        val result = testJson.queryList("store.customers[?email matches \"@example\\\\.com$\"]")
        assertEquals(4, result.size)
    }

    @Test
    fun testMatchesDigits() {
        val result = testJson.queryList("store.products[?name matches \"\\\\d\"]")
        assertEquals(1, result.size) // iPhone 15
    }

    // ===== LENGTH CHECKS TESTS =====

    @Test
    fun testLengthGreaterThan() {
        val result = testJson.queryList("store.products[?name length > 10]")
        assertEquals(3, result.size)
    }

    @Test
    fun testLengthEquals() {
        val result = testJson.queryList("store.products[?name length = 9]")
        assertEquals(1, result.size) // iPhone 15
    }

    @Test
    fun testLengthLessThan() {
        val result = testJson.queryList("store.products[?name length < 10]")
        assertEquals(2, result.size)
    }

    @Test
    fun testArrayLengthGreaterOrEqual() {
        val result = testJson.queryList("store.products[?tags length >= 2]")
        assertEquals(3, result.size)
    }

    @Test
    fun testArrayLengthEquals() {
        val result = testJson.queryList("store.products[?tags length = 1]")
        assertEquals(1, result.size)
    }

    @Test
    fun testNameLengthRange() {
        val result = testJson.queryList("store.customers[?name length > 10]")
        assertEquals(3, result.size)
    }

    // ===== FIELD SELECTION TESTS =====

    @Test
    fun testSelectFields() {
        val result = testJson.query("store.products[0]{name, price}")
        assertTrue(result is JsonObject)
        val obj = result as JsonObject
        assertEquals(2, obj.size)
        assertTrue(obj.containsKey("name"))
        assertTrue(obj.containsKey("price"))
        assertFalse(obj.containsKey("id"))
    }

    @Test
    fun testSelectFieldsWithFilter() {
        val result = testJson.queryList("store.products[?inStock = true]{name, price}")
        assertEquals(4, result.size)
        val first = result[0] as JsonObject
        assertTrue(first.containsKey("name"))
        assertTrue(first.containsKey("price"))
        assertFalse(first.containsKey("category"))
    }

    @Test
    fun testSelectCustomerFields() {
        val result = testJson.query("store.customers[0]{name, email}")
        assertTrue(result is JsonObject)
        val obj = result as JsonObject
        assertEquals(2, obj.size)
    }

    // ===== CHAINED OPERATIONS TESTS =====

    @Test
    fun testFilterThenField() {
        val result = testJson.queryList("store.products[?inStock = true].name")
        assertEquals(4, result.size)
    }

    @Test
    fun testFilterThenPrice() {
        val result = testJson.queryList("store.products[?category = \"laptops\"].price")
        assertEquals(2, result.size)
    }

    @Test
    fun testComplexChain() {
        val result = testJson.queryList("store.products[?price < 500 and inStock = true].name")
        assertEquals(2, result.size)
    }

    // ===== TYPE-SAFE QUERY METHODS TESTS =====

    @Test
    fun testQueryString() {
        val result = testJson.queryString("store.name")
        assertEquals("TechMart", result)
    }

    @Test
    fun testQueryInt() {
        val result = testJson.queryInt("store.products[0].id")
        assertEquals(1, result)
    }

    @Test
    fun testQueryDouble() {
        val result = testJson.queryDouble("store.products[0].price")
        assertEquals(2499.99, result)
    }

    @Test
    fun testQueryBoolean() {
        val result = testJson.queryBoolean("store.products[0].inStock")
        assertEquals(true, result)
    }

    @Test
    fun testQueryList() {
        val result = testJson.queryList("store.products[*].name")
        assertEquals(5, result.size)
    }

    @Test
    fun testQueryExists() {
        assertTrue(testJson.queryExists("store.products[?price > 2000]"))
        assertFalse(testJson.queryExists("store.products[?price > 5000]"))
    }

    @Test
    fun testQueryStringReturnsNull() {
        val result = testJson.queryString("store.nonexistent")
        assertNull(result)
    }

    @Test
    fun testQueryIntReturnsNull() {
        val result = testJson.queryInt("store.nonexistent")
        assertNull(result)
    }

    // ===== COMBINED VALIDATION TESTS =====

    @Test
    fun testCombinedValidations1() {
        val result = testJson.queryList(
            "store.products[?inStock = true and description is not empty and tags is not empty]"
        )
        assertEquals(1, result.size) // Only MacBook Pro matches all criteria
    }

    @Test
    fun testCombinedValidations2() {
        val result = testJson.queryList(
            "store.products[?price between 100 and 2000 and category in [\"laptops\", \"accessories\"]]"
        )
        assertEquals(2, result.size)
    }

    @Test
    fun testCombinedValidations3() {
        val result = testJson.queryList(
            "store.customers[?age >= 18 and premium = true and phone is not null]"
        )
        assertEquals(1, result.size)
    }

    @Test
    fun testCombinedValidations4() {
        val result = testJson.queryList(
            "store.products[?description exists and description is not null and description is not empty]"
        )
        assertEquals(2, result.size) // MacBook Pro and Dell XPS have non-empty descriptions
    }

    @Test
    fun testComplexFiltering() {
        val result = testJson.queryList(
            "store.products[?name contains \"Pro\" and price > 200]"
        )
        assertEquals(2, result.size)
    }

    // ===== EDGE CASES TESTS =====

    @Test
    fun testEmptyQuery() {
        val result = testJson.query("")
        assertEquals(testJson, result)
    }

    @Test
    fun testWhitespaceQuery() {
        val result = testJson.query("   ")
        assertEquals(testJson, result)
    }

    @Test
    fun testQueryOnNull() {
        val result = testJson.query("store.rating.nested")
        assertTrue(result is JsonNull)
    }

    @Test
    fun testFilterOnSingleObject() {
        val result = testJson.query("store.products[0][?inStock = true]")
        assertTrue(result is JsonArray)
        assertEquals(1, (result as JsonArray).size)
    }

    @Test
    fun testWildcardOnNonArray() {
        val result = testJson.query("store.name[*]")
        assertTrue(result is JsonArray)
        assertEquals(0, (result as JsonArray).size)
    }

    @Test
    fun testNestedObjectAccess() {
        val result = testJson.queryInt("store.products[0].specs.ram")
        assertEquals(64, result)
    }

    @Test
    fun testNestedObjectQuery() {
        val result = testJson.queryString("store.products[0].specs.cpu")
        assertEquals("M3 Max", result)
    }

    // ===== ERROR HANDLING TESTS =====

    @Test
    fun testInvalidQuery() {
        assertThrows<QueryParseException> {
            testJson.query("store.products[")
        }
    }

    @Test
    fun testInvalidOperator() {
        assertThrows<QueryParseException> {
            testJson.query("store.products[?price @ 100]")
        }
    }

    @Test
    fun testMissingBracket() {
        assertThrows<QueryParseException> {
            testJson.query("store.products[0")
        }
    }

    @Test
    fun testInvalidFieldName() {
        assertThrows<QueryParseException> {
            testJson.query("store.")
        }
    }

    @Test
    fun testInvalidBetweenSyntax() {
        assertThrows<QueryParseException> {
            testJson.query("store.products[?price between 10]")
        }
    }

    // ===== REAL-WORLD USE CASE TESTS =====

    @Test
    fun testGetAvailableLaptops() {
        val result = testJson.queryList(
            "store.products[?category = \"laptops\" and inStock = true].name"
        )
        assertEquals(2, result.size)
        assertEquals("MacBook Pro", (result[0] as JsonPrimitive).content)
        assertEquals("Dell XPS", (result[1] as JsonPrimitive).content)
    }

    @Test
    fun testGetAffordableProducts() {
        val result = testJson.queryList(
            "store.products[?price < 1000 and inStock = true]{name, price}"
        )
        assertEquals(2, result.size)
    }

    @Test
    fun testGetPremiumAdultCustomers() {
        val result = testJson.queryList(
            "store.customers[?age >= 18 and premium = true]{name, email}"
        )
        assertEquals(2, result.size)
    }

    @Test
    fun testGetAppleProducts() {
        val result = testJson.queryList(
            "store.products[?name matches \"^(Mac|iPhone|Air)\"].name"
        )
        assertEquals(3, result.size) // MacBook Pro, iPhone 15, AirPods Pro
    }

    @Test
    fun testGetProductsWithValidDescription() {
        val result = testJson.queryList(
            "store.products[?description is not empty and description length > 10]"
        )
        assertEquals(2, result.size)
    }

    @Test
    fun testGetContactableCustomers() {
        val result = testJson.queryList(
            "store.customers[?phone exists and phone is not null]"
        )
        assertEquals(1, result.size) // Only Alice has a phone field with a non-null value
    }

    // ===== PERFORMANCE TESTS =====

    @Test
    fun testLargeArrayWildcard() {
        val result = testJson.queryList("store.products[*]")
        assertEquals(5, result.size)
    }

    @Test
    fun testMultipleFiltersPerformance() {
        val start = System.currentTimeMillis()
        val result = testJson.queryList(
            "store.products[?price > 0 and inStock is boolean and name is not empty]"
        )
        val end = System.currentTimeMillis()
        assertEquals(5, result.size)
        assertTrue(end - start < 100) // Should complete in less than 100ms
    }
}
