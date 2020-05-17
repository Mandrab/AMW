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

@commandAddition[atomic]
+!kqml_received(Sender, achieve, add(Command), MsgID)
	:   Command = command(id(CID), name(CName), description(CDescr))[ [] | Versions ]
	<-  !add([ Command ]);
	    .send(Sender, confirm, add(Command), MsgID).

@versionAddition[atomic]
+!kqml_received(Sender, achieve, add(CommandID, Version), MsgID)
	:   Version = variant(v_id(ID), requirements[ Requirements ], script(Script))
	&   command(id(CommandID), name(N), description(D)) [ Versions ]
	<-  -command(id(CommandID), name(N), description(D));
	    +command(id(CommandID), name(N), description(D)) [ Version | Versions ].

-!kqml_received(Sender, achieve, Content, MsgID) <- .send(Sender, refuse, Content, MsgID).

@informationRequest[atomic]
+!kqml_received(Sender, cfp, info(commands), MsgID)                      // send the warehouse state (items info & position)
	<-  .findall(command(id(Id), name(Name), description(Descr))
                [ variant(v_id(VersionID), requirements[ RH | RT ], script(Script)) ],
                command(id(Id), name(Name), description(Descr))
                [ variant(v_id(VersionID), requirements[ RH | RT ], script(Script)) ], L);
        .send(Sender, propose, L, MsgID).

//////////////////////////////////////////////////// UTILITY PLANS /////////////////////////////////////////////////////

///////////////////////////// ADD COMMAND(S)

+!add(Command) : Command = command(id(CID), _, _) [_|_]
    &   not command(id(CID), _, _)
    <-  +Command.

+!add([ Command | [] ]) <- !add(Command).

+!add([ Command | Commands ])
    <-  !add([ Command ]);
		!add(Commands).