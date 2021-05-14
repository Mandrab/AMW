/***********************************************************************************************************************
 Pre-Processing Directives
 **********************************************************************************************************************/

{ include("utility/cache.asl") }
{ include("collection_point_manager/state/collection_points.asl") }
{ include("collection_point_manager/action/request.asl") }

/***********************************************************************************************************************
 Initial goals
 **********************************************************************************************************************/

!setup.                                                             // setup

/***********************************************************************************************************************
 Plans
 **********************************************************************************************************************/

+!setup : not set
    <-  .df_register("management(items)", "info(collection_points)");   // register service as collection points manager
        +set.

+!kqml_received(_, failure, _, _).                                  // failure are already managed automatically

+!kqml_received(Sender, _, M, _)
    <-  .println("[COLLECTION POINT MANAGER] unknown request");
        .send(Sender, failure, unknown(M)).                         // send failure but not cache response

-!kqml_received(Sender, _, M, _)
    <-  .println("[COLLECTION POINT MANAGER] failed request");
        .send(Sender, failure, error(M)).                           // send failure but not cache response
