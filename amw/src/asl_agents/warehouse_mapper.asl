/* Initial beliefs and rules */

set( false ).                                                       // at start is not yet set

item( "Item 1" )[ rack(5), shelf(3), quantity(5) ].                 // initial known items
item( "Item 2" )[ rack(2), shelf(4), quantity(1) ].
item( "Item 3" )[ rack(2), shelf(5), quantity(1) ].



/* Initial goals */

!setup.                                                             // setup



/* Plans */

+!setup
	:   set( false )
	<-  .df_register( "management( items )", "info( warehouse )" ); // register as warehouse's infos dispatcher
		.df_register( "management( items )", "store( item )" );     // register for acquire information about items
		.df_register( "management( items )", "find( item )" );      // register for supply item's position infos
		.df_register( "management( items )", "retrieve( item )" );  // register for remove infos at item removal
		-+set( true ).                                              // set process ended


// OPERATION #2 in purchase sequence schema
@processOrder[atomic]
+!kqml_received( Sender, cfp, Content, MsgId )                      // receive the intention of pick item(s)
	:   Content = retrieve( order_id( OrderId ) )[ Items ]
    <-  !search( Items, Result );                                   // search for items infos
        if ( not Result = error( _ ) ) { !reserve( Result ); };     // if items have been found, then reserve them
        .send( Sender, propose, ack("positions") ).                 // send the positions of the items

// OPERATION #5 in purchase sequence schema
-!kqml_received( Sender, cfp, Content, MsgId )
	<-  .send( Sender, propose, error( "unable to find or reserve items" ) ).

+!kqml_received( Sender, cfp, Content, MsgId )                      // receive the intention of pick item(s)
	:   Content = info( warehouse )
    <-  .findall( item( ItemName )[ rack( R ), shelf( S ), quantity( Q ) ], item( ItemName )[ rack( R ), shelf( S ), quantity( Q ) ], L);
        .send( Sender, propose, L ).                                // TODO probably, ".send" from jason.stdlib send message using setContent instead setContentObject

// OPERATION #3 in purchase sequence schema
+!search( [ Item | Tail ], Result )                                 // Search for the elements position in the warehouse
	<-  if ( not .empty( Tail ) ) { !search( Tail, Res ); }
		else { Res = [ ]; }
		?item( Item )[ rack( RackN ), shelf( ShelfN ), quantity( QuantityN ) ];
		Result = [ item( Item )[ rack( RackN ), shelf( ShelfN ), quantity( QuantityN ) ] | Res ].

// OPERATION #4 in purchase sequence schema
+!reserve( [ Item, Tail] )
	<-  if ( not .empty( Tail ) ) { !reserve( Tail ); }
		!reserve( Item ).

+!reserve( Item )
	<-  -item( ItemId )[ rack( RackN ), shelf( ShelfN ), quantity( QuantityN ) ];
		if ( QuantityN -1 > 0 ) {
            +item( ItemId )[ rack( RackN ), shelf( ShelfN ), quantity( QuantityN - 1 ) ]; }
        if ( reserved_item( ItemId ) ) {
            -+reserved_item( ItemId )[ rack( RackN ), shelf( ShelfN ), quantity( QuantityN +1 ) ]; }
        else {
            +reserved_item( ItemId )[ rack( RackN ), shelf( ShelfN ), quantity( 1 ) ]; }.