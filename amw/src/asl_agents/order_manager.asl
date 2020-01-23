/* Initial beliefs and rules */

set( false ).                                                       // at start is not yet set

/* Initial goals */

!setup.                                                             // setup

/* Plans */

+!setup
	:   set( false )
	<-  .df_register( "management( orders )", "accept( order )" );  // register service as order acceptor
		-+set( true ).                                              // set process ended


// OPERATION #1 in purchase sequence schema
+!kqml_received( Sender, cfp, Content, MsgId )                      // try to accept an order
	:   Content = order( client( Client ), address( Address ) )[ Items ]
	<-  !order_id( Content, OrderId );
		.df_search( "management( items )", "retrieve( item )", Providers );
		.nth( 0, Providers, Provider );
        .send( Providers, cfp, retrieve( order_id( OrderId ) )[ Items ] ).        // ask if there're all items of the order

// OPERATION #5 in purchase sequence schema
+!kqml_received( Sender, propose, Content, MsgId )
	:   Content == error( "unable to find or reserve items" )
	<-  .send( Sender, propose, error( "unable to find or reserve items" ) ).

// OPERATION #7 in purchase sequence schema
+!kqml_received( Sender, propose, Content, MsgId )                  // TODO
	:   Content == ack( ItemPositions )
	<-  .df_search( "executor( item_picker )", "retrieve( item )", Providers );
    	.nth( 0, Providers, Provider );
    	.send( Provider, cfp, retrieve( itemX ) ).

// OPERATION #9 in purchase sequence schema
+!kqml_received( Sender, accept, Content, MsgId )
	:   Content = retrieve( Item )
	<-  //.println( Content );
		.send( Sender, confirm, retrieve( Item ) ).

// OPERATION #12 in purchase sequence schema
+!kqml_received( Sender, complete, Content, MsgId )
	:   Content = retrieve( Item )
	<-  .println( "Picking complete" );
		/* TODO */.



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

