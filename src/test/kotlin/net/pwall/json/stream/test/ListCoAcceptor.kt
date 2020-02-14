package net.pwall.json.stream.test

import net.pwall.util.pipeline.AbstractCoAcceptor

class ListCoAcceptor<A> : AbstractCoAcceptor<A, List<A>>() {

    private val list = ArrayList<A>()

    override val result: List<A>
        get() = list

    override suspend fun acceptObject(value: A) {
        list.add(value)
    }

}
