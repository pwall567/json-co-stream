package net.pwall.json.stream

import net.pwall.json.JSONValue
import net.pwall.util.pipeline.AbstractIntCoAcceptor

class JSONCoStream : AbstractIntCoAcceptor<JSONValue?>() {

    private val delegate = JSONCoValueBuilder()
    private var started = false

    override val result: JSONValue?
        get() = delegate.result

    override suspend fun acceptInt(value: Int) {
        if (!started) {
            started = true
            if (value == BOM)
                return
        }
        while (true) {
            if (delegate.acceptChar(value))
                break
        }
    }

    override fun close() {
        delegate.close()
    }

    companion object {
        const val BOM = 0xFEFF
    }

}
