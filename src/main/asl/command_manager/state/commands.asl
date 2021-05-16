/***********************************************************************************************************************
 Initial beliefs and rules
 **********************************************************************************************************************/

command(id("Command1"), name("command 1 name"), description("description command 1")) [
		script(
		    "[{@l0 +!main <- .println('Executing script ...');.wait(500);!b}, {@l1 +!b <- .println('Script executed')}]"
        )
].
