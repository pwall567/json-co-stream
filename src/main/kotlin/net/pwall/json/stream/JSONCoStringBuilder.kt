/*
 * @(#) JSONCoStringBuilder.kt
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

import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.isSuperclassOf
import kotlin.reflect.full.staticFunctions

import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.MonthDay
import java.time.OffsetDateTime
import java.time.OffsetTime
import java.time.Period
import java.time.Year
import java.time.YearMonth
import java.time.ZonedDateTime
import java.util.Calendar
import java.util.Date
import java.util.UUID

import net.pwall.json.JSONException
import net.pwall.json.JSONString
import net.pwall.json.JSONDeserializerFunctions.hasSingleParameter
import net.pwall.util.ISO8601Date

class JSONCoStringBuilder(private val path: String, private val targetType: KType) : JSONCoBuilder {

    private val targetClass =
            targetType.classifier as? KClass<*> ?: throw JSONException("$path: Can't deserialize $targetType")

    enum class State { NORMAL, BACKSLASH, UNICODE1, UNICODE2, UNICODE3, UNICODE4, COMPLETE }

    private val sb = StringBuilder()
    private val str: String
        get() = sb.toString()
    private var state: State = State.NORMAL
    private var unicode = 0

    override val complete: Boolean
        get() = state == State.COMPLETE

    override val rawValue: Any?
        get() {
            if (!complete)
                throw JSONException("$path: Unterminated JSON string")
            if (targetClass.isSuperclassOf(String::class))
                return str
            if (targetClass == Char::class && sb.length == 1)
                return sb[0]
            try {
                when (targetClass) {
                    CharArray::class -> return str.toCharArray()
                    Array<Char>::class -> return Array(sb.length) { i -> sb[i] }
                    java.sql.Date::class -> return java.sql.Date.valueOf(str)
                    java.sql.Time::class -> return java.sql.Time.valueOf(str)
                    java.sql.Timestamp::class -> return java.sql.Timestamp.valueOf(str)
                    Calendar::class -> return ISO8601Date.decode(str)
                    Date::class -> return ISO8601Date.decode(str).time
                    Instant::class -> return Instant.parse(str)
                    LocalDate::class -> return LocalDate.parse(str)
                    LocalDateTime::class -> return LocalDateTime.parse(str)
                    LocalTime::class -> return LocalTime.parse(str)
                    OffsetTime::class -> return OffsetTime.parse(str)
                    OffsetDateTime::class -> return OffsetDateTime.parse(str)
                    ZonedDateTime::class -> return ZonedDateTime.parse(str)
                    Year::class -> return Year.parse(str)
                    YearMonth::class -> return YearMonth.parse(str)
                    MonthDay::class -> return MonthDay.parse(str)
                    Duration::class -> return Duration.parse(str)
                    Period::class -> return Period.parse(str)
                    UUID::class -> return UUID.fromString(str)
                }
            }
            catch (e: JSONException) {
                throw e
            }
            catch (e: Exception) {
                throw JSONException("$path: Can't deserialize \"$sb\" as $targetType", e)
            }
            if (targetClass.isSubclassOf(Enum::class))
                targetClass.staticFunctions.find { it.name == "valueOf" }?.let { return it.call(str) }
            targetClass.constructors.find { it.hasSingleParameter(String::class) }?.apply { return call(str) }
            throw JSONException("$path: Can't deserialize \"$sb\" as $targetType")
        }

    override val jsonValue: JSONString
        get() = if (complete) JSONString(sb) else throw JSONException("$path: Unterminated JSON string")

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
        const val DIGIT_0 = '0'.toInt()
        const val DIGIT_9 = '9'.toInt()
        const val LETTER_A = 'A'.toInt()
        const val LETTER_F = 'F'.toInt()
        const val LETTER_a = 'a'.toInt()
        const val LETTER_b = 'b'.toInt()
        const val LETTER_f = 'f'.toInt()
        const val LETTER_n = 'n'.toInt()
        const val LETTER_r = 'r'.toInt()
        const val LETTER_t = 't'.toInt()
        const val LETTER_u = 'u'.toInt()
        const val DOUBLE_QUOTE = '"'.toInt()
        const val BACKSLASH = '\\'.toInt()
        const val SLASH = '/'.toInt()
    }

}

