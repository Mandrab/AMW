/***********************************************************************************************************************
 Initial beliefs and rules
***********************************************************************************************************************/

command(id("Command1"), name("command 1 name"), description("descr command 1")) [
		variant(v_id("vid0.0.0.1"), requirements[ "requirement_1", "requirement_3" ], script(
				"[  {@l1 +!main <- .println('Executing script ...');.wait(500); !b}, {@l2 +!b <- .println('Script executed') }]")) ].

/***********************************************************************************************************************
 Initial goals
***********************************************************************************************************************/

!setup.                                                             // setup

/***********************************************************************************************************************
 Plans
***********************************************************************************************************************/

+!setup : not set
	<-  .df_register("management(commands)", "add(command)"); // register for commands adder
		.df_register("management(commands)", "info(commands)");   // register for commands store
		action.load_commands(Commands);
		!add(Commands);
		.include("literal.asl");
		+set.                                              // set process ended

+!kqml_received(Sender, cfp, add(Command), MsgId)
	:   command(id(CID), name(CName), description(CDescr))[ Versions ]
	<-  +Command.

+!kqml_received(Sender, cfp, info(commands), MsgId)                      // send the warehouse state (items info & position)
	<-  .findall(command(id(Id), name(Name), description(Descr))
                [ variant(v_id(VersionID), requirements[ RH | RT ], script(Script)) ],
                command(id(Id), name(Name), description(Descr))
                [ variant(v_id(VersionID), requirements[ RH | RT ], script(Script)) ], L);
        .send(Sender, propose, L, MsgId).

//////////////////////////////////////////////////// UTILITY PLANS /////////////////////////////////////////////////////

///////////////////////////// ADD COMMAND(S)

+!add([ Command | [] ]) <- +Command.

+!add([ Command | Commands ])
    <-  !add([ Command ]);
		!add(Commands).