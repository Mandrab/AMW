package controller.agent.communication

import java.util.*

/**
 * An utility object created for extract values from literals
 *
 * @author Paolo Baldini
 */
object LiteralParser {

    /**
     * Get value contained between parenthesis of element. E.g.:
     *      structure = valueOf(RETURN-VALUE)
     */
    fun getValue(structure: String, valueOf: String): String? {
        val parts = splitStructAndList(structure)

        // if no element contains string then immediately return null
        val structures = split(parts.first).filter { it.contains(valueOf) }

        // search if an element start with value and eventually return the value
        structures.find { it.startsWith(valueOf) }?.let { return getValue(it) }

        // get value of each element and check if its the 'container'
        structures.mapNotNull { getValue(getValue(it), valueOf) }.firstOrNull()?.let { return it }

        val lists = split(parts.second).filter { it.contains(valueOf) }

        // search if an element start with value and eventually return the value
        lists.find { it.startsWith(valueOf) }?.let { return getValue(it) }

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

        if (structure.endsWith("]"))
            return getValue(splitStructAndList(structure).first)
        return structure.slice(0..structure.length -2).substringAfter("(")
    }

    /**
     * Split element and queue. E.g.:
     *      literal = elem(values)[queues]
     *      RETURN = Pair(elem(values), queues)
     */
    fun splitStructAndList(literal: String): Pair<String, String> {
        if (literal.startsWith("["))                              // a simple list
            return if (literal.endsWith("]")) Pair("", literal)
            else throw IllegalStateException() // wrong formatted

        if (!literal.endsWith("]")) return Pair(literal, "")

        val chars = literal.chars().mapToObj { i: Int -> i.toChar() }.toArray()
        var startingIndex = 0
        var openedParenthesis = 0
        for (i in chars.indices) {
            if (chars[i] == '[' && openedParenthesis++ == 0) startingIndex = i
            else if (chars[i] == ']') openedParenthesis--
        }

        return Pair(literal.substring(0, startingIndex), literal.substring(startingIndex + 1, literal.length - 1))
    }

    /**
     * Split a list of element enclosed between square parenthesis. E.g.:
     *      literal = [queue1, queue2, queue3]
     *      RETURN = List(queue1, queue2, queue3)
     */
    fun split(_list: String): List<String> {
        val list = _list.removeSurrounding("[", "]")

        if (list.isBlank() || !list.contains(",")) return listOf(list)

        val chars = list.chars().mapToObj { it.toChar() }.toArray()
        val output: MutableList<String> = LinkedList()

        var startingIdx = 0
        var lastLiteralIdx = 0
        var openedParenthesis = 0

        chars.indices.onEach {
            if (chars[it] == '(' || chars[it] == '[') openedParenthesis++
            if (chars[it] == ')' || chars[it] == ']') openedParenthesis--

            check (openedParenthesis >= 0) { "Parenthesis are not balanced in $_list" }

            if (chars[it] == ',' && openedParenthesis == 0) {
                output.add(chars.copyOfRange(startingIdx, it).joinToString(""))
                lastLiteralIdx = it
                startingIdx = it + 1
            }
        }

        if (output.isNotEmpty() && list.contains(",")) output.add(list.substring(lastLiteralIdx + 1))
        else output.add(list)
        for (i in output.indices) {
            val s = output[i]
            if (s.startsWith(" ") || s.endsWith(" ")) {
                output.removeAt(i)
                if (s.startsWith(" ")) output.add(i, s.replaceFirst(" ", ""))
                if (s.endsWith(" ")) output.add(i, s.substring(0..s.length -2))
            }
        }
        return output
    }
}