/***********************************************************************************************************************
 Pre-Processing Directives
 **********************************************************************************************************************/

{ include("utility/cache.asl") }
{ include("warehouse_mapper/state/warehouse.asl") }                 // initial state of the warehouse
{ include("warehouse_mapper/action/add.asl") }                      // plans for items addition
{ include("warehouse_mapper/action/info.asl") }                     // info of the warehouse
{ include("warehouse_mapper/action/remove.asl") }                   // plans for items removal

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
		+set.                                                       // set setup-process ended

+!kqml_received(_, failure, _, _).                                  // failure are already managed automatically

+!kqml_received(Sender, _, M, _)
    <-  .println("[WAREHOUSE MAPPER] unknown request");
        .send(Sender, failure, unknown(M)).                         // send failure but not cache response

-!kqml_received(Sender, _, M, _)
    <-  .println("[WAREHOUSE MAPPER] failed request");
        .send(Sender, failure, error(M)).                           // send failure but not cache response
