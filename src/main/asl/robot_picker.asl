/***********************************************************************************************************************
 Pre-Processing Directives
 **********************************************************************************************************************/

{ include("action/item_picker.asl") }                               // include plans for items picking
{ include("robot_picker/action/script_executor.asl") }              // include plans for scripts execution
{ include("robot_picker/action/command_executor.asl") }             // include plans for commands execution
{ include("robot_picker/action/move.asl") }                         // include plans for motion
{ include("robot_picker/action/geo-localization.asl") }
{ include("robot_picker/action/job.asl") }

/***********************************************************************************************************************
 Initial beliefs and rules
 **********************************************************************************************************************/

//TODO pick item

/***********************************************************************************************************************
 Initial goals
 **********************************************************************************************************************/

!setup.                                                             // setup

/***********************************************************************************************************************
 Plans
 **********************************************************************************************************************/

///////////////////////////// AGENT SETUP

+!setup : not set
	<-  .df_register("executor(item_picker)", "retrieve(item)");    // register for pick items
		.df_register("executor(command)", "exec(command)");         // register for pick items
		+activity(default);                                         // setup default activity
        +state(available);                                          // set as available to achieve unordinary operations
        +set.                                                       // set process ended

+!kqml_received(Sender, _, Msg, MsgID)
    <-  .println("[ORDER MANAGER] unknown request");
        .send(Sender, failure, error(unknown, Msg), MsgID).         // send failure but not cache response

-!kqml_received(Sender, _, O, MsgID)
    <-  .println("[ORDER MANAGER] failed request");
        .send(Sender, failure, O, MsgID).                           // send failure but not cache response
