/*
 * @(#) JSONDeserializerCoPipeline.kt
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

package net.pwall.json.stream

import kotlin.reflect.KType

import net.pwall.json.JSONConfig
import net.pwall.json.JSONDeserializer
import net.pwall.json.JSONTypeRef
import net.pwall.json.JSONValue
import net.pwall.util.pipeline.AbstractCoPipeline
import net.pwall.util.pipeline.CoAcceptor

/**
 * A pipeline that takes a [JSONValue] and emits the deserialized value.
 *
 * @constructor
 * @param   type        the target type
 * @param   downstream  the consumer of the deserialized values
 * @param   config      an optional `JSONConfig`
 * @param   E           the target type
 * @param   R           the pipeline result type
 * @author  Peter Wall
 */
class JSONDeserializerCoPipeline<E, R>(private val type: KType, downstream: CoAcceptor<E, R>,
        private val config: JSONConfig = JSONConfig.defaultConfig) : AbstractCoPipeline<JSONValue?, E, R>(downstream) {

    override suspend fun acceptObject(value: JSONValue?) {
        @Suppress("UNCHECKED_CAST")
        emit(JSONDeserializer.deserialize(type, value, config) as E)
    }

    companion object {

        inline fun <reified E: Any, R> create(downstream: CoAcceptor<E, R>,
                config: JSONConfig = JSONConfig.defaultConfig) =
                        JSONDeserializerCoPipeline(JSONTypeRef.create<E>().refType, downstream, config)

    }

}
