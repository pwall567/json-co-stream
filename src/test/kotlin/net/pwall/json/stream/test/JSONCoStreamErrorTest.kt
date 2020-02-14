package net.pwall.json.stream.test

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

import kotlinx.coroutines.runBlocking

import net.pwall.json.JSONException
import net.pwall.json.stream.JSONCoStream

class JSONCoStreamErrorTest {

    @Test fun `should reject incorrect JSON`() {
        runBlocking {
            val exception = assertFailsWith<JSONException> {
                val stream = JSONCoStream()
                stream.accept("abc")
            }
            assertEquals("Illegal syntax in JSON", exception.message)
        }
    }

    @Test fun `should throw exception when JSON incomplete`() {
        runBlocking {
            val exception = assertFailsWith<JSONException> {
                val stream = JSONCoStream()
                stream.accept("[")
            }
            assertEquals("Unexpected end of data", exception.message)
        }
    }

    @Test fun `should throw exception when getting result and JSON incomplete`() {
        runBlocking {
            val exception = assertFailsWith<JSONException> {
                val stream = JSONCoStream()
                stream.accept('{'.toInt())
                stream.result
            }
            assertEquals("JSON not complete", exception.message)
        }
    }

    @Test fun `should reject invalid JSON array`() {
        runBlocking {
            val exception = assertFailsWith<JSONException> {
                val stream = JSONCoStream()
                stream.accept("[{}0]")
            }
            assertEquals("Illegal syntax in JSON array", exception.message)
        }
    }

    @Test fun `should throw exception when extra characters after JSON`() {
        runBlocking {
            val exception = assertFailsWith<JSONException> {
                val stream = JSONCoStream()
                stream.accept("[],")
            }
            assertEquals("Unexpected characters at end of JSON", exception.message)
        }
    }

    @Test fun `should throw exception when JSON keyword incorrect`() {
        runBlocking {
            val exception = assertFailsWith<JSONException> {
                val stream = JSONCoStream()
                stream.accept("tru*e")
            }
            assertEquals("Illegal character in JSON keyword", exception.message)
        }
    }

    @Test fun `should reject invalid number 1`() {
        runBlocking {
            val exception = assertFailsWith<JSONException> {
                val stream = JSONCoStream()
                stream.accept("0.a")
            }
            assertEquals("Illegal JSON number", exception.message)
        }
    }

    @Test fun `should reject invalid number 2`() {
        runBlocking {
            val exception = assertFailsWith<JSONException> {
                val stream = JSONCoStream()
                stream.accept("0Ea")
            }
            assertEquals("Illegal JSON number", exception.message)
        }
    }

    @Test fun `should reject invalid JSON object 1`() {
        runBlocking {
            val exception = assertFailsWith<JSONException> {
                val stream = JSONCoStream()
                stream.accept("{0}")
            }
            assertEquals("Illegal syntax in JSON object", exception.message)
        }
    }

    @Test fun `should reject invalid JSON object 2`() {
        runBlocking {
            val exception = assertFailsWith<JSONException> {
                val stream = JSONCoStream()
                stream.accept("{\"aaa\"}")
            }
            assertEquals("Illegal syntax in JSON object", exception.message)
        }
    }

    @Test fun `should reject invalid JSON object 3`() {
        runBlocking {
            val exception = assertFailsWith<JSONException> {
                val stream = JSONCoStream()
                stream.accept("{\"aaa\":0,}")
            }
            assertEquals("Illegal syntax in JSON object", exception.message)
        }
    }

    @Test fun `should reject invalid JSON string 1`() {
        runBlocking {
            val exception = assertFailsWith<JSONException> {
                val stream = JSONCoStream()
                stream.accept("\"a\u001Eb\"")
            }
            assertEquals("Illegal character in JSON string", exception.message)
        }
    }

    @Test fun `should reject invalid JSON string 2`() {
        runBlocking {
            val exception = assertFailsWith<JSONException> {
                val stream = JSONCoStream()
                stream.accept("\"a\\gb\"")
            }
            assertEquals("Illegal escape sequence in JSON string", exception.message)
        }
    }

    @Test fun `should reject invalid JSON string 3`() {
        runBlocking {
            val exception = assertFailsWith<JSONException> {
                val stream = JSONCoStream()
                stream.accept("\"a\\uxxxxb\"")
            }
            assertEquals("Illegal Unicode sequence in JSON string", exception.message)
        }
    }

}
