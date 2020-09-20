/*
 * @(#) JSONCoObjectBuilder.kt
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
import net.pwall.json.JSONDeserializerFunctions.findParameterName
import net.pwall.json.JSONException
import net.pwall.json.JSONObject
import net.pwall.json.JSONValue
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.KType
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.starProjectedType

class JSONCoObjectBuilder(private val path: String, private val targetType: KType, private val config: JSONConfig) :
        JSONCoBuilder {

    enum class State { INITIAL, NAME, COLON, VALUE, COMMA, NEXT, COMPLETE }

    private val targetClass = targetType.classifier as? KClass<*> ?:
            throw JSONException("$path: Can't deserialize $targetType")
    private val entries = LinkedHashMap<String, JSONValue?>()
    private var state: State = State.INITIAL
    private var child: JSONCoBuilder = JSONCoStringBuilder(path, stringType)
    private var name: String = ""

    override val complete: Boolean
        get() = state == State.COMPLETE

    override val rawValue: Any?
        get() = TODO("Not yet implemented")

    override val jsonValue: JSONValue
        get() = if (complete) JSONObject(entries) else throw JSONException("$path: JSON object not complete")

    override suspend fun acceptChar(ch: Int): Boolean {
        when (state) {
            State.INITIAL -> {
                if (!JSONCoBuilder.isWhitespace(ch)) {
                    state = when (ch) {
                        '}'.toInt() -> State.COMPLETE
                        '"'.toInt() -> State.NAME
                        else -> throw JSONException("$path: Illegal syntax in JSON object")
                    }
                }
            }
            State.NAME -> {
                child.acceptChar(ch)
                if (child.complete) {
                    name = child.rawValue.toString()
                    if (entries.containsKey(name))
                        throw JSONException("$path: Duplicate key in JSON object")
                    state = State.COLON
                }
            }
            State.COLON -> {
                if (!JSONCoBuilder.isWhitespace(ch)) {
                    if (ch == ':'.toInt()) {
                        val childPath = if (path.isEmpty()) name else "$path.$name"
                        child = JSONCoValueBuilder(childPath, determineMemberType())
                        state = State.VALUE
                    }
                    else
                        throw JSONException("$path: Illegal syntax in JSON object")
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
                    if (ch == '"'.toInt()) {
                        child = JSONCoStringBuilder(path, stringType)
                        state = State.NAME
                    }
                    else
                        throw JSONException("$path: Illegal syntax in JSON object")
                }
            }
            State.COMPLETE -> JSONCoBuilder.checkWhitespace(ch)
        }
        return true
    }

    private fun expectComma(ch: Int) {
        if (!JSONCoBuilder.isWhitespace(ch)) {
            state = when (ch) {
                ','.toInt() -> State.NEXT
                '}'.toInt() -> State.COMPLETE
                else -> throw JSONException("$path: Illegal syntax in JSON object")
            }
        }
    }

    private fun determineMemberType(): KType {
        if (targetClass.isSubclassOf(Map::class)) // TODO is this the best way of testing this?
            return targetType.arguments.getOrNull(1)?.type ?: JSONCoValueBuilder.anyQType
        for (constructor in targetClass.constructors) {
            for (parameter in constructor.parameters) {
                if (name == findParameterName(parameter, config))
                    return parameter.type
            }
        }
        for (member in targetClass.members) {
            if (member is KProperty<*> && (config.findNameFromAnnotation(member.annotations) ?: member.name) == name)
                return member.returnType
        }
        return JSONCoValueBuilder.anyQType
    }

    companion object {
        val stringType = String::class.starProjectedType
    }

}
