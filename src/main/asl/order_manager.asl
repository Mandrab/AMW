/***********************************************************************************************************************
 Pre-Processing Directives
 **********************************************************************************************************************/

{ include("utility/cache.asl") }
{ include("utility/communication.asl") }
{ include("utility/literal.asl") }
{ include("order_manager/action/id_generator.asl") }
{ include("order_manager/action/info.asl") }
{ include("order_manager/action/request.asl") }
{ include("order_manager/action/retrieve.asl") }

/***********************************************************************************************************************
 Initial goals
***********************************************************************************************************************/

!setup.                                                             // start setup

/***********************************************************************************************************************
 Plans
***********************************************************************************************************************/

+!setup : not set
    <-  .df_register("management(orders)", "accept(order)");        // register service as order acceptor
        .df_register("management(orders)", "info(orders)");         // register service as order informant
        +set.                                                       // set setup-process ended

+!kqml_received(_, failure, _, _).                                  // failure are already managed automatically

+!kqml_received(Sender, _, Msg, MsgID)
    <-  .println("[ORDER MANAGER] unknown request");
        .send(Sender, failure, error(unknown, Msg), MsgID).         // send failure but not cache response

-!kqml_received(Sender, _, Msg, MsgID)
    <-  .println("[ORDER MANAGER] failed request");
        .send(Sender, failure, Msg, MsgID).                         // send failure but not cache response
