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

///////////////////////////// AGENT SETUP

@setup[atomic] +!setup                                              // setup the agent
	:   set( false )
	<-  .df_register( "management( orders )", "accept( order )" );  // register service as order acceptor
		.include( "util/literal.asl" );                            // include string utils plans
		.include( "util/communication.asl" );                      // include communication utils plans
		-+set( true ).                                              // set process ended




//////////////////////////////////////////////////// ORDER REQUEST /////////////////////////////////////////////////////

///////////////////////////// ORDER RECEPTION

+!kqml_received( Sender, achieve, Content, MsgID )                  // receive an order                                 // KQML achieve = ACL request: http://jason.sourceforge.net/doc/faq.html TODO remove
	:   Content = order( client( Client ), email( Email ), address( Address ) )[ [] | Items ]
	<-  !new_order_id( OrderID );                                   // generate an id for the order
		ClientInfo = client( name( Client ), address( Address ), email( Email ) )[ aid( Sender ) ];
		+order( id( OrderID ), ClientInfo, status( checking_items ),
				items( Items ) );                                   // save order's info (status=checking for validity)
		.df_search( "management( items )", "retrieve( item )",
				Providers );                                        // search the agent(s) that manages the warehouse
        .nth( 0, Providers, Provider );                             // get the first ( agent )
		!concat( retrieve( order_id( OrderID ) ), Items, Res );
        .send( Provider, achieve, Res ).                            // ask for items reservation and positions




////////////////////////////////////////////////// WAREHOUSE RESPONSE //////////////////////////////////////////////////

///////////////////////////// ERROR

+!kqml_received( Sender, failure, Content, MsgID )                  // manage error from items retrieve
	:   Content = error( order_id( OrderID ), error_code( ErrorCode ) )
	&   order( id( OrderID ), status( checking_items ), client( _, _, email( Email ) )[ aid( Client ) ], items( Items ) )
	<-	-order( id( OrderID ), _, _, _ );
		.send( Client, failure, order( id( OrderID ), items( Items ) )[ err_code( ErrorCode ) ] );
		.//!!send_feedback( Email, ErrorCode ).         // send failure mail TODO commentato

///////////////////////////// CONFIRMATION

+!kqml_received( Sender, confirm, Content, MsgID )                  // receive items position and reservation confirm
	:   Content = confirmation( order_id( OrderID ) )[ [] | Positions ]
	<-  -order( id( OrderID ), ClientInfo, status( checking_items ), items( Items ) );
		fuse( Items, Positions, Fused );
        +order( id( OrderID ), ClientInfo, status( checking_gather_point ), items( Fused ) );
        !gather( OrderID ).                                         // gather items




////////////////////////////////////////// COLLECTION POINTS MANAGER RESPONSE //////////////////////////////////////////

///////////////////////////// PROPOSAL ACCEPT

+!kqml_received( Sender, propose, Content, MsgID )                  // receive items position and reservation confirm
	:   Content = order_id( OrderID )[ x( XPos ), y( YPos ) ]
	<-  !item_of_order( OrderID, Result );
		if ( not Result = false ) {
			.send( Sender, accept_proposal, Content, MsgID );
            //send_feedback( Email, 202, OrderId, Result );    //TODO non voglio intasare di mail
            !retrieve( OrderID, Result );                                // retrieve all the items
        }.

@item_of_order[atomic]
+!item_of_order( OrderID, Items )
	:   order( id( OrderID ), ClientInfo, status( checking_gather_point ), items( Items ) )
	<-  -order( id( OrderID ), _, _, _ );
		+order( id( OrderID ), ClientInfo, status( accepted ), items( Items ) ).

@item_of_order_fail[atomic]
+!item_of_order( OrderID, false )
:  order( id( OrderID ), ClientInfo, status( S ), items( Items ) )<-.println(S).


+!kqml_received( Sender, failure, Content, MsgID ).                 // it will automatically retry

// TODO refuse proposal




//////////////////////////////////////////////////// ROBOT RESPONSE ////////////////////////////////////////////////////

///////////////////////////// PROPOSAL ACCEPT

@retrieve_proposal[atomic]
+!kqml_received( Sender, propose, Content, MsgID )
	:   Content = retrieve( ID )
	&   retrieve( ID )[ order( OrderID ), state( unaccepted ), flat( F ) ]
	<-  -retrieve( ID )[ order( OrderID ), state( unaccepted ), flat( F ) ];
		+retrieve( ID )[ order( OrderID ), state( accepted ), flat( F ) ];
		.send( Sender, accept_proposal, Content ).

///////////////////////////// PROPOSAL REFUSE

@retrieve_unasked_proposal[atomic]
+!kqml_received( Sender, propose, retrieve( ID ), MsgID )
	:   not retrieve( ID )[ order( _ ), state( unaccepted ), flat( _ ) ]
	<-  .send( Sender, reject_proposal, retrieve( ID ) ).

///////////////////////////// REFUSE CFP

+!kqml_received( Sender, refuse, Content, MsgID )
	:   Content = retrieve( ID )
	&   retrieve( ID )[ order( _ ), state( unaccepted ), flat( ReshapedItem ) ]
	<-  !random_agent( "executor( item_picker )", "retrieve( item )",
                Provider );                                                     // get a random agent to contact
		.send( Provider, cfp, retrieve( id( ID ), item( ReshapedItem ) ) ).

+!kqml_received( Sender, refuse, Content, MsgID )
	:   Content = retrieve( ID )
	&   not retrieve( ID )[ order( OrderID ), state( unaccepted ), flat( ReshapedItem ) ].

///////////////////////////// FAILURE

+!kqml_received( Sender, failure, Content, MsgID )
	:   Content = retrieve( Item )
	&   retrieve( ItemID )[ _, state( unaccepted ), _ ]
	<-  !random_agent( "executor( item_picker )", "retrieve( item )",
                Provider );                                                     // get a random agent to contact
        .send( Provider, cfp, retrieve( id( ID ), item( ReshapedItem ) ) ).

///////////////////////////// COMPLETED

+!kqml_received( Sender, complete, Content, MsgID )
	:   Content = retrieve( Item )
	<-  -retrieve( Item );
		+retrieve( Item )[ order( OrderID ), state( completed ), flat( ReshapedItem ) ];
		!check_missing( OrderID ).



/***********************************************************************************************************************
 Utils
 **********************************************************************************************************************/

//////////////////////////////////////////////////// ID GENERATION /////////////////////////////////////////////////////

///////////////////////////// GENERATE ( SIMPLE ) ORDER ID

@first_order_id[atom]
+!new_order_id( "o0" )                                                          // generate an order ID
	:   not last_order_id( N ) <- +last_order_id( 0 ).

@next_order_id[atom]
+!new_order_id( S )                                                             // generate an order ID
	:   last_order_id( N )
	<-  -+last_order_id( N + 1 );
		.concat( o, N + 1, S ).

///////////////////////////// GENERATE ( SIMPLE ) ITEM ID

@next_item_id[atom]
+!new_item_id( "i0" )                                                           // generate an ID for a item retrieve
	:   not last_item_id( N ) <- +last_item_id( 0 ).

@first_item_id[atom]
+!new_item_id( S )                                                              // generate an ID for a item retrieve
	:   last_item_id( N )
	<-  -+last_item_id( N + 1 );
		.concat( i, N + 1, S ).

//////////////////////////////////////////////// ORDER'S ITEMS GATHERING ///////////////////////////////////////////////

///////////////////////////// GATHER

+!gather( OrderID )
	<-  !require_gather_point( OrderID );
        .wait( 5000 );
        !verify_obtainment( OrderID, Result );
        if ( not Result ) { !gather( OrderID ); }.

@require_gather_point[atomic] +!require_gather_point( OrderID )
	:   order( id( OrderID ), _, _, _ )
	<-  !random_agent( "management( items )", "info( collection_points )", Provider );
        .send( Provider, cfp, collection_point( OrderID ) ).

+!verify_obtainment( OrderID, true ) : order( id( OrderID ), _, status( accepted ), _ ).

+!verify_obtainment( OrderID, false ) : not order( id( OrderID ), _, status( accepted ), _ ).

///////////////////////////// RETRIEVE

+!retrieve( OrderID, Item, RetrieveID )
	:   Item = item( id( ItemID ), quantity( 1 ) )[ [] | Positions ]
	<-  !random_agent( "executor( item_picker )", "retrieve( item )",
                Provider );                                                     // get a random agent to contact
        !concat( item( ItemID ), Positions, ReshapedItem );
        +retrieve( RetrieveID )[ order( OrderID ), state( unaccepted ), flat( ReshapedItem ) ];
        .send( Provider, cfp, retrieve( id( RetrieveID ), item( ReshapedItem ) ) ); // ask item retrieve
        !!start_timer( RetrieveID, Item ).

+!retrieve( OrderID, [ Item | [] ] )
	:   Item = item( id( ItemID ), quantity( 1 ) )[ [] | Positions ]
	<-  !new_item_id( RetrieveID );
		!retrieve( OrderID, Item, RetrieveID ).

+!retrieve( OrderID, [ Item | [] ] )
	:   Item = item( id( ItemID ), quantity( Quantity ) )[ [] | Positions ] & Quantity > 1
	<-  !retrieve( OrderID, [ item( id( ItemID ), quantity( 1 ) )[ [] | Positions ] ] );
		!retrieve( OrderID, [ item( id( ItemID ), quantity( Quantity -1 ) )[ [] | Positions ] ] ).

+!retrieve( OrderID, [ Head | Tail ] )
	<-  !retrieve( OrderID, [ Head ] );
		!retrieve( OrderID, Tail ).

///////////////////////////// START RETRIEVE TIMER ( HANDLE NO RESPONSE )

+!start_timer( RetrieveID, Item )
	:   retrieve( RetrieveID )[ order( _ ), state( unaccepted ), flat( ReshapedItem ) ]
	<-  .wait( 5000 );
		!check_acceptance( RetrieveID, Item ).

+!check_acceptance( RetrieveID, Item )
    :   retrieve( RetrieveID )[ order( OrderID ), state( unaccepted ), flat( _ ) ]
    <-  !retrieve( OrderID, Item, RetrieveID ).

+!check_acceptance( RetrieveID, Item )
    :   not retrieve( RetrieveID )[ order( _ ), state( unaccepted ), flat( _ ) ].

///////////////////////////// CHECK IF ANY ITEM HAS NOT BEEN RETRIEVED

@checking_all_retrieved[atomic]
+!check_missing( OrderID )
	:   order( id( OrderID ), client( _, _, email( Email ) ), status( _ ), items( Items ) )
	<-  .findall( ID, retrieve( ID )[ order( OrderID ), state( unaccepted ), flat( F ) ]
				| retrieve( ID )[ order( OrderID ), state( accepted ), flat( F ) ], I );
		.length( I, L );
		if ( L == 0 ) {
			//!!send_feedback( Email, 200, OrderID, Items );
			// TODO
		}.
