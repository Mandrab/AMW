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

+!setup : set( false )
	<-  .df_register( "management( items )", "info( warehouse )" ); // register as warehouse's infos dispatcher
		.df_register( "management( items )", "store( item )" );     // register for acquire information about items
		.df_register( "management( items )", "find( item )" );      // register for supply item's position infos
		.df_register( "management( items )", "retrieve( item )" );  // register for remove infos at item removal
		-+set( true ).                                              // set process ended

// OPERATION #3 in purchase sequence schema
@processOrder[atomic]
+!kqml_received( Sender, cfp, Content, MsgId )                      // receive the intention of pick item(s)
	:   Content = retrieve( order_id( OrderId ) )[ [] | Items ]
    <-  !sufficient( Items, SResult );                              // check if all the elements exists
        if ( not SResult ) {                                        // if at least an item doesn't exist, send error msg
            .send( Sender, propose, error( order_id( OrderId ), error_code( "404, not found" ) ) );
        } else {
            !reserve( Items, RResult );                         // try to reserve the items
            .println( RResult );
            if ( RResult = failed ) {                               // if i get a conflict error, send it back
                .send( Sender, propose, error( order_id( OrderId ), error_code( "409, conflict" ) ) );
            } else {
                !concat( confirmation( order_id( OrderId ) ), RResult, Msg );
                .send( Sender, propose, Msg );
            }
        }.

// OPERATION #4 in purchase sequence schema
+!sufficient( [ Item ], Result )                                    // check if there's a sufficient quantity of product
	:   Item = item( id( ItemId ), quantity( RequiredQ ) )
	<-  .eval( Result, item( id( ItemId ), quantity( StoredQ ), reserved( ReservedQ ) )
			& RequiredQ < StoredQ - ReservedQ ).                    // return result

+!sufficient( [ Item | Tail ], Result )                             // check if the prods's quantities are sufficient
	<-  !sufficient( [ Item ], HeadRes );                           // check for the first element
		!sufficient( Tail, TailRes );                               // check for the remaining elements
		.eval( Result, HeadRes == false | TailRes == false ).       // return result

// OPERATION #9 in purchase sequence schema
+!reserve( [ Item ], Positions )
	:   Item = item( id( ItemId ), quantity( RequiredQ ) )
	&   item( id( ItemId ), quantity( StoredQ ), reserved( ReservedQ ) )
	&   RequiredQ < StoredQ - ReservedQ
	<-  -item( id( ItemId ), quantity( StoredQ ), reserved( ReservedQ ) )[ Positions ];
		+item( id( ItemId ), quantity( StoredQ ), reserved( ReservedQ + RequiredQ ) )[ Positions ].

+!reserve( [ Item ], Result )
	:   Item = item( id( ItemId ), quantity( RequiredQ ) )
    &   ( not item( id( ItemId ), quantity( StoredQ ), reserved( ReservedQ ) ) | StoredQ - ReservedQ < RequiredQ )
	<-  Result = failed.

+!reserve( [ Item | Tail ], Positions )
	<-  !reserve( [ Item ], HeadRes );
		!reserve( Tail, TailRes );
		if( HeadRes = failed | TailRes = failed ) { Result = failed; }
		else { Result = [ HeadRes | TailRes ]; }.

/*/ OPERATION #9 in purchase sequence schema
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
		Positions = [ Pos | Res ].*/

+!kqml_received( Sender, cfp, Content, MsgId )                      // send the warehouse state (items info & position)
	:   Content = info( warehouse )
    <-  .findall( item( id( ItemId ), quantity( QT ), reserved( R ) )
                [ position( rack( RK ), shelf( S ), quantity( Q ) ) ],
                item( id( ItemId ), quantity( QT ), reserved( R ) )
                [ position( rack( RK ), shelf( S ), quantity( Q ) ) ], L);
        !reshape( L, Res );
        .send( Sender, propose, Res, MsgId ).

/***********************************************************************************************************************
 Utils
 **********************************************************************************************************************/

+!reshape( [ Head | Tail ], Result )
	:   Head = item( id( ItemId ), quantity( Quantity ), reserved( ReservedNumber ) )[ Pos ]
	<-  if ( not .empty( Tail ) ) { !reshape( Tail, Res ); }
		else { Res = []; }
		Result = [ item( id( ItemId ) )[ Pos ] | Res ].