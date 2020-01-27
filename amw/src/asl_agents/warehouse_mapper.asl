/***********************************************************************************************************************
 Initial beliefs and rules
 **********************************************************************************************************************/

set( false ).                                                       // at start is not yet set

// initial known items:
item( "Item 1" )[ rack(5), shelf(3), quantity(5), reserved( 0 ) ].
item( "Item 2" )[ rack(2), shelf(4), quantity(1), reserved( 1 ) ].
item( "Item 3" )[ rack(2), shelf(5), quantity(1), reserved( 0 ) ].

/***********************************************************************************************************************
 Initial goals
 **********************************************************************************************************************/

!setup.                                                             // setup

/***********************************************************************************************************************
 Plans
 **********************************************************************************************************************/

+!setup
	:   set( false )
	<-  .df_register( "management( items )", "info( warehouse )" ); // register as warehouse's infos dispatcher
		.df_register( "management( items )", "store( item )" );     // register for acquire information about items
		.df_register( "management( items )", "find( item )" );      // register for supply item's position infos
		.df_register( "management( items )", "retrieve( item )" );  // register for remove infos at item removal
		-+set( true ).                                              // set process ended


// OPERATION #3 in purchase sequence schema
@processOrder[atomic]
+!kqml_received( Sender, cfp, Content, MsgId )                      // receive the intention of pick item(s)
	:   Content = retrieve( order_id( OrderId ) )[ Items ]
    <-  !exist( Items, Result );                                    // check that all the elements exists
        if ( Result == error( _ ) ) {                               // if some don't not exist, then return an error
            .send( Sender, propose,
                    error( order_id( OrderId ), Result ) );
        } else {
            !reserve( Items, Positions );                           // try to reserve the items
            if ( Positions == error_code( _ ) ) {                   // if i get a conflict error, send it back
                .send( Sender, propose, error( order_id( OrderId ), Positions ) ); }
            .send( Sender, propose, confirmation( order_id( OrderId ) )
                    [ Positions ] );                                // send the positions of the items
        }.

// OPERATION #4 in purchase sequence schema
+!exist( [ Item | Tail ], Result )                                  // search for the elements in the warehouse
	<-  ?item( Item );                                              // check if the element exist
		if ( not .empty( Tail ) ) { !exist( Tail, Res ); }.         // search for the element in the tail (if present)

-!exist( [ Item | Tail ], Result )                                  // if an item does not exist, then return an error
	<-  Result = error_code( "404, not found" ).

// OPERATION #9 in purchase sequence schema
+!reserve( [ Item | Tail ], Positions )                               // TODO 409 conflic
	<-  -+item( ItemId )[ rack( RackN ), shelf( ShelfN ), quantity( QuantityN -1 ), reserved( ReservedN +1 ) ];
		if ( not .empty( Tail ) ) { !reserve( Tail, Res ); };
		Result = [ item( ItemId )[ rack( RackN ), shelf( ShelfN ) ] | Res ].

+!kqml_received( Sender, cfp, Content, MsgId )                      // send the warehouse state (items info & position)
	:   Content = info( warehouse )
    <-  .findall( item( ItemName )[ rack( R ), shelf( S ),
                quantity( Q ), reserved( RQ ) ],
                item( ItemName )[ rack( R ), shelf( S ),
                quantity( Q ), reserved( RQ ) ], L);
        .send( Sender, propose, L ).