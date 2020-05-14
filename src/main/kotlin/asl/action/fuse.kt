package asl.action

import jason.asSemantics.DefaultInternalAction
import jason.asSemantics.TransitionSystem
import jason.asSemantics.Unifier
import jason.asSyntax.ListTerm
import jason.asSyntax.ListTermImpl
import jason.asSyntax.Term
import common.translation.LiteralBuilder
import common.translation.LiteralBuilder.Companion.pairTerm
import common.translation.LiteralParser.getValue
import common.translation.LiteralParser.split
import common.translation.LiteralParser.splitStructAndList

class fuse : DefaultInternalAction() {
    override fun execute(ts: TransitionSystem, un: Unifier, args: Array<Term>): Any {
        val items: List<String> = split(args[0].toString())
        val positions: List<String> = split(args[1].toString())
        val result: ListTerm = ListTermImpl()
        result.addAll(items.map { i ->
            LiteralBuilder("item").setValues(
                    pairTerm("id", getValue(i, "id")!!),
                    pairTerm("quantity", getValue(i, "quantity")!!.toDouble())
            ).setQueue(*split(splitStructAndList(positions
                    .first { p: String? -> getValue(i, "id") == getValue(p!!, "id") }).second)
                    .map{ p -> LiteralBuilder("position").setValues(
                        pairTerm("rack", getValue(p, "rack")!!),
                        pairTerm("shelf", getValue(p, "shelf")!!),
                        pairTerm("quantity", getValue(p, "quantity")!!)
                    ).build() }.toTypedArray()
            ).build()
        } )
        return un.unifies(result, args[2])
    }

    companion object {
        private const val ITEM_POS_MISSING = "The items has not been found in the positions list"
    }
}