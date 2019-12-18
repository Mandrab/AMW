/* Initial beliefs and rules */

set( false ).                                                       // at start is not yet set



/* Initial goals */

!setup.                                                             // setup



/* Plans */

+!setup
	:   set( false )
	<-  .df_register( "management( orders )", "accept( order )" );  // register service as order acceptor
		-+set( true );
		.println("Order Manager set up").                           // set process ended



+!kqml_received( Sender, cfp, Content, MsgId )                      // receive order
    <-  asl_actions.order_specs( Content, _, _, Item );             // retrieve infos from the order

        .df_search( "management( items )", "find( item )", Providers );
        if ( .empty( Providers ) )                                  // if no provider exist, send an error
            { .send( Sender, tell, "Error: items positions not known" ); }       // TODO propose?

		!save_order( Sender, Content, OrderId );
        .nth( 0, Providers, Provider );                             // take the first provider
        .send( Providers, askOne, [ OrderId | [ find | Item ] ] ).  // ask for item's info



+!kqml_received( Sender, tell, [ OrderId | StoredItems ], MsgId )   // receive items infos
	:   order( Client, Content, OrderId )
	<-  asl_actions.order_specs( Content, _, _, OrderItems );
		!check_availability( OrderItems, StoredItems );             // check if items are available
		.send( Sender, cfp, [ retrieve | OrderItems ] ).




+!save_order( Sender, Content, UniqueId )
	<-  .date( Y, M, D ); .time( H, Min, S );
		asl_actions.order_specs( Content, Client, Address, _ );     // retrieve infos from the order
		!concat_str( [ Client, Address, Y, M, D, H, Min, S ], UniqueId );
        +order( Sender, Content, UniqueId ).                        // save order infos

+!concat_str( [ L1 | [ Head | Tail ] ], Result )                    // concat a list of string
	<-  if ( .empty( Tail ) ) { Result = L1; }
		else {
			.concat( L1, Head, Res );
			!concat_str( [ Res | Tail ], Result ); }.



+!check_availability( OrderItems, StoredItems )
	<-  if ( not .empty( OrderItems ) ) {
			.sort( OrderItems, [ Head1 | Tail1 ] );
			.sort( StoredItems, [ [ Name2 | [ Rack | [ Shelf | [ Quantity ] ] ] ] | Tail2 ] );
			if ( not Head1 == Name2 )                                   // skip element if different
				{ !check_availability( [ Head1 | Tail1 ], Tail2 ); }
			elif ( Quantity - 1 > 0 ) {
				!check_availability( Tail1, [ [ Name2 | [ Rack | [ Shelf | [ Quantity -1 ] ] ] ] | Tail2 ] );
			} else {
				!check_availability( Tail1, Tail2 );
			}
		}.


		//?Head2( _, quantity( QuantityN ), _, _ );
		//.println( QuantityN ).
		//if ( ?Head2() )
		//check_availability( Tail1, Tail2 ).
