/***********************************************************************************************************************
 Initial beliefs and rules
 **********************************************************************************************************************/

command(id("Command1"), name("command 1 name"), description("descr command 1")) [
		variant(v_id("vid0.0.0.1"), requirements[ "move" ], script(
				"[  {@l1 +!main <- .println('Executing script ...');.wait(500); !b}, {@l2 +!b <- .println('Script executed') }]")) ].