/***********************************************************************************************************************
 Initial beliefs and rules
 **********************************************************************************************************************/

set( false ).                                                       // at start is not yet set

/***********************************************************************************************************************
 Initial goals
 **********************************************************************************************************************/

!setup.                                                             // setup

/***********************************************************************************************************************
 Plans
 **********************************************************************************************************************/

+!setup
	:   set( false )
	<-  .df_register( "management( orders )", "accept( order )" );  // register service as order acceptor
		.include( "utils.asl" );                                    // include utils
		-+set( true ).                                              // set process ended

// OPERATION #1 in purchase sequence schema
+!kqml_received( Sender, cfp, Content, MsgId )                      // try to accept an order
	:   Content = order( client( Client ), address( Address ) )[ [] | Items ]
	<-  !order_id( Content, OrderId );                              // generate an id for the order
		+order( id( OrderId ), client( Sender ),
				status( checking ), info( Content ) ); // save the order info
		.df_search( "management( items )", "retrieve( item )",
				Providers );                                        // search for agents able to manage items retrieve
		.nth( 0, Providers, Provider );                             // get the first agent
		!concat( retrieve( order_id( OrderId ) ), Items, Res );
        .send( Provider, cfp, Res ).                                // ask if there're all items of the order

// OPERATION #6/11 in purchase sequence schema
+!kqml_received( Sender, propose, Content, MsgId )
	:   Content = error( order_id( OrderId ), error_code( ErrorCode ) )
	<-	-order( id( OrderId ), client( Client ),
				status( checking ), info( OrderInfo ) );            // retrieve order information and remove the value
		.send( Client, failure, error( order_id( OrderId ),
				error_code( ErrorCode ) ) ).                        // send the error message

// OPERATION #14 in purchase sequence schema
+!kqml_received( Sender, propose, Content, MsgId )
	:   Content = confirmation( order_id( OrderId ) )[ [] | Positions ]
	<-  -order( id( OrderId ), client( Client ),
        		status( checking ), info( OrderInfo ) );            // remove old order status
		+order( id( OrderId ), client( Client ),
				status( pending ), info( OrderInfo ) );             // update order status
		.send( Client, request, confirmation( order_id( OrderId ),
				info( OrderInfo ) ) );                              // ask user for confirm
		.wait( 30000 );                                             // wait for timeout ( 30s )
		?order( id( OrderId ), client( Client ),
                status( Status ), info( OrderInfo ) );              // retrieve order info
		if ( not Status == confirm & not .empty( Status ) ) {
			-+order( id( OrderId ), client( _ ), status( aborted ), info( _ ) );
		}.

// OPERATION #17 in purchase sequence schema
+!kqml_received( Sender, refuse, Content, MsgId )
	:   Content = abort( OrderId, OrderInfo )[ [] | Items ]
	<-  -+order( id( OrderId ), client( _ ), status( aborted ), info( _ ) ).

+order( id( OrderId ), client( Client ), status( aborted ), info( OrderInfo ) )
	<-  -order( id( OrderId ), client( Client ), status( aborted ), info( OrderInfo ) );
		.df_search( "management( items )", "retrieve( item )",
                Providers );                                        // search for agents able to manage items retrieve
        .nth( 0, Providers, Provider );                             // get the first agent
        .concat( release, Items, Msg );
        .term2string( TermMessage, Msg );
        .send( Provider, cfp, TermMessage ).                        // release items reservation

// OPERATION #18 in purchase sequence schema
+!kqml_received( Sender, confirm, Content, MsgId )
	:   Content = confirm( OrderId, OrderInfo )[ [] | Items ]
	<-  -order( id( OrderId ), client( _ ), status( pending ), info( _ ) );
		.println("TODO loop items").

/***********************************************************************************************************************
 Utils
 **********************************************************************************************************************/

+!order_id( Order, ResultId )                                                   // generate an ID for the order
	:   Order = order( client( Client ), address( Address ) )[ [] | Items ]
	<-  .date( Y, M, D );
		.time( H, Min, S );
		!str_concat( Client, [ Address, Y, M, D, H, Min, S ], ResultId ).       // concat order infos