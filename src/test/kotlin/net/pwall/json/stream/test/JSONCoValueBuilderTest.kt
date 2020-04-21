/*
 * @(#) JSONCoValueBuilderTest.kt
 *
 * json-co-stream Kotlin coroutine JSON Streams
 * Copyright (c) 2020 Peter Wall
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package net.pwall.json.stream.test

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

import kotlinx.coroutines.runBlocking

import net.pwall.json.JSONArray
import net.pwall.json.JSONBoolean
import net.pwall.json.JSONDecimal
import net.pwall.json.JSONInteger
import net.pwall.json.JSONLong
import net.pwall.json.JSONObject
import net.pwall.json.JSONString
import net.pwall.json.JSONValue
import net.pwall.json.JSONZero
import net.pwall.json.stream.JSONCoStream
import net.pwall.util.pipeline.CoUTF8_CodePoint

class JSONCoValueBuilderTest {

    @Test fun `should parse number zero`() = runBlocking {
        val stream = JSONCoStream()
        stream.accept("0")
        assertSame(JSONZero.ZERO, stream.result)
    }

    @Test fun `should parse number zero with leading space`() = runBlocking {
        val stream = JSONCoStream()
        stream.accept(" 0")
        assertSame(JSONZero.ZERO, stream.result)
    }

    @Test fun `should parse number zero with trailing space`() = runBlocking {
        val stream = JSONCoStream()
        stream.accept("0 ")
        assertSame(JSONZero.ZERO, stream.result)
    }

    @Test fun `should parse number zero with multiple spaces`() = runBlocking {
        val stream = JSONCoStream()
        stream.accept("    0  ")
        assertSame(JSONZero.ZERO, stream.result)
    }

    @Test fun `should parse a simple integer`() = runBlocking {
        val stream = JSONCoStream()
        stream.accept("123")
        val result = stream.result
        assertTrue(result is JSONInteger)
        assertEquals(JSONInteger(123), result)
    }

    @Test fun `should parse a simple integer with leading space`() = runBlocking {
        val stream = JSONCoStream()
        stream.accept(" 4")
        val result = stream.result
        assertTrue(result is JSONInteger)
        assertEquals(JSONInteger(4), result)
    }

    @Test fun `should parse a simple integer with trailing space`() = runBlocking {
        val stream = JSONCoStream()
        stream.accept("8888 ")
        val result = stream.result
        assertTrue(result is JSONInteger)
        assertEquals(JSONInteger(8888), result)
    }

    @Test fun `should parse a simple integer with multiple spaces`() = runBlocking {
        val stream = JSONCoStream()
        stream.accept("  100001                 ")
        val result = stream.result
        assertTrue(result is JSONInteger)
        assertEquals(JSONInteger(100001), result)
    }

    @Test fun `should parse a negative integer`() = runBlocking {
        val stream = JSONCoStream()
        stream.accept("-54321")
        val result = stream.result
        assertTrue(result is JSONInteger)
        assertEquals(JSONInteger(-54321), result)
    }

    @Test fun `should parse a negative integer with multiple spaces`() = runBlocking {
        val stream = JSONCoStream()
        stream.accept("         -876  ")
        val result = stream.result
        assertTrue(result is JSONInteger)
        assertEquals(JSONInteger(-876), result)
    }

    @Test fun `should parse a long integer`() = runBlocking {
        val stream = JSONCoStream()
        stream.accept("1234567812345678")
        val result = stream.result
        assertTrue(result is JSONLong)
        assertEquals(JSONLong(1234567812345678), result)
    }

    @Test fun `should parse a long integer with multiple spaces`() = runBlocking {
        val stream = JSONCoStream()
        stream.accept("     1232343454565676787  ")
        val result = stream.result
        assertTrue(result is JSONLong)
        assertEquals(JSONLong(1232343454565676787), result)
    }

    @Test fun `should parse a simple decimal`() = runBlocking {
        val stream = JSONCoStream()
        stream.accept("123.45678")
        val result = stream.result
        assertTrue(result is JSONDecimal)
        assertEquals(JSONDecimal("123.45678"), result)
    }

    @Test fun `should parse a simple decimal with leading space`() = runBlocking {
        val stream = JSONCoStream()
        stream.accept(" 123.45678")
        val result = stream.result
        assertTrue(result is JSONDecimal)
        assertEquals(JSONDecimal("123.45678"), result)
    }

    @Test fun `should parse a simple decimal with trailing space`() = runBlocking {
        val stream = JSONCoStream()
        stream.accept("123.5 ")
        val result = stream.result
        assertTrue(result is JSONDecimal)
        assertEquals(JSONDecimal("123.5"), result)
    }

    @Test fun `should parse a simple decimal with multiple spaces`() = runBlocking {
        val stream = JSONCoStream()
        stream.accept(" 98876.25   ")
        val result = stream.result
        assertTrue(result is JSONDecimal)
        assertEquals(JSONDecimal("98876.25"), result)
    }

    @Test fun `should parse a negative decimal`() = runBlocking {
        val stream = JSONCoStream()
        stream.accept("-123.45678")
        val result = stream.result
        assertTrue(result is JSONDecimal)
        assertEquals(JSONDecimal("-123.45678"), result)
    }

    @Test fun `should parse a negative decimal with multiple spaces`() = runBlocking {
        val stream = JSONCoStream()
        stream.accept("     -123.45678 ")
        val result = stream.result
        assertTrue(result is JSONDecimal)
        assertEquals(JSONDecimal("-123.45678"), result)
    }

    @Test fun `should parse scientific notation`() = runBlocking {
        val stream = JSONCoStream()
        stream.accept("123e58")
        val result = stream.result
        assertTrue(result is JSONDecimal)
        assertEquals(JSONDecimal("123e58"), result)
    }

    @Test fun `should parse scientific notation with multiple spaces`() = runBlocking {
        val stream = JSONCoStream()
        stream.accept("   1.2345e+10    ")
        val result = stream.result
        assertTrue(result is JSONDecimal)
        assertEquals(JSONDecimal("1.2345e+10"), result)
    }

    @Test fun `should parse negative scientific notation`() = runBlocking {
        val stream = JSONCoStream()
        stream.accept("-6789.08e-22")
        val result = stream.result
        assertTrue(result is JSONDecimal)
        assertEquals(JSONDecimal("-6789.08e-22"), result)
    }

    @Test fun `should parse negative scientific notation with multiple spaces`() = runBlocking {
        val stream = JSONCoStream()
        stream.accept("   -1.777e-5 ")
        val result = stream.result
        assertTrue(result is JSONDecimal)
        assertEquals(JSONDecimal("-1.777e-5"), result)
    }

    @Test fun `should parse a simple string`() = runBlocking {
        val stream = JSONCoStream()
        stream.accept("\"abcdef\"")
        val result = stream.result
        assertTrue(result is JSONString)
        assertEquals(JSONString("abcdef"), result)
    }

    @Test fun `should parse a simple string with leading space`() = runBlocking {
        val stream = JSONCoStream()
        stream.accept(" \"ghijk\"")
        val result = stream.result
        assertTrue(result is JSONString)
        assertEquals(JSONString("ghijk"), result)
    }

    @Test fun `should parse a simple string with trailing space`() = runBlocking {
        val stream = JSONCoStream()
        stream.accept("\"lmnop\" ")
        val result = stream.result
        assertTrue(result is JSONString)
        assertEquals(JSONString("lmnop"), result)
    }

    @Test fun `should parse a string with a newline`() = runBlocking {
        val stream = JSONCoStream()
        stream.accept("\"abc\\ndef\"")
        val result = stream.result
        assertTrue(result is JSONString)
        assertEquals(JSONString("abc\ndef"), result)
    }

    @Test fun `should parse empty string`() = runBlocking {
        val stream = JSONCoStream()
        stream.accept("\"\"")
        val result = stream.result
        assertTrue(result is JSONString)
        assertEquals(JSONString(""), result)
    }

    @Test fun `should parse a string with a unicode sequence`() = runBlocking {
        val stream = JSONCoStream()
        stream.accept("\"abc\\u000Adef\"")
        val result = stream.result
        assertTrue(result is JSONString)
        assertEquals(JSONString("abc\ndef"), result)
    }

    @Test fun `should parse a string with an emoji`() = runBlocking {
        val stream = CoUTF8_CodePoint(JSONCoStream())
        val json = byteArrayOf('"'.toByte(), 'a'.toByte(), 'a'.toByte(), 0xF0.toByte(), 0x9F.toByte(), 0x98.toByte(),
                0x82.toByte(), 'b'.toByte(), 'b'.toByte(), '"'.toByte())
        stream.accept(json)
        val result = stream.result
        assertTrue(result is JSONString)
        assertEquals(JSONString("aa\uD83D\uDE02bb"), result)
    }

    @Test fun `should parse empty array`() = runBlocking {
        val stream = JSONCoStream()
        stream.accept("[]")
        val result = stream.result
        assertTrue(result is JSONArray)
        assertEquals(JSONArray(), result)
    }

    @Test fun `should parse empty array with spaces 1`() = runBlocking {
        val stream = JSONCoStream()
        stream.accept(" []")
        val result = stream.result
        assertTrue(result is JSONArray)
        assertEquals(JSONArray(), result)
    }

    @Test fun `should parse empty array with spaces 2`() = runBlocking {
        val stream = JSONCoStream()
        stream.accept("[ ]")
        val result = stream.result
        assertTrue(result is JSONArray)
        assertEquals(JSONArray(), result)
    }

    @Test fun `should parse empty array with spaces 3`() = runBlocking {
        val stream = JSONCoStream()
        stream.accept(" [ ]")
        val result = stream.result
        assertTrue(result is JSONArray)
        assertEquals(JSONArray(), result)
    }

    @Test fun `should parse empty array with spaces 4`() = runBlocking {
        val stream = JSONCoStream()
        stream.accept("[] ")
        val result = stream.result
        assertTrue(result is JSONArray)
        assertEquals(JSONArray(), result)
    }

    @Test fun `should parse empty array with spaces 5`() = runBlocking {
        val stream = JSONCoStream()
        stream.accept(" [] ")
        val result = stream.result
        assertTrue(result is JSONArray)
        assertEquals(JSONArray(), result)
    }

    @Test fun `should parse empty array with spaces 6`() = runBlocking {
        val stream = JSONCoStream()
        stream.accept("[ ] ")
        val result = stream.result
        assertTrue(result is JSONArray)
        assertEquals(JSONArray(), result)
    }

    @Test fun `should parse empty array with spaces 7`() = runBlocking {
        val stream = JSONCoStream()
        stream.accept(" [ ] ")
        val result = stream.result
        assertTrue(result is JSONArray)
        assertEquals(JSONArray(), result)
    }

    @Test fun `should parse array with single zero element`() = runBlocking {
        val stream = JSONCoStream()
        stream.accept("[0]")
        val result = stream.result
        assertTrue(result is JSONArray)
        assertEquals(JSONArray(JSONZero.ZERO), result)
    }

    @Test fun `should parse array with two zero elements`() = runBlocking {
        val stream = JSONCoStream()
        stream.accept("[0,0]")
        val result = stream.result
        assertTrue(result is JSONArray)
        assertEquals(JSONArray(JSONZero.ZERO, JSONZero.ZERO), result)
    }

    @Test fun `should parse array with three zero elements including extra spacing`() = runBlocking {
        val stream = JSONCoStream()
        stream.accept(" [0,  0   ,0]")
        val result = stream.result
        assertTrue(result is JSONArray)
        assertEquals(JSONArray(JSONZero.ZERO, JSONZero.ZERO, JSONZero.ZERO), result)
    }

    @Test fun `should parse array with three string elements`() = runBlocking {
        val stream = JSONCoStream()
        stream.accept("""["abcdef","ghijkl","mnopqr"]""")
        val result = stream.result
        assertTrue(result is JSONArray)
        assertEquals(JSONArray(JSONString("abcdef"), JSONString("ghijkl"), JSONString("mnopqr")), result)
    }

    @Test fun `should parse nested array`() = runBlocking {
        val stream = JSONCoStream()
        stream.accept("[[12,34],[56,78]]")
        val result = stream.result
        assertTrue(result is JSONArray)
        assertEquals(JSONArray(JSONArray(JSONInteger(12), JSONInteger(34)),
                JSONArray(JSONInteger(56), JSONInteger(78))), result)
    }

    @Test fun `should parse boolean true`() = runBlocking {
        val stream = JSONCoStream()
        stream.accept("true")
        val result = stream.result
        assertTrue(result is JSONBoolean)
        assertSame(JSONBoolean.TRUE, result)
    }

    @Test fun `should parse boolean true with leading space`() = runBlocking {
        val stream = JSONCoStream()
        stream.accept(" true")
        val result = stream.result
        assertTrue(result is JSONBoolean)
        assertSame(JSONBoolean.TRUE, result)
    }

    @Test fun `should parse boolean true with trailing space`() = runBlocking {
        val stream = JSONCoStream()
        stream.accept("true ")
        val result = stream.result
        assertTrue(result is JSONBoolean)
        assertSame(JSONBoolean.TRUE, result)
    }

    @Test fun `should parse boolean false`() = runBlocking {
        val stream = JSONCoStream()
        stream.accept("false")
        val result = stream.result
        assertTrue(result is JSONBoolean)
        assertSame(JSONBoolean.FALSE, result)
    }

    @Test fun `should parse keyword null`() = runBlocking {
        val stream = JSONCoStream()
        stream.accept("null")
        assertNull(stream.result)
    }

    @Test fun `should parse heterogenous array`() = runBlocking {
        val stream = JSONCoStream()
        stream.accept("[0,true,\"abc\",8.5,200,[]]")
        val result = stream.result
        assertTrue(result is JSONArray)
        assertEquals(JSONArray(JSONZero.ZERO, JSONBoolean.TRUE, JSONString("abc"), JSONDecimal("8.5"), JSONInteger(200),
                JSONArray()), result)
    }

    @Test fun `should parse heterogenous array with extra spacing`() = runBlocking {
        val stream = JSONCoStream()
        stream.accept(" [0 ,true,   \"abc\",  8.5 ,    200 ,[   ]]  ")
        val result = stream.result
        assertTrue(result is JSONArray)
        assertEquals(JSONArray(JSONZero.ZERO, JSONBoolean.TRUE, JSONString("abc"), JSONDecimal("8.5"), JSONInteger(200),
                JSONArray()), result)
    }

    @Test fun `should parse simple object`() = runBlocking {
        val stream = JSONCoStream()
        stream.accept("{\"field\":0}")
        val result = stream.result
        assertTrue(result is JSONObject)
        val map = mapOf<String, JSONValue?>("field" to JSONZero.ZERO)
        assertEquals(JSONObject(map), result)
    }

    @Test fun `should parse object with two fields`() = runBlocking {
        val stream = JSONCoStream()
        stream.accept("""{"f1":0,"f2":123}""")
        val result = stream.result
        assertTrue(result is JSONObject)
        val map = mapOf<String, JSONValue?>("f1" to JSONZero.ZERO, "f2" to JSONInteger(123))
        assertEquals(JSONObject(map), result)
    }

    @Test fun `should parse object with three fields`() = runBlocking {
        val stream = JSONCoStream()
        stream.accept("""{"f1":0,"f2":27.555,"f3":true}""")
        val result = stream.result
        assertTrue(result is JSONObject)
        val map = mapOf<String, JSONValue?>("f1" to JSONZero.ZERO, "f2" to JSONDecimal("27.555"),
                "f3" to JSONBoolean.TRUE)
        assertEquals(JSONObject(map), result)
    }

    @Test fun `should parse object with three fields and multiple spaces`() = runBlocking {
        val stream = JSONCoStream()
        stream.accept(""" {   "f1" :0 ,"f2":   27.555,  "f3"    :  true }   """)
        val result = stream.result
        assertTrue(result is JSONObject)
        val map = mapOf<String, JSONValue?>("f1" to JSONZero.ZERO, "f2" to JSONDecimal("27.555"),
                "f3" to JSONBoolean.TRUE)
        assertEquals(JSONObject(map), result)
    }

    @Test fun `should parse array of object`() = runBlocking {
        val stream = JSONCoStream()
        stream.accept("""[{"aaa":2000}]""")
        val result = stream.result
        assertTrue(result is JSONArray)
        val map = mapOf<String, JSONValue?>("aaa" to JSONInteger(2000))
        assertEquals(JSONArray(JSONObject(map)), result)
    }

    @Test fun `should parse array of object containing array`() = runBlocking {
        val stream = JSONCoStream()
        stream.accept("""[{"aaa":[0]}]""")
        val result = stream.result
        assertTrue(result is JSONArray)
        val map = mapOf<String, JSONValue?>("aaa" to JSONArray(JSONZero.ZERO))
        assertEquals(JSONArray(JSONObject(map)), result)
    }

    @Test fun `should parse array of object containing array with spaces`() = runBlocking {
        val stream = JSONCoStream()
        stream.accept(""" [ { "aaa" : [ 0 ] } ] """)
        val result = stream.result
        assertTrue(result is JSONArray)
        val map = mapOf<String, JSONValue?>("aaa" to JSONArray(JSONZero.ZERO))
        assertEquals(JSONArray(JSONObject(map)), result)
    }

    @Test fun `should ignore byte order mark`() = runBlocking {
        // BOM is not allowed in JSON, but JSONCoStream ignores it anyway
        val stream = CoUTF8_CodePoint(JSONCoStream())
        val json = byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte(), '0'.toByte())
        stream.accept(json)
        assertSame(JSONZero.ZERO, stream.result)
    }

}
