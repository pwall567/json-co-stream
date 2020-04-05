/*
 * @(#) JSONDeserializerCoPipelineTest.kt
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
import kotlin.test.expect
import kotlinx.coroutines.runBlocking

import net.pwall.json.Dummy1
import net.pwall.json.JSONInt
import net.pwall.json.JSONObject
import net.pwall.json.JSONString
import net.pwall.json.stream.JSONDeserializerCoPipeline

class JSONDeserializerCoPipelineTest {

    @Test fun `should deserialize strings`() {
        runBlocking {
            val pipeline = JSONDeserializerCoPipeline.create(ListCoAcceptor<String>())
            pipeline.accept(JSONString("alpha"))
            pipeline.accept(JSONString("beta"))
            pipeline.accept(JSONString("gamma"))
            pipeline.close()
            val result = pipeline.result
            expect(3) { result.size }
            expect("alpha") { result[0] }
            expect("beta") { result[1] }
            expect("gamma") { result[2] }
        }
    }

    @Test fun `should deserialize integers`() {
        runBlocking {
            val pipeline = JSONDeserializerCoPipeline.create(ListCoAcceptor<Int>())
            pipeline.accept(JSONInt(123))
            pipeline.accept(JSONInt(456))
            pipeline.close()
            val result = pipeline.result
            expect(2) { result.size }
            expect(123) { result[0] }
            expect(456) { result[1] }
        }
    }

    @Test fun `should deserialize objects`() {
        runBlocking {
            val pipeline = JSONDeserializerCoPipeline.create(ListCoAcceptor<Dummy1>())
            pipeline.accept(JSONObject().apply {
                putValue("field1", "abcdef")
                putValue("field2", 123)
            })
            pipeline.accept(JSONObject().apply {
                putValue("field1", "ghijkl")
                putValue("field2", 456)
            })
            pipeline.close()
            val result = pipeline.result
            expect(2) { result.size }
            expect(Dummy1("abcdef", 123)) { result[0] }
            expect(Dummy1("ghijkl", 456)) { result[1] }
        }
    }

}
