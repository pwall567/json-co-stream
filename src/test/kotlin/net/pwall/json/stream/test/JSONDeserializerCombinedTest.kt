/*
 * @(#) JSONDeserializerCombinedTest.kt
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
import kotlin.test.assertTrue
import kotlin.test.expect

import kotlinx.coroutines.runBlocking

import net.pwall.json.Dummy1
import net.pwall.json.stream.JSONArrayCoPipeline
import net.pwall.json.stream.JSONDeserializerCoPipeline
import net.pwall.pipeline.accept

class JSONDeserializerCombinedTest {

    @Test fun `should parse and deserialize in one pipeline`() = runBlocking {
        val pipeline = JSONArrayCoPipeline(JSONDeserializerCoPipeline.create(ListCoAcceptor<Int>()))
        pipeline.accept("[1,3,5,7,9]")
        assertTrue(pipeline.complete)
        val list = pipeline.result
        expect(5) { list.size }
        expect(1) { list[0] }
        expect(3) { list[1] }
        expect(5) { list[2] }
        expect(7) { list[3] }
        expect(9) { list[4] }
     }

    @Test fun `should parse and deserialize objects in one pipeline`() = runBlocking {
        val pipeline = JSONArrayCoPipeline(JSONDeserializerCoPipeline.create(ListCoAcceptor<Dummy1>()))
        pipeline.accept("""[{"field1":"abcdef","field2":543},{"field1":"xyz","field2":-1}]""")
        assertTrue(pipeline.complete)
        val list = pipeline.result
        expect(2) { list.size }
        expect(Dummy1("abcdef", 543)) { list[0] }
        expect(Dummy1("xyz", -1)) { list[1] }
     }

}
