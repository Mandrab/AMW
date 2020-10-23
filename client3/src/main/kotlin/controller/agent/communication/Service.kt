package controller.agent.communication

import controller.agent.communication.LiteralBuilder.Companion.pairTerm
import common.type.Command
import jason.asSyntax.*

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
    PICKER_ITEMS("executor(item_picker)")
}

enum class ServiceType(
    val service: String,
    val parse: (Any) -> Literal = { _ -> Literal.parseLiteral(service) }
) {
    ACCEPT_ORDER("accept(order)", parseOrder),
    ADD_COMMAND("add(command)", parseCommand),
    ADD_VERSION("add(version)", parseVersionPair),
    INFO_COMMANDS("info(commands)"),
    INFO_ORDERS("info(orders)", parseOrderInfo),
    INFO_WAREHOUSE("info(warehouse)"),
    EXEC_COMMAND("exec(command)", parseExecCommand),
    EXEC_SCRIPT("exec(command)", parseExecScript),
    REMOVE_ITEM("remove(item)", parseRemoveItem),
    RETRIEVE_ITEMS("retrieve(item)", parseRetrieveItems),
    RETRIEVE_ITEM("retrieve(item)"), // TODO
    STORE_ITEM("store(item)", parseStoreItem);

    val literal: Literal = Literal.parseLiteral(service)
}

private val parseOrder: (Any) -> Literal = { it ->
    check(it is MutableList<*> && it.size == 4 && it[0] is String && it[1] is String && it[2] is String
            && it[3] is Array<*> && (it[3] as Array<*>).all { it is Pair<*,*> })
    val client = StringTermImpl(it.removeAt(0) as String)
    val email = StringTermImpl(it.removeAt(0) as String)
    val address = StringTermImpl(it.removeAt(0) as String)
    check((it.first() is Array<*>))
    val items = (it.first() as Array<*>).map {
        check(it is Pair<*,*>)
        LiteralBuilder("item").setValues(Structure("id").addTerms(StringTermImpl((it.first as String))),
            pairTerm("quantity", (it.second as Int).toDouble())).build() }
    LiteralBuilder("order").setValues(Structure("client").addTerms(client), Structure("email").addTerms(email),
        Structure("address").addTerms(address)).setQueue(*items.toTypedArray()).build()
}

private val parseOrderInfo: (Any) -> Literal = { it -> check(it is Array<*> && it.size >= 2 && it.all { it is String })
    LiteralBuilder("info").setValues(StringTermImpl(it[0] as String), StringTermImpl(it[1] as String)).build()
}

private val parseExecCommand: (Any) -> Literal = { check(it is String)
    LiteralBuilder("execute").setValues(LiteralBuilder("command_id").setValues(it).build()).build()
}

private val parseExecScript: (Any) -> Literal = {
    check(it is Pair<*,*> && it.first is String && it.second is Set<*> && (it.second as Set<*>).all { e -> e is String })
    LiteralBuilder("execute").setValues(LiteralBuilder("script").setValues(StringTermImpl(it.first as String))
        .setQueue(*(it.second as Set<*>).map { r -> StringTermImpl(r as String) }.toTypedArray()).build()).build()
}

private val parseRemoveItem: (Any) -> Literal = {
    check(it is Pair<*,*> && it.first is String && it.second is Array<*>)
    Structure("remove").addTerms(LiteralBuilder("item").setValues(
        LiteralBuilder("id").setValues(StringTermImpl(it.first as String)).build(), Structure("position")
            .addTerms(
                LiteralBuilder("rack").setValues(NumberTermImpl(((it.second as Array<*>)[0] as Int).toDouble()))
                    .build(),
                LiteralBuilder("shelf").setValues(NumberTermImpl(((it.second as Array<*>)[1] as Int).toDouble()))
                    .build(),
                LiteralBuilder("quantity").setValues(NumberTermImpl(((it.second as Array<*>)[2] as Int).toDouble()))
                    .build())).build())
}

private val parseRetrieveItems: (Any) -> Literal = {
    check(it is Pair<*,*> && it.first is String && (it.second as Collection<*>).all { e ->
        e is Pair<*,*> && e.first is String && e.second is Int })
    LiteralBuilder("retrieve").setValues(LiteralBuilder("order_id").setValues(it.first as String).build())
        .setQueue(*(it.second as Collection<Pair<String, Int>>).map { p -> LiteralBuilder("item")
            .setValues(LiteralBuilder("id").setValues(StringTermImpl(p.first)).build(), pairTerm("quantity", p.second.toDouble())).build() }
            .toTypedArray()).build()
}

private val parseCommand: (Any) -> Literal =  { command -> check(command is Command)
    Structure("add").addTerms(LiteralBuilder("command").setValues(
        Structure("id").addTerms(StringTermImpl(command.id)),
        Structure("name").addTerms(StringTermImpl(command.name)),
        Structure("description").addTerms(StringTermImpl(command.description))
    ).setQueue(*command.versions.map { parseVersion(it) }.toTypedArray()).build())
}

private val parseVersionPair: (Any) -> Literal = {
    check(it is Pair<*,*> && it.first is String && it.second is Command.Version)
    Structure("add").addTerms(StringTermImpl(it.first as String), parseVersion(it.second as Command.Version))
}

private val parseVersion: (Any) -> Literal = { version -> check(version is Command.Version)
    LiteralBuilder("variant").setValues(
        Structure("v_id").addTerms(StringTermImpl(version.id)),
        LiteralBuilder("requirements").setQueue(*version.requirements.map { StringTermImpl(it) }.toTypedArray())
            .build(), Structure("script").addTerms(StringTermImpl(version.script))
    ).build()
}

private val parseStoreItem: (Any) -> Literal = {
    check(it is Pair<*,*> && it.first is String && it.second is Array<*>)
    Structure("add").addTerms(LiteralBuilder("item").setValues(
        LiteralBuilder("id").setValues(StringTermImpl(it.first as String)).build(), Structure("position")
            .addTerms(
                LiteralBuilder("rack").setValues(NumberTermImpl(((it.second as Array<*>)[0] as Int).toDouble()))
                    .build(),
                LiteralBuilder("shelf").setValues(NumberTermImpl(((it.second as Array<*>)[1] as Int).toDouble()))
                    .build(),
            LiteralBuilder("quantity").setValues(NumberTermImpl(((it.second as Array<*>)[2] as Int).toDouble()))
                .build())).build())
}