/***********************************************************************************************************************
 Initial beliefs and rules
 **********************************************************************************************************************/

set( false ).                                                       // at start is not yet set

// initial known items and positions:
item( id( "Item 1" ), quantity( 12 ), reserved( 0 ) ) [
	position( rack( 5 ), shelf( 3 ), quantity( 5 ) ),
	position( rack( 6 ), shelf( 3 ), quantity( 7 ) )].
item( id( "Item 2" ), quantity( 1 ), reserved( 1 ) )[
	position( rack( 2 ), shelf( 4 ), quantity( 1 ) ) ].
item( id( "Item 3" ), quantity( 1 ), reserved( 0 ) )[
	position( rack( 2 ), shelf( 5 ), quantity( 1 ) ) ].

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
    <-  !sufficient( [ Items ], Result );                                // check if all the elements exists
        if ( Result = error_code( _ ) )     // if some don't not exist, then return an error
            { Msg = error( order_id( OrderId ), Result ); }
        else {
            !reserve( [ Items ], Positions );                       // try to reserve the items
            if ( Positions = error_code( _ ) )                      // if i get a conflict error, send it back
                { Msg = error( order_id( OrderId ), Positions ); }
            else
                { .concat( confirmation( order_id( OrderId ) ), Positions, Msg ); }
        }
        .term2string( TermMessage, Msg );
        .println( Msg );
        .send( Sender, propose, TermMessage ).                      // send the results ( eventually, an error )

// OPERATION #4 in purchase sequence schema
+!sufficient( [ Item | Tail ], Result )                             // search for the elements in the warehouse
	<-  !sufficient( Item, HeadRes );
		!sufficient( Tail, TailRes );
		.println(HeadRes);.println(TailRes);
		if( HeadRes = false | TailRes = false ) { Result = false; }
		else { Result = true; }
		.println(Result).

+!sufficient( Item, Result )                                        // check if there's a sufficient quantity of product
	:   Item = item( id( ItemId ), quantity( RequiredNum ) )
	&   item( id( ItemId ), quantity( RequiredNum ), reserved( ReservedNum ) )
	<-  if ( StoredNum - ReservedNum < RequiredNum ) { Result = false; }
		else { Result = true; }.

// OPERATION #9 in purchase sequence schema
+!reserve( [ Item | Tail ], Positions )                             // TODO 409 conflic
	:   Item = item( id( ItemId ), quantity( RequiredNum ) )
	<-  -item( id( ItemId ), quantity( StoredNum ),
				reserved( ReservedNum ) )[ Pos ];                   // retrieve information about quantity

		if ( StoredNum - ReservedNum < RequiredNum )
                { Result = error_code( "409, conflict" ); }         // if too low quantity -> error

		+item( id( ItemId ), quantity( StoredNum ),
				reserved( ReservedNum + RequiredNum ) )[ Pos ];     // reserve quantity

		if ( not .empty( Tail ) ) { !reserve( Tail, Res ); }
		else { Res = []; }
		Positions = [ Pos | Res ].

+!kqml_received( Sender, cfp, Content, MsgId )                      // send the warehouse state (items info & position)
	:   Content = info( warehouse )
    <-  .findall( item( id( ItemId ), quantity( QT ), reserved( R ) )
                [ position( rack( RK ), shelf( S ), quantity( Q ) ) ],
                item( id( ItemId ), quantity( QT ), reserved( R ) )
                [ position( rack( RK ), shelf( S ), quantity( Q ) ) ], L);
        !reshape( L, Res );
        .send( Sender, propose, Res, MsgId ).

+!reshape( [ Head | Tail ], Result )
	:   Head = item( id( ItemId ), quantity( Quantity ), reserved( ReservedNumber ) )[ Pos ]
	<-  if ( not .empty( Tail ) ) { !reshape( Tail, Res ); }
		else { Res = []; }
		Result = [ item( id( ItemId ) )[ Pos ] | Res ].