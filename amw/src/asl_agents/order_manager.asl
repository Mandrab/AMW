/***********************************************************************************************************************
 Initial beliefs and rules
***********************************************************************************************************************/

set( false ).                                                       // at start is not yet set

/***********************************************************************************************************************
 Initial goals
***********************************************************************************************************************/

!setup.                                                             // start setup

/***********************************************************************************************************************
 Plans
***********************************************************************************************************************/

+!setup                                                             // setup the agent
	:   set( false )
	<-  .df_register( "management( orders )", "accept( order )" );  // register service as order acceptor
		.include( "utils/string_utils.asl" );                       // include string utils plans
		.include( "utils/literal_utils.asl" );                      // include string utils plans
		-+set( true ).                                              // set process ended

// OPERATION #1 in purchase sequence schema: order reception
+!kqml_received( Sender, achieve, Content, MsgId )                  // receive an order                                 // KQML achieve = ACL request: http://jason.sourceforge.net/doc/faq.html TODO remove
	:   Content = order( client( Client ), email( Email ), address( Address ) )[ [] | Items ]
	<-  .println( "Op 1" );
		!order_id( Content, OrderId );                              // generate an id for the order
		+order( id( OrderId ), status( checking ), client( Client ), email( Email ), address( Address ),
				items( Items ) );                                   // save order's info (status=checking for validity)
		.df_search( "management( items )", "retrieve( item )",
				Providers );                                        // search the agent(s) that manages the warehouse
		.nth( 0, Providers, Provider );                             // get the first ( agent )
		!concat( retrieve( order_id( OrderId ) ), Items, Res );
        .send( Provider, achieve, Res ).                            // ask for items reservation and positions

// OPERATION #6/11 in purchase sequence schema: absence or conflict for items reservation
+!kqml_received( Sender, failure, Content, MsgId )                  // manage error from items retrieve
	:   Content = error( order_id( OrderId ), error_code( ErrorCode ) )
	<-	.println( "Op 6/11" );
		-order( id( OrderId ), status( checking ), client( _ ), email( Email ), address( _ ),
                items( _ ) );                                       // retrieve order information and remove the value
		asl_actions.send_feedback( Email, ErrorCode ).              // send failure mail

// OPERATION #14 in purchase sequence schema: confirm of items reservation and reception of positions
+!kqml_received( Sender, confirm, Content, MsgId )                  // receive items position and reservation confirm
	:   Content = confirmation( order_id( OrderId ) )[ [] | Positions ]
	&   order( id( OrderId ), status( checking ), client( Client ), email( Email ), address( Address ), items( Items ) )
	<-  .println( "Op 14" );
		.println( Items );
		.println( Positions );
		asl_actions.fuse( Items, Positions, Fused );
		asl_actions.send_feedback( Email, 200, OrderId, Items );
		!retrieve( Fused ).                                         // retrieve all the items

/***********************************************************************************************************************
 Utils
 **********************************************************************************************************************/

+!order_id( Order, ResultId )                                                   // generate an ID for the order
	:   Order = order( client( Client ), email( Email ), address( Address ) )[ [] | Items ]
	<-  .date( Y, M, D );
		.time( H, Min, S );
		!str_concat( Client, [ Email, Address, Y, M, D, H, Min, S ], ResultId ).// concat order infos

+!retrieve( [ Head | [] ] )
	:   Head = item( id( ItemId ), quantity( Quantity ) )[ [] | Positions ]
	<-  .df_search( "executor( item_picker )", "retrieve( item )",
                Providers );                                                    // search the robot agent(s)
        .nth( 0, Providers, Provider );                                         // get the first ( agent )
        .send( Provider, cfp, retrieve( item( ItemId ) )[ Positions ] ).        // ask item retrieve

+!retrieve( [ Head | Tail ] )
	<-  !retrieve( [ Head ] );
		!retrieve( Tail ).