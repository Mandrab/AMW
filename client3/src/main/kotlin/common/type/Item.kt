package common.type

import java.util.*
import controller.agent.communication.LiteralParser.getValue
import controller.agent.communication.LiteralParser.split
import controller.agent.communication.LiteralParser.splitStructAndList

/**
 * This class represent an item in a specific position in the warehouse
 *
 * @author Paolo Baldini
 */
class Item(val itemId: String, val reserved: Int, var positions: List<Triple<Int, Int, Int>>): Cloneable {

    public override fun clone() = Item(itemId, reserved, positions.map { Triple(it.first, it.second, it.third) })

    override fun equals(other: Any?): Boolean = other is Item && itemId == other.itemId && reserved == other.reserved
            && positions.size == other.positions.size && positions.all { other.positions.contains(it) }

    override fun toString(): String = "ID: $itemId, Reserved: $reserved " +
		    positions.joinToString { "rack: ${it.first}, shelf: ${it.second}, quantity: ${it.third}" } + "\n"

    override fun hashCode(): Int {
        var result = itemId.hashCode()
        result = 31 * result + positions.hashCode()
        return result
    }

	/**
	 * Parse a string to extract an Item object
	 */
    companion object {
        fun parse(input: String): Item {
            val itemId = getValue(input, "id")!!
            val reserved: Int = Objects.requireNonNull(getValue(input, "reserved"))!!.toInt()
            return Item(itemId, reserved, split(splitStructAndList(input).second).map {
	            Triple(
		            Objects.requireNonNull(getValue(it, "rack"))!!.toInt(),
		            Objects.requireNonNull(getValue(it, "shelf"))!!.toInt(),
		            Objects.requireNonNull(getValue(it, "quantity"))!!.toInt()
	            )
	            }
            )
        }
    }
}