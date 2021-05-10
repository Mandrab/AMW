/***********************************************************************************************************************
 Pre-Processing Directives
 **********************************************************************************************************************/

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

+!kqml_received(Sender, _, Msg, MsgID)
    <-  .println("[COLLECTION POINT MANAGER] unknown request");
        .send(Sender, failure, error(unknown, Msg), MsgID).         // send failure but not cache response

-!kqml_received(Sender, _, O, MsgID)
    <-  .println("[COLLECTION POINT MANAGER] failed request");
        .send(Sender, failure, O, MsgID).                           // send failure but not cache response
