package net.pwall.json.stream

import net.pwall.json.JSONBoolean
import net.pwall.json.JSONException
import net.pwall.json.JSONValue

class JSONCoValueBuilder : JSONCoBuilder {

    private var delegate: JSONCoBuilder? = null

    override val complete: Boolean
        get() = delegate?.complete ?: false

    override val result: JSONValue?
        get() = delegate.let { if (it != null && it.complete) it.result else throw JSONException("JSON not complete") }

    override suspend fun acceptChar(ch: Int): Boolean {
        delegate.let {
            if (it == null) {
                if (!JSONCoBuilder.isWhitespace(ch)) {
                    delegate = when (ch.toChar()) {
                        '{' -> JSONCoObjectBuilder()
                        '[' -> JSONCoArrayBuilder()
                        '"' -> JSONCoStringBuilder()
                        '-', in '0'..'9' -> JSONCoNumberBuilder(ch.toChar())
                        't' -> JSONCoKeywordBuilder("true", JSONBoolean.TRUE)
                        'f' -> JSONCoKeywordBuilder("false", JSONBoolean.FALSE)
                        'n' -> JSONCoKeywordBuilder("null", null)
                        else -> throw JSONException("Illegal syntax in JSON")
                    }
                }
                return true
            }
            if (it.complete) {
                JSONCoBuilder.checkWhitespace(ch)
                return true
            }
            return it.acceptChar(ch)
        }
    }

    override fun close() {
        delegate.let {
            if (it != null) {
                if (!it.complete)
                    it.close()
            }
            else
                throw JSONException("JSON value not complete")
        }
    }

}
