/***********************************************************************************************************************
 Pre-Processing Directives
 **********************************************************************************************************************/

{ include("utility/literal.asl") }                                // include utilities for works on literals
{ include("state/commands.asl") }

/***********************************************************************************************************************
 Initial goals
 **********************************************************************************************************************/

!setup.                                                             // setup

/***********************************************************************************************************************
 Plans
 **********************************************************************************************************************/

+!setup : not set
	<-  .df_register("management(commands)", "add(command)"); // register for commands adder
	    .df_register("management(commands)", "request(command)"); // register for commands dispatcher
		.df_register("management(commands)", "info(commands)");   // register for commands store
		action.load_commands(Commands);
		!add(Commands);
		+set.                                              // set process ended

@commandAddition[atomic]
+!kqml_received(Sender, achieve, add(Command), MsgID)
	:   Command = command(id(CID), name(CName), description(CDescr))[ [] | Versions ]
	<-  !add([ Command ]);
	    // TODO fail labelling della versione
	    .println("Request for command addition...");
	    .send(Sender, confirm, add(Command), MsgID).

@versionAddition[atomic]
+!kqml_received(Sender, achieve, add(CommandID, Version), MsgID)
    :   Version = variant(v_id(ID), requirements[ [] | Requirements ], script(Script))
    &   command(id(CommandID), N, D) [ source(self) | Versions ]
    <-  ? not Versions = [ variant(v_id(ID), _, _) | _ ];
        .println("Request for version addition...");
        -command(id(CommandID), N, D);
        +command(id(CommandID), name(N), description(D)) [ Version | Versions ];
        .send(Sender, confirm, add(Command), MsgID).

-!kqml_received(Sender, achieve, Content, MsgID)
    <-  .println("Invalid request...");
        .send(Sender, refuse, Content, MsgID).

@commandRequest[atomic]
+!kqml_received(Sender, achieve, Content, MsgID)                  // send the warehouse state (items info & position)
	:   Content = request(command_id(CommandID))
	&   command(id(CommandID), _, _)[ source(self) | Variants ]
    <-  .println("Request for command...");
        !concat(command(CommandID), Variants, Msg);
        .term2string(MsgT, Msg);
        .send(Sender, tell, Msg, MsgID).                          // ask if able to run the specified script

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