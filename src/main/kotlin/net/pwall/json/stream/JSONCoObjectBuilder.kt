/*
 * @(#) JSONCoObjectBuilder.kt
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
import net.pwall.json.JSONObject
import net.pwall.json.JSONValue

class JSONCoObjectBuilder : JSONCoBuilder {

    enum class State { INITIAL, NAME, COLON, VALUE, COMMA, NEXT, COMPLETE }

    private val entries = LinkedHashMap<String, JSONValue?>()
    private var state: State = State.INITIAL
    private var child: JSONCoBuilder = JSONCoStringBuilder()
    private var name: String = ""

    override val complete: Boolean
        get() = state == State.COMPLETE

    override val result: JSONValue
        get() = if (complete) JSONObject(entries) else throw JSONException("JSON object not complete")

    override suspend fun acceptChar(ch: Int): Boolean {
        when (state) {
            State.INITIAL -> {
                if (!JSONCoBuilder.isWhitespace(ch)) {
                    state = when (ch) {
                        '}'.code -> State.COMPLETE
                        '"'.code -> State.NAME
                        else -> throw JSONException("Illegal syntax in JSON object")
                    }
                }
            }
            State.NAME -> {
                child.acceptChar(ch)
                if (child.complete) {
                    name = child.result.toString()
                    if (entries.containsKey(name))
                        throw JSONException("Duplicate key in JSON object")
                    state = State.COLON
                }
            }
            State.COLON -> {
                if (!JSONCoBuilder.isWhitespace(ch)) {
                    if (ch == ':'.code) {
                        child = JSONCoValueBuilder()
                        state = State.VALUE
                    }
                    else
                        throw JSONException("Illegal syntax in JSON object")
                }
            }
            State.VALUE -> {
                val consumed = child.acceptChar(ch)
                if (child.complete) {
                    entries[name] = child.result
                    state = State.COMMA
                }
                if (!consumed) {
                    state = State.COMMA
                    expectComma(ch)
                }
            }
            State.COMMA -> expectComma(ch)
            State.NEXT -> {
                if (!JSONCoBuilder.isWhitespace(ch)) {
                    if (ch == '"'.code) {
                        child = JSONCoStringBuilder()
                        state = State.NAME
                    }
                    else
                        throw JSONException("Illegal syntax in JSON object")
                }
            }
            State.COMPLETE -> JSONCoBuilder.checkWhitespace(ch)
        }
        return true
    }

    private fun expectComma(ch: Int) {
        if (!JSONCoBuilder.isWhitespace(ch)) {
            state = when (ch) {
                ','.code -> State.NEXT
                '}'.code -> State.COMPLETE
                else -> throw JSONException("Illegal syntax in JSON object")
            }
        }
    }

}
