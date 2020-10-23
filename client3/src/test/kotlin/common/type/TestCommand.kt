package common.type

import org.junit.Test
import common.type.Command.Version
import org.junit.Assert.*

/**
 * Test command class
 *
 * @author Paolo Baldini
 */
class TestCommand {

	@Test fun testCloneCommand() {
		var command = Command("id", "command name", "description", emptyList())
		assertFalse(command === command.clone())
		assertEquals(command, command.clone())
		assertEquals(command.hashCode(), command.clone().hashCode())

		assertNotEquals(Command("", "", "", emptyList()), command)
		assertNotEquals(Command("", "", "", emptyList()).hashCode(), command.hashCode())

		val versions = listOf(Version("id", listOf("req1", "req2"), "script"))
		command = Command("id", "command name", "description", versions)
		assertFalse(command === command.clone())
		assertEquals(command, command.clone())
		assertEquals(command.hashCode(), command.clone().hashCode())

		assertEquals(Command("id", "command name", "description", emptyList()), command)
		assertEquals(Command("id", "command name", "description", emptyList()).hashCode(), command.hashCode())
	}

	@Test fun testCloneVersion() {
		val version = Version("id", listOf("req1", "req2"), "script")
		assertEquals(version, version.clone())
		assertEquals(version.hashCode(), version.clone().hashCode())

		assertEquals(Version("id", listOf("req1"), "script"), version)
		assertEquals(Version("id", listOf("req1"), "script").hashCode(), version.hashCode())
	}

	@Test fun testCloningSublist() {
		val version = Version("id", listOf("req1", "req2"), "script")
		val command = Command("id", "command name", "description", listOf(version))

		assertFalse(command.versions === command.clone().versions)
		assertEquals(command.versions, command.clone().versions)
		assertEquals(command.versions.hashCode(), command.clone().versions.hashCode())

		assertFalse(version.requirements === version.clone().requirements)
		assertEquals(version.requirements, version.clone().requirements)
		assertEquals(version.requirements.hashCode(), version.clone().requirements.hashCode())
	}

	@Test fun testParseCommand() {
		var commandStr = "command(id(\"Command1\"), name(\"command 1 name\"), description(\"descr command 1\"))[]"
		var command = Command("\"Command1\"", "\"command 1 name\"", "\"descr command 1\"", emptyList())
		assertEquals(command, Command.parse(commandStr))

		val versionStr = "variant(v_id(\"VersionID\"), requirements[\"req1\",\"req2\"], script(\"Script\"))"
		commandStr = "command(id(\"Command1\"), name(\"command 1 name\"), description(\"descr command 1\"))" +
				"[$versionStr, $versionStr]"
		command = Command("\"Command1\"", "\"command 1 name\"", "\"descr command 1\"",
				listOf(Version.parse(versionStr), Version.parse(versionStr)))
		assertEquals(command, Command.parse(commandStr))
	}

	@Test fun testParseVersion() {
		val versionStr = "variant(v_id(\"VersionID\"), requirements[\"req1\",\"req2\"], script(\"Script\"))"
		val version = Version("\"VersionID\"", listOf("\"req1\"", "\"req2\""), "\"Script\"")
		assertEquals(version, Version.parse(versionStr))
	}
}