/***********************************************************************************************************************
 Pre-Processing Directives
 **********************************************************************************************************************/

{ include("utility/cache.asl") }
{ include("utility/communication.asl") }
{ include("robot_picker/action/pick_request.asl") }
{ include("robot_picker/action/script_request.asl") }

/***********************************************************************************************************************
 Initial goals
 **********************************************************************************************************************/

!setup.                                                             // setup

/***********************************************************************************************************************
 Plans
 **********************************************************************************************************************/

+!setup : not set
	<-  .df_register("executor(item_picker)", "retrieve(item)");    // register for pick items
		.df_register("executor(command)", "exec(command)");         // register for pick items
        +set.                                                       // set process ended

+!kqml_received(_, failure, _, _).                                  // failure are already managed automatically

+!kqml_received(Sender, _, M, _)
    <-  .println("[ROBOT PICKER] unknown request");
        .send(Sender, failure, unknown(M)).                         // send failure but not cache response

-!kqml_received(Sender, _, M, _)
    <-  .println("[ROBOT PICKER] failed request");
        .send(Sender, failure, error(M)).                           // send failure but not cache response
