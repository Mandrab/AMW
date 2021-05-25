package controller.agent.communication.translation.`in`

import kotlin.streams.asSequence

/**
 * An utility object created for extract values from literals
 *
 * @author Paolo Baldini
 */
object LiteralParser {

    fun String.value() = getValue(this)

    fun String.value(pattern: String) = getValue(this, pattern)

    fun String.struct() = splitStructAndList(this).first

    fun String.queue() = splitStructAndList(this).second

    fun String.asList() = this@LiteralParser.split(this)

    /**
     * Get value contained between parenthesis of element. E.g.:
     *      structure = valueOf(RETURN-VALUE)
     */
    fun getValue(structure: String, valueOf: String): String? {
        val parts = splitStructAndList(structure)

        // if no element contains string then immediately return null
        val structures = parts.first.asList().filter { it.contains(valueOf) }

        // search if an element start with value and eventually return the value
        structures.find { it.startsWith(valueOf) }?.let { return it.value() }

        // get value of each element and check if its the 'container'
        structures.mapNotNull { getValue(it.value(), valueOf) }.firstOrNull()?.let { return it }

        val lists = parts.second.asList().filter { it.contains(valueOf) }

        // search if an element start with value and eventually return the value
        lists.find { it.startsWith(valueOf) }?.let { return it.value() }

        // get value of each element and check if its the 'container'
        return lists.mapNotNull { getValue(getValue(it), valueOf) }.firstOrNull()
    }

    /**
     * Get value contained between parenthesis of element. E.g.:
     *      structure = element(RETURN-VALUE)
     */
    fun getValue(structure: String): String {
        check(structure.contains("(") && structure.contains(")"))
                { "structure $structure has no parenthesis value" }
        check(!structure.startsWith("["))
                { "is possible to retrieve only the value of a structure or a literal" }

        if (structure.endsWith("]")) return structure.struct().value()
        return structure.slice(0..structure.length -2).substringAfter("(")
    }

    /**
     * Split element and queue. E.g.:
     *      literal = elem(values)[queues]
     *      RETURN = Pair(elem(values), queues)
     */
    fun splitStructAndList(literal: String): Pair<String, String> {
        // a simple list
        if (literal.startsWith("[")) return Pair(String(), literal.removeSurrounding("[", "]"))

        var openedParenthesis = 0
        val startingIndex = literal.chars().mapToObj { i: Int -> i.toChar() }.asSequence()
                .takeWhile { it != '[' || openedParenthesis > 0 }
                .onEach { when (it) {
                    '(' -> openedParenthesis++
                    ')' -> openedParenthesis--
                    else -> Unit
                } }.count().let { if (it == literal.length) null else it }

        return startingIndex ?. let {
            Pair(literal.substring(0, startingIndex), literal.substring(startingIndex).removeSurrounding("[", "]"))
        } ?: Pair(literal, String())
    }

    /**
     * Split a list of element enclosed between square parenthesis. E.g.:
     *      literal = [queue1, queue2, queue3]
     *      RETURN = List(queue1, queue2, queue3)
     */
    fun split(list: String): List<String> {
        list.removeSurrounding("[", "]").let { elements ->
            var openedParenthesis = 0
            var counter = 0

            return elements.chars().mapToObj { it.toChar() }.asSequence()
                    .onEach {
                        when (it) {
                            '(', '[' -> openedParenthesis++
                            ')', ']' -> openedParenthesis--
                            else -> Unit
                        }
                    }.groupBy { if (it == ',' && openedParenthesis == 0) counter++ else counter }
                    .map { it.value.joinToString("").removeSuffix(",") }
        }
    }
}