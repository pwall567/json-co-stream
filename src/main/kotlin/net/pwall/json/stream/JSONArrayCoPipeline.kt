/*
 * @(#) JSONArrayCoPipeline.kt
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

import net.pwall.json.JSONConfig
import kotlin.reflect.KType

import net.pwall.json.JSONException
import net.pwall.json.JSONValue
import net.pwall.util.pipeline.AbstractIntObjectCoPipeline
import net.pwall.util.pipeline.CoAcceptor

/**
 * A (coroutine) pipeline that accepts a JSON array as a stream of Unicode code points and emits a `JSONValue` for each
 * item in the array.
 *
 * @constructor
 * @param   valueConsumer   the `JSONValue` consumer
 * @param   R               the pipeline result type
 * @author  Peter Wall
 */
class JSONArrayCoPipeline<R>(targetType: KType, valueConsumer: CoAcceptor<JSONValue?, R>,
        config: JSONConfig = JSONConfig.defaultConfig) : AbstractIntObjectCoPipeline<JSONValue?, R>(valueConsumer) {

    private val arrayProcessor = JSONArrayCoProcessor("", targetType, config) {
        emit(it.jsonValue)
    }

    override val complete: Boolean
        get() = arrayProcessor.complete

    override suspend fun acceptInt(value: Int) {
        arrayProcessor.acceptInt(value)
    }

    override fun close() {
        if (!complete)
            throw JSONException("Unexpected end of data in JSON array")
    }

}
