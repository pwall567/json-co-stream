package net.pwall.json.stream

import net.pwall.json.JSONException
import net.pwall.json.JSONValue
import net.pwall.util.pipeline.CoAbstractIntObjectPipeline
import net.pwall.util.pipeline.CoAcceptor

class JSONCoArrayPipeline<R>(valueConsumer: CoAcceptor<JSONValue, R>) :
        CoAbstractIntObjectPipeline<JSONValue, R>(valueConsumer) {

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

            }
        }
    }

}
