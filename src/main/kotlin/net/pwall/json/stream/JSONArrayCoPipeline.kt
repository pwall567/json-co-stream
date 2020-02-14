package net.pwall.json.stream

import net.pwall.json.JSONException
import net.pwall.json.JSONValue
import net.pwall.util.pipeline.AbstractIntObjectCoPipeline
import net.pwall.util.pipeline.CoAcceptor

class JSONArrayCoPipeline<R>(valueConsumer: CoAcceptor<JSONValue?, R>) :
        AbstractIntObjectCoPipeline<JSONValue?, R>(valueConsumer) {

    enum class State { INITIAL, FIRST, ENTRY, COMMA, COMPLETE }

    private var state: State = State.INITIAL
    private var child: JSONCoValueBuilder = JSONCoValueBuilder()

    override val complete: Boolean
        get() = state == State.COMPLETE

    override suspend fun acceptInt(value: Int) {
        when (state) {
            State.INITIAL -> {
                if (!JSONCoBuilder.isWhitespace(value)) {
                    if (value.toChar() == '[')
                        state = State.FIRST
                    else
                        throw JSONException("Pipeline must contain array")
                }
            }
            State.FIRST -> {
                if (!JSONCoBuilder.isWhitespace(value)) {
                    if (value.toChar() == ']')
                        state = State.COMPLETE
                    else {
                        state = State.ENTRY
                        child.acceptChar(value)
                        // always true for first character
                    }
                }
            }
            State.ENTRY -> {
                val consumed = child.acceptChar(value)
                if (child.complete) {
                    emit(child.result)
                    state = State.COMMA
                }
                if (!consumed) {
                    state = State.COMMA
                    expectComma(value)
                }
            }
            State.COMMA -> expectComma(value)
            State.COMPLETE -> JSONCoBuilder.checkWhitespace(value)
        }
    }

    private fun expectComma(ch: Int) {
        if (!JSONCoBuilder.isWhitespace(ch)) {
            state = when (ch.toChar()) {
                ',' -> {
                    child = JSONCoValueBuilder()
                    State.ENTRY
                }
                ']' -> State.COMPLETE
                else -> throw JSONException("Illegal syntax in JSON array")
            }
        }
    }

    override fun close() {
        if (!complete)
            throw JSONException("Unexpected end of data in JSON array")
    }

}
