/*
 * @(#) JSONCoBuilder.kt
 *
 * json-co-stream Kotlin coroutine JSON Streams
 * Copyright (c) 2020, 2023 Peter Wall
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

import net.pwall.json.JSONException
import net.pwall.json.JSONValue

interface JSONCoBuilder {

    val complete: Boolean
    val result: JSONValue?

    suspend fun acceptChar(ch: Int): Boolean

    fun close() {
        if (!complete)
            throw JSONException("Unexpected end of data")
    }

    companion object {
        fun isWhitespace(ch: Int) = ch == ' '.code || ch == '\t'.code || ch == '\n'.code || ch == '\r'.code
        fun checkWhitespace(ch: Int) {
            if (!isWhitespace(ch))
                throw JSONException("Unexpected characters at end of JSON")
        }
    }

}
