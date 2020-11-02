/***********************************************************************************************************************
 Pre-Processing Directives
 **********************************************************************************************************************/

{ include("utilities/literal.asl") }                                // utilities for works on literals
{ include("state/warehouse.asl") }                                  // initial state of the warehouse
{ include("action/add.asl") }                                       // plans for items addition
{ include("action/remove.asl") }                                    // plans for items removal
{ include("action/reserve.asl") }                                   // plans for items reservation
{ include("action/info.asl") }                                      // info of the warehouse

/***********************************************************************************************************************
 Initial goals
 **********************************************************************************************************************/

!setup.                                                             // setup

/***********************************************************************************************************************
 Plans
 **********************************************************************************************************************/

+!setup : not set
	<-  .df_register("management(items)", "info(warehouse)");       // register as warehouse's info dispatcher
		.df_register("management(items)", "store(item)");           // register for store new items in the warehouse
		.df_register("management(items)", "remove(item)");          // register for remove items from the warehouse
		.df_register("management(items)", "retrieve(item)");        // register for reserve items of the warehouse
		+set.                                                       // set setup-process ended

// unknown messages management
+!kqml_received(Sender, _, Msg, MsgID) <- .send(Sender, failure, error(unknown, Msg), MsgID).