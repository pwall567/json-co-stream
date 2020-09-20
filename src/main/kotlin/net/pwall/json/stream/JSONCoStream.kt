/*
 * @(#) JSONCoStream.kt
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
import net.pwall.json.JSONValue
import net.pwall.util.pipeline.AbstractIntCoAcceptor
import kotlin.reflect.KType

class JSONCoStream(targetType: KType = JSONCoValueBuilder.anyQType, config: JSONConfig = JSONConfig.defaultConfig) :
        AbstractIntCoAcceptor<JSONValue?>() {

    private val delegate = JSONCoValueBuilder("", targetType, config)
    private var started = false

    override val result: JSONValue?
        get() = delegate.jsonValue

    override suspend fun acceptInt(value: Int) {
        if (!started) {
            started = true
            if (value == BOM)
                return
        }
        while (true) {
            if (delegate.acceptChar(value))
                break
        }
    }

    override fun close() {
        delegate.close()
    }

    companion object {
        const val BOM = 0xFEFF
    }

}
