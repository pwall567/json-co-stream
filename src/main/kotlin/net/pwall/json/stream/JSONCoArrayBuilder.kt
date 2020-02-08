package net.pwall.json.stream

import net.pwall.json.JSONArray
import net.pwall.json.JSONException
import net.pwall.json.JSONValue

class JSONCoArrayBuilder : JSONCoBuilder {

    enum class State { INITIAL, ENTRY, COMMA, COMPLETE }

    private val entries = ArrayList<JSONValue?>()
    private var state: State = State.INITIAL
    private var child: JSONCoBuilder = JSONCoValueBuilder()

    override val complete: Boolean
        get() = state == State.COMPLETE

    override val result: JSONArray
        get() = if (complete) JSONArray(entries) else throw JSONException("Array not complete")

    override suspend fun acceptChar(ch: Int): Boolean {
        when (state) {
            State.INITIAL -> {
                if (!JSONCoBuilder.isWhitespace(ch)) {
                    state = when (ch) {
                        ']'.toInt() -> State.COMPLETE
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
                    entries.add(child.result)
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
        return true
    }

    private fun expectComma(ch: Int) {
        if (!JSONCoBuilder.isWhitespace(ch)) {
            state = when (ch) {
                ','.toInt() -> {
                    child = JSONCoValueBuilder()
                    State.ENTRY
                }
                ']'.toInt() -> State.COMPLETE
                else -> throw JSONException("Illegal syntax in JSON array")
            }
        }
    }

}
