/***********************************************************************************************************************
 Pre-Processing Directives
 **********************************************************************************************************************/

{ include("utility/cache.asl") }
{ include("utility/communication.asl") }
{ include("utility/literal.asl") }
{ include("order_manager/action/id_generator.asl") }
{ include("order_manager/action/info.asl") }
{ include("order_manager/action/request.asl") }

/***********************************************************************************************************************
 Initial goals
***********************************************************************************************************************/

!setup.                                                             // start setup

/***********************************************************************************************************************
 Plans
***********************************************************************************************************************/

@setup[atomic]
+!setup : not set
    <-  .df_register("management(orders)", "accept(order)");
        .df_register("management(orders)", "info(orders)");
        +set.                                                       // register service as order acceptor