/*
 * @(#) JSONCoArrayBuilder.kt
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

import kotlin.reflect.KType

import net.pwall.json.JSONArray
import net.pwall.json.JSONConfig
import net.pwall.json.JSONException

class JSONCoArrayBuilder(private val path: String, targetType: KType, config: JSONConfig) : JSONCoBuilder {

    private val results = ArrayList<JSONCoValueBuilder>()
    private val arrayProcessor = JSONArrayCoProcessor(path, targetType, config, true) {
        results.add(it)
    }

    override val complete: Boolean
        get() = arrayProcessor.complete

    override val rawValue: Any?
        get() = if (complete) results.map { it.rawValue } else throw JSONException("$path: Array not complete")

    override val jsonValue: JSONArray
        get() = if (complete) JSONArray(results.map { it.jsonValue })
                else throw JSONException("$path: Array not complete")

    override suspend fun acceptChar(ch: Int): Boolean {
        arrayProcessor.acceptInt(ch)
        return true
    }

}
