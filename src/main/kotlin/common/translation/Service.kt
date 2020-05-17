package common.translation

import common.type.Command
import jason.asSyntax.Literal
import jason.asSyntax.StringTermImpl

/**
 * Possible services type in the system
 *
 * @author Paolo Baldini
 */
enum class Service(val service: String) {
    MANAGEMENT_COMMANDS("management(commands)"),
    MANAGEMENT_ITEMS("management(items)"),
    MANAGEMENT_ORDERS("management(orders)"),
    EXECUTOR_COMMAND("executor(command)"),
    EXECUTOR_SCRIPT("executor(command)"),
}

enum class ServiceType(
    val service: String,
    val parse: (Any) -> Literal = { _ -> Literal.parseLiteral(service) }
) {
    ACCEPT_ORDER("accept(order)"),
    ADD_COMMANDS("add(command)", parseCommand),
    INFO_WAREHOUSE("info(warehouse)"),
    INFO_COMMANDS("info(commands)"),
    EXEC_COMMAND("exec(command)", parseExecCommand),
    EXEC_SCRIPT("exec(command)", parseExecScript);

    val literal: Literal = Literal.parseLiteral(service)
}

private val parseExecCommand: (Any) -> Literal = { check(it is String)
    LiteralBuilder("execute").setValues(LiteralBuilder("command_id").setValues(it).build()).build()
}

private val parseExecScript: (Any) -> Literal = {
    check(it is Pair<*,*> && it.first is String && it.second is Set<*> && (it.second as Set<*>).all { e -> e is String })
    LiteralBuilder("execute").setValues(LiteralBuilder("script").setValues(StringTermImpl(it.first as String))
        .setQueue(*(it.second as Set<String>).map { r -> StringTermImpl(r) }.toTypedArray()).build()).build()
}

private val parseCommand: (Any) -> Literal =  { command -> check(command is Command)
    LiteralBuilder("command").setValues(
        LiteralBuilder.pairTerm("id", command.id),
        LiteralBuilder.pairTerm("name", command.name),
        LiteralBuilder.pairTerm("description", command.description)
    ).setQueue(*command.versions.map { parseVersion(it) }.toTypedArray()).build()
}

private fun parseVersion(version: Command.Version): Literal {
    return LiteralBuilder("variant").setValues(
        LiteralBuilder.pairTerm("v_id", version.id),
        LiteralBuilder("requirements").setQueue(*version.requirements.toTypedArray()).build(),
        LiteralBuilder.pairTerm("script", version.script)
    ).build()
}