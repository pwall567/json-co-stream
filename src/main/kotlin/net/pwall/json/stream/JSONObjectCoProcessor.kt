package net.pwall.json.stream

import net.pwall.json.JSONConfig
import net.pwall.json.JSONDeserializerFunctions.findParameterName
import net.pwall.json.JSONException
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.KType
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.starProjectedType

class JSONObjectCoProcessor(private val path: String, private val targetType: KType, private val config: JSONConfig,
        openingBraceSeen: Boolean = false, val consume: suspend (Pair<String, JSONCoBuilder>) -> Unit) {

    enum class State { INITIAL, FIRST, NAME, COLON, VALUE, COMMA, NEXT, COMPLETE }

    private val targetClass = targetType.classifier as? KClass<*> ?:
            throw JSONException("$path: Can't deserialize $targetType")
    private var state: State = if (openingBraceSeen) State.FIRST else State.INITIAL
    private var child: JSONCoBuilder = JSONCoStringBuilder(path, stringType)
    private var name: String = ""
    private val keys = mutableSetOf<String>()

    val complete: Boolean
        get() = state == State.COMPLETE

    suspend fun acceptInt(ch: Int) {
        when (state) {
            State.INITIAL -> {
                if (!JSONCoBuilder.isWhitespace(ch)) {
                    if (ch == OPENING_BRACE)
                        state = State.FIRST
                    else
                        throw JSONException("$path: Expecting opening brace of object")
                }
            }
            State.FIRST -> {
                if (!JSONCoBuilder.isWhitespace(ch)) {
                    state = when (ch) {
                        CLOSING_BRACE -> State.COMPLETE
                        QUOTE -> State.NAME
                        else -> throw JSONException("$path: Illegal syntax in JSON object")
                    }
                }
            }
            State.NAME -> {
                child.acceptChar(ch)
                if (child.complete) {
                    name = child.rawValue.toString()
                    if (keys.contains(name))
                        throw JSONException("$path: Duplicate key in JSON object")
                    state = State.COLON
                }
            }
            State.COLON -> {
                if (!JSONCoBuilder.isWhitespace(ch)) {
                    if (ch == COLON) {
                        val childPath = if (path.isEmpty()) name else "$path.$name"
                        child = JSONCoValueBuilder(childPath, determineMemberType(), config)
                        state = State.VALUE
                    }
                    else
                        throw JSONException("$path: Illegal syntax in JSON object")
                }
            }
            State.VALUE -> {
                val consumed = child.acceptChar(ch)
                if (child.complete) {
                    consume(name to child)
                    keys.add(name)
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
                    if (ch == QUOTE) {
                        child = JSONCoStringBuilder(path, stringType)
                        state = State.NAME
                    }
                    else
                        throw JSONException("$path: Illegal syntax in JSON object")
                }
            }
            State.COMPLETE -> JSONCoBuilder.checkWhitespace(ch)
        }
    }

    private fun expectComma(ch: Int) {
        if (!JSONCoBuilder.isWhitespace(ch)) {
            state = when (ch) {
                COMMA -> State.NEXT
                CLOSING_BRACE -> State.COMPLETE
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
        const val OPENING_BRACE = '{'.toInt()
        const val CLOSING_BRACE = '}'.toInt()
        const val QUOTE = '"'.toInt()
        const val COLON = ':'.toInt()
        const val COMMA = ','.toInt()
        val stringType = String::class.starProjectedType
    }

}
