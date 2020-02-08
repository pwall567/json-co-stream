package net.pwall.json.stream

import net.pwall.json.JSONException
import net.pwall.json.JSONValue

interface JSONCoBuilder {
    val complete: Boolean
    val result: JSONValue?
    suspend fun acceptChar(ch: Int): Boolean
    fun close() {
        if (!complete)
            throw JSONException("Unexpected end of data")
    }
    companion object {
        fun isWhitespace(ch: Int) = ch == ' '.toInt() || ch == '\t'.toInt() || ch == '\n'.toInt() || ch == '\r'.toInt()
        fun checkWhitespace(ch: Int) {
            if (!isWhitespace(ch))
                throw JSONException("Unexpected characters at end of JSON")
        }
    }
}
