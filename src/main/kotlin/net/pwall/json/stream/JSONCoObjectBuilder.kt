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
                        '}'.toInt() -> State.COMPLETE
                        '"'.toInt() -> State.NAME
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
                    if (ch == ':'.toInt()) {
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
                    if (ch == '"'.toInt()) {
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
                ','.toInt() -> State.NEXT
                '}'.toInt() -> State.COMPLETE
                else -> throw JSONException("Illegal syntax in JSON object")
            }
        }
    }

}
