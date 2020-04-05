/*
 * @(#) JSONCoValueBuilder.kt
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

import net.pwall.json.JSONBoolean
import net.pwall.json.JSONException
import net.pwall.json.JSONValue

class JSONCoValueBuilder : JSONCoBuilder {

    private var delegate: JSONCoBuilder? = null

    override val complete: Boolean
        get() = delegate?.complete ?: false

    override val result: JSONValue?
        get() = delegate.let { if (it != null && it.complete) it.result else throw JSONException("JSON not complete") }

    override suspend fun acceptChar(ch: Int): Boolean {
        delegate.let {
            if (it == null) {
                if (!JSONCoBuilder.isWhitespace(ch)) {
                    delegate = when (ch.toChar()) {
                        '{' -> JSONCoObjectBuilder()
                        '[' -> JSONCoArrayBuilder()
                        '"' -> JSONCoStringBuilder()
                        '-', in '0'..'9' -> JSONCoNumberBuilder(ch.toChar())
                        't' -> JSONCoKeywordBuilder("true", JSONBoolean.TRUE)
                        'f' -> JSONCoKeywordBuilder("false", JSONBoolean.FALSE)
                        'n' -> JSONCoKeywordBuilder("null", null)
                        else -> throw JSONException("Illegal syntax in JSON")
                    }
                }
                return true
            }
            if (it.complete) {
                JSONCoBuilder.checkWhitespace(ch)
                return true
            }
            return it.acceptChar(ch)
        }
    }

    override fun close() {
        delegate.let {
            if (it != null) {
                if (!it.complete)
                    it.close()
            }
            else
                throw JSONException("JSON value not complete")
        }
    }

}
