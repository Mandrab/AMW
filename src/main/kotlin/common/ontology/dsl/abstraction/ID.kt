package common.ontology.dsl.abstraction

object ID {

    data class ID(val name: String, val syntax: String)

    fun id(id: String) = ID(id, "id")

    fun v_id(id: String) = ID(id, "v_id")

    fun order_id(id: String) = ID(id, "order_id")

    fun command_id(id: String) = ID(id, "command_id")
}