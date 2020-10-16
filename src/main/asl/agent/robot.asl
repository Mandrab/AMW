/***********************************************************************************************************************
 Pre-Processing Directives
 **********************************************************************************************************************/

{ include("module/robot/item_picker.asl") }                          // include plans for items picking
{ include("module/robot/script_executor.asl") }                      // include plans for scripts execution
{ include("module/robot/command_executor.asl") }                     // include plans for commands execution
{ include("module/robot/move.asl") }                                // include plans for motion
{ include("module/robot/geo-localization.asl") }

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
	<-  !move_by(5, 6);
	    .println("Doing stuffs ...");
		.wait(1000);                                                // fake execution time
		!work.                                                      // restart to work

///////////////////////////// PICKING

+!work : activity(picking)[client(Client),item(id(ID),item(Item))]  // only if picking
	<-  Item = item(id(IID))[H|T];                                  // TODO docu che in teoria potrebbe non esserci l'oggetto e nel caso dovrebbe cercare da qualche altra parte
	    .println("Picking ...");
	    !!remove(item(IID),_);                                      // TODO la posizione non è spacificata perchè non è implementata la ricerca di cui sopra
	    .wait(1000);                                                // fake execution time
		.send(Client,complete,retrieve(Item));                      // confirm task completion
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