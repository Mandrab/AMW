/***********************************************************************************************************************
 Pre-Processing Directives
 **********************************************************************************************************************/

{ include("literal.asl") }                                          // utilities for works on literals
{ include("state/warehouse.asl") }                                  // initial state of the warehouse
{ include("module/warehouse_mapper/add.asl") }                      // plans for items addition
{ include("module/warehouse_mapper/remove.asl") }                   // plans for items removal
{ include("module/warehouse_mapper/reserve.asl") }                  // plans for items reservation
{ include("module/warehouse_mapper/info.asl") }                     // info of the warehouse

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