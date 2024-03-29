/*
 * @(#) JSONCoStringBuilder.kt
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
import net.pwall.json.JSONString

class JSONCoStringBuilder : JSONCoBuilder {

    enum class State { NORMAL, BACKSLASH, UNICODE1, UNICODE2, UNICODE3, UNICODE4, COMPLETE }

    private val sb = StringBuilder()
    private var state: State = State.NORMAL
    private var unicode = 0

    override val complete: Boolean
        get() = state == State.COMPLETE

    override val result: JSONString
        get() = if (complete) JSONString(sb) else throw JSONException("Unterminated JSON string")

    override suspend fun acceptChar(ch: Int): Boolean {
        when (state) {
            State.NORMAL -> acceptNormal(ch)
            State.BACKSLASH -> acceptBackslash(ch)
            State.UNICODE1 -> acceptUnicode(ch, State.UNICODE2)
            State.UNICODE2 -> acceptUnicode(ch, State.UNICODE3)
            State.UNICODE3 -> acceptUnicode(ch, State.UNICODE4)
            State.UNICODE4 -> {
                acceptUnicode(ch, State.NORMAL)
                store(unicode.toChar())
            }
            State.COMPLETE -> JSONCoBuilder.checkWhitespace(ch)
        }
        return true
    }

    private fun acceptNormal(ch: Int) {
        when {
            ch == DOUBLE_QUOTE -> state = State.COMPLETE
            ch == BACKSLASH -> state = State.BACKSLASH
            ch <= 0x1F -> throw JSONException("Illegal character in JSON string")
            Character.isBmpCodePoint(ch) -> sb.append(ch.toChar())
            else -> {
                sb.append(Character.highSurrogate(ch))
                sb.append(Character.lowSurrogate(ch))
            }
        }
    }

    private fun acceptBackslash(ch: Int) {
        when (ch) {
            DOUBLE_QUOTE, BACKSLASH, SLASH -> store(ch.toChar())
            LETTER_b -> store('\b')
            LETTER_f -> store('\u000C')
            LETTER_n -> store('\n')
            LETTER_r -> store('\r')
            LETTER_t -> store('\t')
            LETTER_u -> state = State.UNICODE1
            else -> throw JSONException("Illegal escape sequence in JSON string")
        }
    }

    private fun store(ch: Char) {
        sb.append(ch)
        state = State.NORMAL
    }

    private fun acceptUnicode(ch: Int, nextState: State) {
        val digit = when (ch) {
            in DIGIT_0..DIGIT_9 -> ch - DIGIT_0
            in LETTER_A..LETTER_F -> ch - LETTER_A + 10
            in LETTER_a..LETTER_f -> ch - LETTER_a + 10
            else -> throw JSONException("Illegal Unicode sequence in JSON string")
        }
        unicode = (unicode shl 4) or digit
        state = nextState
    }

    companion object {
        const val DIGIT_0 = '0'.code
        const val DIGIT_9 = '9'.code
        const val LETTER_A = 'A'.code
        const val LETTER_F = 'F'.code
        const val LETTER_a = 'a'.code
        const val LETTER_b = 'b'.code
        const val LETTER_f = 'f'.code
        const val LETTER_n = 'n'.code
        const val LETTER_r = 'r'.code
        const val LETTER_t = 't'.code
        const val LETTER_u = 'u'.code
        const val DOUBLE_QUOTE = '"'.code
        const val BACKSLASH = '\\'.code
        const val SLASH = '/'.code
    }

}
