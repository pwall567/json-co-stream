/*
 * @(#) JSONArrayProcessor.kt
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

import net.pwall.json.JSONException
import net.pwall.json.JSONValue

class JSONArrayProcessor(openingBracketSeen: Boolean = false, val consume: suspend (JSONValue?) -> Unit) {

    enum class State { INITIAL, FIRST, ENTRY, COMMA, COMPLETE }

    private var state: State = if (openingBracketSeen) State.FIRST else State.INITIAL
    private var child: JSONCoValueBuilder = JSONCoValueBuilder()

    val complete: Boolean
        get() = state == State.COMPLETE

    suspend fun acceptInt(ch: Int) {
        when (state) {
            State.INITIAL -> {
                if (!JSONCoBuilder.isWhitespace(ch)) {
                    if (ch == OPENING_BRACKET)
                        state = State.FIRST
                    else
                        throw JSONException("Pipeline must contain array")
                }
            }
            State.FIRST -> {
                if (!JSONCoBuilder.isWhitespace(ch)) {
                    state = when (ch) {
                        CLOSING_BRACKET -> State.COMPLETE
                        else -> {
                            child.acceptChar(ch) // always true for first character
                            State.ENTRY
                        }
                    }
                }
            }
            State.ENTRY -> {
                val consumed = child.acceptChar(ch)
                if (child.complete) {
                    consume(child.result)
                    state = State.COMMA
                }
                if (!consumed) {
                    state = State.COMMA
                    expectComma(ch)
                }
            }
            State.COMMA -> expectComma(ch)
            State.COMPLETE -> JSONCoBuilder.checkWhitespace(ch)
        }
    }

    private fun expectComma(ch: Int) {
        if (!JSONCoBuilder.isWhitespace(ch)) {
            state = when (ch) {
                COMMA -> {
                    child = JSONCoValueBuilder()
                    State.ENTRY
                }
                CLOSING_BRACKET -> State.COMPLETE
                else -> throw JSONException("Illegal syntax in JSON array")
            }
        }
    }

    companion object {
        const val OPENING_BRACKET = '['.toInt()
        const val CLOSING_BRACKET = ']'.toInt()
        const val COMMA = ','.toInt()
    }

}
