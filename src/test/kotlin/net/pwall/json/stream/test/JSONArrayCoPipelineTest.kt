package net.pwall.json.stream.test

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

import kotlinx.coroutines.runBlocking

import net.pwall.json.JSONArray
import net.pwall.json.JSONBoolean
import net.pwall.json.JSONDecimal
import net.pwall.json.JSONInteger
import net.pwall.json.JSONString
import net.pwall.json.JSONValue
import net.pwall.json.JSONZero
import net.pwall.json.stream.JSONArrayCoPipeline
import net.pwall.util.pipeline.AbstractCoAcceptor

class JSONArrayCoPipelineTest {

    @Test fun `should stream array to receiving lambda`() {
        runBlocking {
            val pipeline = JSONArrayCoPipeline(ListCoAcceptor())
            pipeline.accept("[0,true,\"abc\",8.5,200,[]]")
            assertTrue(pipeline.complete)
            val list = pipeline.result
            assertEquals(6, list.size)
            assertEquals(JSONZero.ZERO, list[0])
            assertEquals(JSONBoolean.TRUE, list[1])
            assertEquals(JSONString("abc"), list[2])
            assertEquals(JSONDecimal("8.5"), list[3])
            assertEquals(JSONInteger(200), list[4])
            assertEquals(JSONArray(), list[5])
        }
    }

    @Test fun `should stream array to Unit Acceptor`() {
        runBlocking {
            val list = ArrayList<JSONValue?>()
            val acceptor = object : AbstractCoAcceptor<JSONValue?, Unit>() {
                override suspend fun acceptObject(value: JSONValue?) {
                    list.add(value)
                }
            }
            val pipeline = JSONArrayCoPipeline(acceptor)
            pipeline.accept("[0,true,\"abc\",8.5,200,[]]")
            assertTrue(pipeline.complete)
            assertEquals(6, list.size)
            assertEquals(JSONZero.ZERO, list[0])
            assertEquals(JSONBoolean.TRUE, list[1])
            assertEquals(JSONString("abc"), list[2])
            assertEquals(JSONDecimal("8.5"), list[3])
            assertEquals(JSONInteger(200), list[4])
            assertEquals(JSONArray(), list[5])
        }
    }

}
