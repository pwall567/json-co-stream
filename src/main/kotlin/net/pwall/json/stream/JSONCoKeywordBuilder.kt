package net.pwall.json.stream

import net.pwall.json.JSONException
import net.pwall.json.JSONValue

class JSONCoKeywordBuilder(val keyword: String, val value: JSONValue?) : JSONCoBuilder {

    private var offset = 1

    override val complete: Boolean
        get() = offset == keyword.length

    override val result: JSONValue?
        get() = if (complete) value else throw JSONException("Keyword not complete")

    override suspend fun acceptChar(ch: Int): Boolean {
        if (complete)
            throw JSONException("Unexpected characters at end of JSON keyword")
        if (ch.toChar() != keyword[offset])
            throw JSONException("Illegal character in JSON keyword")
        offset++
        return true
    }

}
