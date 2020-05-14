package asl.action

import jason.asSemantics.DefaultInternalAction
import jason.asSemantics.TransitionSystem
import jason.asSemantics.Unifier
import jason.asSyntax.StringTerm
import jason.asSyntax.StringTermImpl
import jason.asSyntax.Term
import common.translation.LiteralParser.split
import java.io.IOException
import java.util.concurrent.atomic.AtomicInteger
import java.util.function.Function
import java.util.function.Supplier
import java.util.stream.Collectors

class labelize : DefaultInternalAction() {
    @Throws(IOException::class)
    override fun execute(ts: TransitionSystem, un: Unifier, args: Array<Term>): Any {
        val script = (args[0] as StringTerm).string
        val plans: List<String> = split(script.substring(1, script.length - 1))
        val ai = AtomicInteger()
        val labeledPlans: StringTerm = StringTermImpl("[" + plans.stream()
                .map { s: String ->
                    labelizePlan(
                        s,
                        Supplier { ai.getAndIncrement().toString() + "" })
                }.collect(Collectors.joining(",")) + "]")
        un.unifies(labeledPlans, args[1])
        return true
    }

    companion object {
        fun labelizePlan(planString: String, code: Supplier<String>): String {
            var planString = planString
            val newLabel = Function { s: String -> "@l" + code.get() + s + " " }
            planString = planString.substring(1) // remove "{"
            if (planString.startsWith("@")) {
                val oldLabel = planString.substring(1, planString.indexOf(" "))
                planString = planString.substring(planString.indexOf(" "))
                return if (oldLabel.contains("[")) "{" + newLabel.apply(oldLabel.substring(oldLabel.indexOf("["))) + planString else "{" + newLabel.apply("") + planString
            }
            return "{" + newLabel.apply("") + planString
        }
    }
}