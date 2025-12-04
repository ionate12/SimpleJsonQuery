package com.simplejsonquery

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.*

class QueryParserTest {

    @Test
    fun testParseSimpleFieldAccess() {
        val parser = QueryParser("name")
        val result = parser.parse()
        assertTrue(result.segments.size == 1)
        assertTrue(result.segments[0] is FieldAccess)
        assertEquals("name", (result.segments[0] as FieldAccess).name)
    }

    @Test
    fun testParseNestedFieldAccess() {
        val parser = QueryParser("user.name")
        val result = parser.parse()
        assertEquals(2, result.segments.size)
        assertTrue(result.segments[0] is FieldAccess)
        assertTrue(result.segments[1] is FieldAccess)
        assertEquals("user", (result.segments[0] as FieldAccess).name)
        assertEquals("name", (result.segments[1] as FieldAccess).name)
    }

    @Test
    fun testParseDeepNestedFieldAccess() {
        val parser = QueryParser("user.address.city.name")
        val result = parser.parse()
        assertEquals(4, result.segments.size)
    }

    @Test
    fun testParseArrayIndex() {
        val parser = QueryParser("items[0]")
        val result = parser.parse()
        assertEquals(2, result.segments.size)
        assertTrue(result.segments[1] is ArrayIndex)
        assertEquals(0, (result.segments[1] as ArrayIndex).index)
    }

    @Test
    fun testParseNegativeArrayIndex() {
        val parser = QueryParser("items[-1]")
        val result = parser.parse()
        assertTrue(result.segments[1] is ArrayIndex)
        assertEquals(-1, (result.segments[1] as ArrayIndex).index)
    }

    @Test
    fun testParseArrayWildcard() {
        val parser = QueryParser("items[*]")
        val result = parser.parse()
        assertTrue(result.segments[1] is ArrayWildcard)
    }

    @Test
    fun testParseFilter() {
        val parser = QueryParser("items[?age > 18]")
        val result = parser.parse()
        assertTrue(result.segments[1] is Filter)
    }

    @Test
    fun testParseSelectFields() {
        val parser = QueryParser("user{name, email}")
        val result = parser.parse()
        assertTrue(result.segments[1] is SelectFields)
        val fields = (result.segments[1] as SelectFields).fields
        assertEquals(2, fields.size)
        assertTrue(fields.contains("name"))
        assertTrue(fields.contains("email"))
    }

    @Test
    fun testParseComparisonEquals() {
        val parser = QueryParser("items[?status = \"active\"]")
        val result = parser.parse()
        val filter = result.segments[1] as Filter
        assertTrue(filter.condition is Comparison)
        val comparison = filter.condition as Comparison
        assertEquals(ComparisonOp.EQUALS, comparison.operator)
    }

    @Test
    fun testParseComparisonNotEquals() {
        val parser = QueryParser("items[?status != \"inactive\"]")
        val result = parser.parse()
        val filter = result.segments[1] as Filter
        val comparison = filter.condition as Comparison
        assertEquals(ComparisonOp.NOT_EQUALS, comparison.operator)
    }

    @Test
    fun testParseComparisonGreaterThan() {
        val parser = QueryParser("items[?age > 18]")
        val result = parser.parse()
        val filter = result.segments[1] as Filter
        val comparison = filter.condition as Comparison
        assertEquals(ComparisonOp.GREATER, comparison.operator)
    }

    @Test
    fun testParseComparisonLessThan() {
        val parser = QueryParser("items[?age < 65]")
        val result = parser.parse()
        val filter = result.segments[1] as Filter
        val comparison = filter.condition as Comparison
        assertEquals(ComparisonOp.LESS, comparison.operator)
    }

    @Test
    fun testParseLogicalAnd() {
        val parser = QueryParser("items[?age > 18 and active = true]")
        val result = parser.parse()
        val filter = result.segments[1] as Filter
        assertTrue(filter.condition is LogicalAnd)
    }

    @Test
    fun testParseLogicalOr() {
        val parser = QueryParser("items[?age < 18 or age > 65]")
        val result = parser.parse()
        val filter = result.segments[1] as Filter
        assertTrue(filter.condition is LogicalOr)
    }

    @Test
    fun testParseIsNull() {
        val parser = QueryParser("items[?field is null]")
        val result = parser.parse()
        val filter = result.segments[1] as Filter
        assertTrue(filter.condition is IsNullCondition)
    }

    @Test
    fun testParseIsNotNull() {
        val parser = QueryParser("items[?field is not null]")
        val result = parser.parse()
        val filter = result.segments[1] as Filter
        assertTrue(filter.condition is IsNotNullCondition)
    }

    @Test
    fun testParseIsEmpty() {
        val parser = QueryParser("items[?field is empty]")
        val result = parser.parse()
        val filter = result.segments[1] as Filter
        assertTrue(filter.condition is IsEmptyCondition)
    }

    @Test
    fun testParseIsNotEmpty() {
        val parser = QueryParser("items[?field is not empty]")
        val result = parser.parse()
        val filter = result.segments[1] as Filter
        assertTrue(filter.condition is IsNotEmptyCondition)
    }

    @Test
    fun testParseIsObject() {
        val parser = QueryParser("items[?field is object]")
        val result = parser.parse()
        val filter = result.segments[1] as Filter
        assertTrue(filter.condition is IsObjectCondition)
    }

    @Test
    fun testParseIsArray() {
        val parser = QueryParser("items[?field is array]")
        val result = parser.parse()
        val filter = result.segments[1] as Filter
        assertTrue(filter.condition is IsArrayCondition)
    }

    @Test
    fun testParseIsString() {
        val parser = QueryParser("items[?field is string]")
        val result = parser.parse()
        val filter = result.segments[1] as Filter
        assertTrue(filter.condition is IsStringCondition)
    }

    @Test
    fun testParseIsNumber() {
        val parser = QueryParser("items[?field is number]")
        val result = parser.parse()
        val filter = result.segments[1] as Filter
        assertTrue(filter.condition is IsNumberCondition)
    }

    @Test
    fun testParseIsBoolean() {
        val parser = QueryParser("items[?field is boolean]")
        val result = parser.parse()
        val filter = result.segments[1] as Filter
        assertTrue(filter.condition is IsBooleanCondition)
    }

    @Test
    fun testParseExists() {
        val parser = QueryParser("items[?field exists]")
        val result = parser.parse()
        val filter = result.segments[1] as Filter
        assertTrue(filter.condition is ExistsCondition)
    }

    @Test
    fun testParseNotExists() {
        val parser = QueryParser("items[?field not_exists]")
        val result = parser.parse()
        val filter = result.segments[1] as Filter
        assertTrue(filter.condition is NotExistsCondition)
    }

    @Test
    fun testParseIn() {
        val parser = QueryParser("items[?status in [\"active\", \"pending\"]]")
        val result = parser.parse()
        val filter = result.segments[1] as Filter
        assertTrue(filter.condition is InCondition)
        val inCondition = filter.condition as InCondition
        assertEquals(2, inCondition.values.size)
    }

    @Test
    fun testParseNotIn() {
        val parser = QueryParser("items[?status not_in [\"inactive\", \"deleted\"]]")
        val result = parser.parse()
        val filter = result.segments[1] as Filter
        assertTrue(filter.condition is NotInCondition)
    }

    @Test
    fun testParseBetween() {
        val parser = QueryParser("items[?age between 18 and 65]")
        val result = parser.parse()
        val filter = result.segments[1] as Filter
        assertTrue(filter.condition is BetweenCondition)
        val between = filter.condition as BetweenCondition
        assertEquals(18, between.min.toInt())
        assertEquals(65, between.max.toInt())
    }

    @Test
    fun testParseContains() {
        val parser = QueryParser("items[?name contains \"test\"]")
        val result = parser.parse()
        val filter = result.segments[1] as Filter
        assertTrue(filter.condition is ContainsCondition)
    }

    @Test
    fun testParseStartsWith() {
        val parser = QueryParser("items[?name startswith \"prefix\"]")
        val result = parser.parse()
        val filter = result.segments[1] as Filter
        assertTrue(filter.condition is StartsWithCondition)
    }

    @Test
    fun testParseEndsWith() {
        val parser = QueryParser("items[?name endswith \"suffix\"]")
        val result = parser.parse()
        val filter = result.segments[1] as Filter
        assertTrue(filter.condition is EndsWithCondition)
    }

    @Test
    fun testParseMatches() {
        val parser = QueryParser("items[?name matches \"^test.*\"]")
        val result = parser.parse()
        val filter = result.segments[1] as Filter
        assertTrue(filter.condition is MatchesCondition)
    }

    @Test
    fun testParseLength() {
        val parser = QueryParser("items[?name length > 5]")
        val result = parser.parse()
        val filter = result.segments[1] as Filter
        assertTrue(filter.condition is LengthCondition)
    }

    @Test
    fun testParseStringWithSingleQuotes() {
        val parser = QueryParser("items[?name = 'test']")
        val result = parser.parse()
        val filter = result.segments[1] as Filter
        val comparison = filter.condition as Comparison
        assertEquals("test", comparison.value.content)
    }

    @Test
    fun testParseStringWithEscapedQuotes() {
        val parser = QueryParser("items[?name = \"test\\\"value\"]")
        val result = parser.parse()
        val filter = result.segments[1] as Filter
        val comparison = filter.condition as Comparison
        assertEquals("test\"value", comparison.value.content)
    }

    @Test
    fun testParseNumberInteger() {
        val parser = QueryParser("items[?age = 42]")
        val result = parser.parse()
        val filter = result.segments[1] as Filter
        val comparison = filter.condition as Comparison
        assertTrue(comparison.value.isString.not())
        assertNotNull(comparison.value.content.toLongOrNull())
    }

    @Test
    fun testParseNumberDouble() {
        val parser = QueryParser("items[?price = 99.99]")
        val result = parser.parse()
        val filter = result.segments[1] as Filter
        val comparison = filter.condition as Comparison
        assertTrue(comparison.value.isString.not())
        assertNotNull(comparison.value.content.toDoubleOrNull())
    }

    @Test
    fun testParseNegativeNumber() {
        val parser = QueryParser("items[?value = -42]")
        val result = parser.parse()
        val filter = result.segments[1] as Filter
        val comparison = filter.condition as Comparison
        assertTrue(comparison.value.isString.not())
        assertEquals("-42", comparison.value.content)
    }

    @Test
    fun testParseBooleanTrue() {
        val parser = QueryParser("items[?active = true]")
        val result = parser.parse()
        val filter = result.segments[1] as Filter
        val comparison = filter.condition as Comparison
        assertTrue(comparison.value.isString.not())
        assertEquals("true", comparison.value.content)
    }

    @Test
    fun testParseBooleanFalse() {
        val parser = QueryParser("items[?active = false]")
        val result = parser.parse()
        val filter = result.segments[1] as Filter
        val comparison = filter.condition as Comparison
        assertTrue(comparison.value.isString.not())
        assertEquals("false", comparison.value.content)
    }

    @Test
    fun testParseComplexQuery() {
        val parser = QueryParser("store.products[?price > 100 and inStock = true].name")
        val result = parser.parse()
        assertEquals(4, result.segments.size)
        assertTrue(result.segments[0] is FieldAccess)
        assertTrue(result.segments[1] is FieldAccess)
        assertTrue(result.segments[2] is Filter)
        assertTrue(result.segments[3] is FieldAccess)
    }

    @Test
    fun testParseChainedFilters() {
        val parser = QueryParser("items[?age > 18][0]")
        val result = parser.parse()
        assertEquals(3, result.segments.size)
        assertTrue(result.segments[1] is Filter)
        assertTrue(result.segments[2] is ArrayIndex)
    }

    @Test
    fun testParseWhitespace() {
        val parser = QueryParser("items[ ? age > 18 ]")
        val result = parser.parse()
        assertTrue(result.segments[1] is Filter)
    }

    @Test
    fun testParseFieldWithHyphen() {
        val parser = QueryParser("user.first-name")
        val result = parser.parse()
        assertEquals("first-name", (result.segments[1] as FieldAccess).name)
    }

    @Test
    fun testParseFieldWithUnderscore() {
        val parser = QueryParser("user.first_name")
        val result = parser.parse()
        assertEquals("first_name", (result.segments[1] as FieldAccess).name)
    }

    // ===== ERROR CASES =====

    @Test
    fun testParseErrorMissingClosingBracket() {
        assertThrows<QueryParseException> {
            QueryParser("items[0").parse()
        }
    }

    @Test
    fun testParseErrorInvalidCharacterInBracket() {
        assertThrows<QueryParseException> {
            QueryParser("items[@]").parse()
        }
    }

    @Test
    fun testParseErrorMissingFieldName() {
        assertThrows<QueryParseException> {
            QueryParser("user.").parse()
        }
    }

    @Test
    fun testParseErrorInvalidOperator() {
        assertThrows<QueryParseException> {
            QueryParser("items[?age @ 18]").parse()
        }
    }

    @Test
    fun testParseErrorUnterminatedString() {
        assertThrows<QueryParseException> {
            QueryParser("items[?name = \"test]").parse()
        }
    }

    @Test
    fun testParseErrorMissingValue() {
        assertThrows<QueryParseException> {
            QueryParser("items[?age >]").parse()
        }
    }

    @Test
    fun testParseErrorInvalidBetweenSyntax() {
        assertThrows<QueryParseException> {
            QueryParser("items[?age between 18]").parse()
        }
    }

    @Test
    fun testParseErrorMissingInList() {
        assertThrows<QueryParseException> {
            QueryParser("items[?status in]").parse()
        }
    }

    @Test
    fun testParseErrorUnterminatedList() {
        assertThrows<QueryParseException> {
            QueryParser("items[?status in [\"a\", \"b\"").parse()
        }
    }

    @Test
    fun testParseErrorEmptyQuery() {
        val parser = QueryParser("")
        val result = parser.parse()
        assertEquals(0, result.segments.size)
    }

    @Test
    fun testParseErrorInvalidTypeCheck() {
        assertThrows<QueryParseException> {
            QueryParser("items[?field is invalid]").parse()
        }
    }

    @Test
    fun testParseNestedFieldPath() {
        val parser = QueryParser("items[?user.age > 18]")
        val result = parser.parse()
        val filter = result.segments[1] as Filter
        val comparison = filter.condition as Comparison
        assertEquals("user.age", comparison.field)
    }

    @Test
    fun testParseMultipleSelectFields() {
        val parser = QueryParser("user{name, email, age, phone}")
        val result = parser.parse()
        val select = result.segments[1] as SelectFields
        assertEquals(4, select.fields.size)
    }

    @Test
    fun testParseComplexLogicalExpression() {
        val parser = QueryParser("items[?age > 18 and status = \"active\" or premium = true]")
        val result = parser.parse()
        val filter = result.segments[1] as Filter
        assertTrue(filter.condition is LogicalOr)
    }
}
