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

+!kqml_received(Sender, _, Msg, MsgID)
    <-  .println("[ROBOT PICKER] unknown request");
        .send(Sender, failure, error(unknown, Msg), MsgID).         // send failure but not cache response

-!kqml_received(Sender, _, O, MsgID)
    <-  .println("[ROBOT PICKER] failed request");
        .send(Sender, failure, O, MsgID).                           // send failure but not cache response
