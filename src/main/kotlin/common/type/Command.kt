package common.type

import common.translation.LiteralParser.getValue
import common.translation.LiteralParser.split
import common.translation.LiteralParser.splitStructAndList

/**
 * Data class to store commands info. A command is an operation executable by an agent
 *
 * @author Paolo Baldini
 */
class Command(val id: String, val name: String, val description: String, val versions: List<Version>): Cloneable {

    public override fun clone() = Command(id, name, description, versions.map { it.clone() }.toList())

    override fun equals(other: Any?): Boolean = other is Command && id == other.id && name == other.name
            && description == other.description && versions == other.versions

    override fun toString() = "ID: $id, name: $name, Description: $description, Versions:" + versions
            .joinToString { it.toString() }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + versions.map { it.hashCode() }.sum()
        return result
    }

    class Version(val id: String, val requirements: List<String>, val script: String): Cloneable {

        public override fun clone() = Version(id, requirements.map { s -> s }.toList(), script)

        override fun equals(other: Any?): Boolean = other is Version && id == other.id
                && requirements == other.requirements && script == other.script

        override fun hashCode(): Int {
            var result = 31 * id.hashCode()
            result = 31 * result + requirements.map { it.hashCode() }.sum()
            result = 31 * result + script.hashCode()
            return result
        }

        override fun toString() = "ID: $id; Requirements: $requirements; Script: $script"

        /**
         * Parse a string to extract a Version object
         */
        companion object {
            fun parse(input: String) = Version(getValue(input, "v_id")!!,
                    split(splitStructAndList(split(getValue(input)).first { it.startsWith("requirements") }).second),
	                getValue(input, "script")!!)
        }
    }

    /**
     * Parse a string to extract a Command object
     */
    companion object {
        fun parse(input: String): Command {
            val pair = splitStructAndList(input)
            val id = getValue(pair.first, "id")!!
            val name = getValue(pair.first, "name")!!
            val description = getValue(pair.first, "description")!!
            val versions = split(pair.second).filter { it.isNotBlank() }.map { Version.parse(it) }
            return Command(id, name, description, versions)
        }
    }
}