/*
 * @(#) JSONCoNumberBuilder.kt
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

import net.pwall.json.JSONDecimal
import net.pwall.json.JSONException
import net.pwall.json.JSONInteger
import net.pwall.json.JSONLong
import net.pwall.json.JSONValue
import net.pwall.json.JSONZero

class JSONCoNumberBuilder(initialChar: Char) : JSONCoBuilder {

    enum class State { MINUS_SEEN, ZERO_SEEN, INTEGER, DOT_SEEN, FRACTION, E_SEEN, E_SIGN_SEEN, EXPONENT, COMPLETE }

    private val number = StringBuilder().apply {
        append(initialChar)
    }

    private var state: State = when (initialChar) {
        '-' -> State.MINUS_SEEN
        '0' -> State.ZERO_SEEN
        in '1'..'9' -> State.INTEGER
        else -> throw JSONException("Illegal JSON number")
    }

    private var floating = false

    override val complete: Boolean
        get() = state == State.COMPLETE

    override val result: JSONValue
        get() = when {
            !complete -> throw JSONException("Number not complete")
            number.length == 1 && number[0] == '0' -> JSONZero.ZERO
            floating -> JSONDecimal(number.toString())
            else -> {
                val longValue = number.toString().toLong()
                val intValue = longValue.toInt()
                if (intValue.toLong() == longValue) JSONInteger(intValue) else JSONLong(longValue)
            }
        }

    override suspend fun acceptChar(ch: Int): Boolean {
        when (state) {
            State.MINUS_SEEN -> state = when (ch.toChar()) {
                '0' -> State.ZERO_SEEN
                in '1'..'9' -> State.INTEGER
                else -> throw JSONException("Illegal JSON number")
            }
            State.ZERO_SEEN -> state = when (ch.toChar()) {
                '.' -> State.DOT_SEEN
                'e', 'E' -> State.E_SEEN
                else -> State.COMPLETE
            }
            State.INTEGER -> {
                if (ch.toChar() !in '0'..'9') {
                    state = when (ch.toChar()) {
                        '.' -> State.DOT_SEEN
                        'e', 'E' -> State.E_SEEN
                        else -> State.COMPLETE
                    }
                }
            }
            State.DOT_SEEN -> {
                floating = true
                if (ch.toChar() in '0'..'9')
                    state = State.FRACTION
                else
                    throw JSONException("Illegal JSON number")
            }
            State.FRACTION -> {
                if (ch.toChar() !in '0'..'9') {
                    state = when (ch.toChar()) {
                        'e', 'E' -> State.E_SEEN
                        else -> State.COMPLETE
                    }
                }
            }
            State.E_SEEN -> {
                floating = true
                state = when (ch.toChar()) {
                    '-', '+' -> State.E_SIGN_SEEN
                    in '0'..'9' -> State.EXPONENT
                    else -> throw JSONException("Illegal JSON number")
                }
            }
            State.E_SIGN_SEEN -> state = when (ch.toChar()) {
                in '0'..'9' -> State.EXPONENT
                else -> throw JSONException("Illegal JSON number")
            }
            State.EXPONENT -> {
                if (ch.toChar() !in '0'..'9')
                    state = State.COMPLETE
            }
            State.COMPLETE -> {
                JSONCoBuilder.checkWhitespace(ch)
                return true
            }
        }
        if (complete)
            return false
        store(ch.toChar())
        return true
    }

    override fun close() {
        when (state) {
            State.MINUS_SEEN, State.DOT_SEEN, State.E_SEEN, State.E_SIGN_SEEN ->
                throw JSONException("Illegal JSON number")
            else -> state = State.COMPLETE
        }
    }

    private fun store(ch: Char) {
        number.append(ch)
    }

}
