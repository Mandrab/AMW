/***********************************************************************************************************************
 Pre-Processing Directives
 **********************************************************************************************************************/

{ include("utility/literal.asl") }                                  // include utilities for works on literals
{ include("state/commands.asl") }

/***********************************************************************************************************************
 Initial goals
 **********************************************************************************************************************/

!setup.                                                             // setup

/***********************************************************************************************************************
 Plans
 **********************************************************************************************************************/

+!setup : not set
	<-  .df_register("management(commands)", "add(command)");       // register for commands adder
	    .df_register("management(commands)", "request(command)");   // register for commands dispatcher
		.df_register("management(commands)", "info(commands)");     // register for commands store
		+set.                                                       // set process ended

+!kqml_received(Sender, _, M, _)
    <-  .println("[COMMAND MANAGER] unknown request");
        .send(Sender, failure, unknown(M)).                         // send failure but not cache response

-!kqml_received(Sender, _, M, _)
    <-  .println("[COMMAND MANAGER] failed request");
        .send(Sender, failure, error(M)).                           // send failure but not cache response
