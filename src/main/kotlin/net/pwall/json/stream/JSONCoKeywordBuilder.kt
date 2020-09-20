/*
 * @(#) JSONCoKeywordBuilder.kt
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

import net.pwall.json.JSONException
import net.pwall.json.JSONValue

class JSONCoKeywordBuilder(private val path: String, private val targetType: KType, private val keyword: String,
        private val rawResult: Any?, private val jsonResult: JSONValue?) : JSONCoBuilder {

    private var offset = 1

    override val complete: Boolean
        get() = offset == keyword.length

    override val rawValue: Any?
        get() = validate(rawResult)

    override val jsonValue: JSONValue?
        get() = validate(jsonResult)

    override suspend fun acceptChar(ch: Int): Boolean {
        if (complete)
            throw JSONException("$path: Unexpected characters at end of JSON keyword")
        if (ch.toChar() != keyword[offset])
            throw JSONException("$path: Illegal character in JSON keyword")
        offset++
        return true
    }

    private fun <T: Any> validate(result: T?): T? {
        if (!complete)
            throw JSONException("$path: Keyword not complete")
        if (result == null) {
            if (!targetType.isMarkedNullable)
                throw JSONException("$path: Can't deserialize null as $targetType")
        }
        else {
            if (!result::class.isSubclassOf(targetType.classifier as KClass<*>))
                throw JSONException("$path: Can't deserialize $keyword as $targetType")
        }
        return result
    }

}
