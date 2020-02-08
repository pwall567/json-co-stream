package net.pwall.json.stream

import net.pwall.json.JSONException
import net.pwall.json.JSONString

class JSONCoStringBuilder : JSONCoBuilder {

    enum class State { NORMAL, BACKSLASH, UNICODE1, UNICODE2, UNICODE3, UNICODE4, COMPLETE }

    private val sb = StringBuilder()
    private var state: State = State.NORMAL
    private var unicode = 0

    override val complete: Boolean
        get() = state == State.COMPLETE

    override val result: JSONString
        get() = if (complete) JSONString(sb) else throw JSONException("Unterminated JSON string")

    override suspend fun acceptChar(ch: Int): Boolean {
        when (state) {
            State.NORMAL -> acceptNormal(ch)
            State.BACKSLASH -> acceptBackslash(ch)
            State.UNICODE1 -> acceptUnicode(ch.toChar(), State.UNICODE2)
            State.UNICODE2 -> acceptUnicode(ch.toChar(), State.UNICODE3)
            State.UNICODE3 -> acceptUnicode(ch.toChar(), State.UNICODE4)
            State.UNICODE4 -> {
                acceptUnicode(ch.toChar(), State.NORMAL)
                store(unicode.toChar())
            }
            State.COMPLETE -> JSONCoBuilder.checkWhitespace(ch)
        }
        return true
    }

    private fun acceptNormal(ch: Int) {
        when {
            ch == '"'.toInt() -> state = State.COMPLETE
            ch == '\\'.toInt() -> state = State.BACKSLASH
            ch <= 0x1F -> throw JSONException("Illegal character in JSON string")
            Character.isBmpCodePoint(ch) -> sb.append(ch.toChar())
            else -> {
                sb.append(Character.highSurrogate(ch))
                sb.append(Character.lowSurrogate(ch))
            }
        }
    }

    private fun acceptBackslash(ch: Int) {
        when (ch) {
            '"'.toInt(), '\\'.toInt(), '/'.toInt() -> store(ch.toChar())
            'b'.toInt() -> store('\b')
            'f'.toInt() -> store('\u000C')
            'n'.toInt() -> store('\n')
            'r'.toInt() -> store('\r')
            't'.toInt() -> store('\t')
            'u'.toInt() -> state = State.UNICODE1
            else -> throw JSONException("Illegal escape sequence in JSON string")
        }
    }

    private fun store(ch: Char) {
        sb.append(ch)
        state = State.NORMAL
    }

    private fun acceptUnicode(ch: Char, nextState: State) {
        val digit = when (ch) {
            in '0'..'9' -> ch.toInt() - '0'.toInt()
            in 'A'..'F' -> ch.toInt() - 'A'.toInt() + 10
            in 'a'..'f' -> ch.toInt() - 'a'.toInt() + 10
            else -> throw JSONException("Illegal Unicode sequence in JSON string")
        }
        unicode = (unicode shl 4) or digit
        state = nextState
    }

}
