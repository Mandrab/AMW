/***********************************************************************************************************************
 Pre-Processing Directives
 **********************************************************************************************************************/

{ include("module/robot/ItemPicker.asl") }                          // include plans for items picking
{ include("module/robot/ScriptExecutor.asl") }                      // include plans for scripts execution
{ include("module/robot/CommandExecutor.asl") }                     // include plans for commands execution

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
        +set;                                                       // set process ended
		!work.                                                      // start working

///////////////////////////////////////////////////////// JOBS /////////////////////////////////////////////////////////

///////////////////////////// DEFAULT

+!work : activity(default)                                          // only if achieving default activity
	<-  .println("Doing stuffs ...");
		.wait(1000);                                                // fake execution time
		!work.                                                      // restart to work

///////////////////////////// PICKING

+!work : activity(picking)[ client(Client), item(Item) ]            // only if picking
	<-  .println("Picking ...");
    .wait(1000);                                                    // fake execution time
		.send(Client, complete, retrieve(Item));                    // confirm task completion
        -+activity(default);                                        // setup default activity
        -+state(available);                                         // set as available to achieve unordinary operations
        !work.                                                      // restart to work

///////////////////////////// SCRIPT EXECUTION

+!work : activity(executing)[ client(Client), script(Script) ]      // only if executing script
	<-  !main[source(script)];                                      // run the main intention of the script
		!remove_plans(0);                                           // remove all plans with label in the form of "lN"
		-+activity(default);                                        // at end, setup default activity
        -+state(available);                                         // set as available to achieve unordinary operations
        !work.                                                      // restart to work

///////////////////////////// IDLE

+!work : activity(_).