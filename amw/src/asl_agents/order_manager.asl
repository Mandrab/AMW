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
		-+set( true ).                                              // set process ended


// OPERATION #1 in purchase sequence schema
+!kqml_received( Sender, cfp, Content, MsgId )                      // try to accept an order
	:   Content = order( client( Client ), address( Address ) )[ Items ]
	<-  .println( Content );
		!order_id( Content, OrderId );                              // generate an id for the order
		+order( id( OrderId ), client( Sender ),
				status( checking ), info( Content ) ); // save the order info
		.df_search( "management( items )", "retrieve( item )",
				Providers );                                        // search for agents able to manage items retrieve
		.nth( 0, Providers, Provider );                             // get the first agent
		Items = [ Head | Tail ];
        .send( Provider, cfp,
                retrieve( order_id( OrderId ) ) [ Head | Items ] ).         // ask if there're all items of the order

// OPERATION #6/11 in purchase sequence schema
+!kqml_received( Sender, propose, Content, MsgId )
	:   Content = error( order_id( OrderId ), error_code( ErrorCode ) )
	<-	.println( Content );
        -order( id( OrderId ), client( Client ),
				status( checking ), info( OrderInfo ) );            // retrieve order information and remove the value
		.send( Client, propose, error( order_id( OrderId ),
				error_code( ErrorCode ) ) ).                        // send the error message

// OPERATION #14 in purchase sequence schema
+!kqml_received( Sender, propose, Content, MsgId )
	:   Content = confirmation( order_id( OrderId ) )[ Positions ]
	<-  .println( Content );
        -order( id( OrderId ), client( Client ),
        		status( checking ), info( OrderInfo ) );            // remove old order status
		+order( id( OrderId ), client( Client ),
				status( pending ), info( OrderInfo ) );             // update order status and t
		.send( Client, propose, confirmation( order_id( OrderId ),
				info( OrderInfo ) ) );                              // ask user for confirm
		.wait( 30000 );                                             // wait for timeout ( 30s )
		?order( id( OrderId ), client( Client ),
                status( Status ), info( OrderInfo ) );              // retrieve order info
		if ( not Status == confirm & not .empty( Status ) ) {
			-+order( id( OrderId ), client( _ ), status( aborted ), info( _ ) );
		}.

// OPERATION #17 in purchase sequence schema
+!kqml_received( Sender, propose, Content, MsgId )
	:   Content = abort( OrderId, OrderInfo )[ Items ]
	<-  .println( Content );
		-+order( id( OrderId ), client( _ ), status( aborted ), info( _ ) ).

+order( id( OrderId ), client( Client ), status( aborted ), info( OrderInfo ) )
	<-  -order( id( OrderId ), client( Client ), status( aborted ), info( OrderInfo ) );
		.df_search( "management( items )", "retrieve( item )",
                Providers );                                        // search for agents able to manage items retrieve
        .nth( 0, Providers, Provider );                             // get the first agent
        .send( Provider, cfp, release[ Items ] ).                   // release items reservation

// OPERATION #18 in purchase sequence schema
+!kqml_received( Sender, propose, Content, MsgId )
	:   Content = confirm( OrderId, OrderInfo )[ Items ]
	<-  .println( Content );
        -order( id( OrderId ), client( _ ), status( pending ), info( _ ) );
		.println("TODO loop items").

/***********************************************************************************************************************
 Utils
 **********************************************************************************************************************/

+!order_id( Order, OrderId )
	:   Order = order( client( Client ), address( Address ) )[ Items ]
	<-  .date( Y, M, D );
		.time( H, Min, S );
		!str_concat( Client, [ Address, Y, M, D, H, Min, S ], OrderId ).

+!str_concat( Str1, [ Str2 | Other ], Result )
	<-  if ( not .string( Str1 ) ) { .term2string( Str1, S1 ); }
		else { S1 = Str1; }
		if ( not .string( Str2 ) ) { .term2string( Str2, S2 ); }
		else { S2 = Str2; }
		.concat( S1, S2, Res );
		if ( not .empty( Other ) ) { !str_concat( Res, Other, Result ); }
		else { Result = Res; }.

