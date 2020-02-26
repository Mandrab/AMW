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

+!setup                                                             // setup the agent
	:   set( false )
	<-  .df_register( "management( orders )", "accept( order )" );  // register service as order acceptor
		.include( "utils/literal.asl" );                            // include string utils plans
		.include( "utils/communication.asl" );                      // include communication utils plans
		-+set( true ).                                              // set process ended

///////////////////////////// ORDER RECEPTION:      OP #1 in order submission schema

+!kqml_received( Sender, achieve, Content, MsgId )                  // receive an order                                 // KQML achieve = ACL request: http://jason.sourceforge.net/doc/faq.html TODO remove
	:   Content = order( client( Client ), email( Email ), address( Address ) )[ [] | Items ]
	<-  !new_order_id( OrderId );                                   // generate an id for the order
		+order( id( OrderId ), status( checking ), client( Client ), email( Email ), address( Address ),
				items( Items ) );                                   // save order's info (status=checking for validity)
		.df_search( "management( items )", "retrieve( item )",
				Providers );                                        // search the agent(s) that manages the warehouse
		.nth( 0, Providers, Provider );                             // get the first ( agent )
		!concat( retrieve( order_id( OrderId ) ), Items, Res );
        .send( Provider, achieve, Res ).                            // ask for items reservation and positions

///////////////////////////// ERROR FROM WAREHOUSE: OP #6/11 in order submission schema

+!kqml_received( Sender, failure, Content, MsgId )                  // manage error from items retrieve
	:   Content = error( order_id( OrderId ), error_code( ErrorCode ) )
	<-	-order( id( OrderId ), status( checking ), client( _ ), email( Email ), address( _ ),
                items( _ ) );                                       // retrieve order information and remove the value
		.//!!asl_actions.send_feedback( Email, ErrorCode ).              // send failure mail TODO commentato

///////////////////////////// CONFIRM RETRIEVE:     OP #14 in order submission schema

+!kqml_received( Sender, confirm, Content, MsgId )                  // receive items position and reservation confirm
	:   Content = confirmation( order_id( OrderId ) )[ [] | Positions ]
	&   order( id( OrderId ), status( checking ), client( Client ), email( Email ), address( Address ), items( Items ) )
	<-  asl_actions.fuse( Items, Positions, Fused );
		//!!asl_actions.send_feedback( Email, 202, OrderId, Items );    //TODO non voglio intasare di mail
		!retrieve( OrderId, Fused ).                                // retrieve all the items

///////////////////////////// RECEIVE PROPOSAL:     OP #17 in order submission schema

//@retrieve_proposal[atomic]
+!kqml_received( Sender, propose, Content, MsgId )
	:   Content = retrieve( Id )
	&   retrieve( Id )[ order( OrderId ), state( unaccepted ), flat( F ) ]
	<-  -retrieve( Id )[ order( OrderId ), state( unaccepted ), flat( F ) ];
		+retrieve( Id )[ order( OrderId ), state( accepted ), flat( F ) ];
		.send( Sender, accept_proposal, Content ).

//@retrieve_unasked_proposal[atomic]
+!kqml_received( Sender, propose, Content, MsgId )
	:   Content = retrieve( Id )
	&   not retrieve( Id )[ order( _ ), state( unaccepted ), flat( _ ) ]
	<-  .send( Sender, reject_proposal, Content ).

///////////////////////////// REFUSE CFP

+!kqml_received( Sender, refuse, Content, MsgId )
	:   Content = retrieve( Id )
	&   retrieve( Id )[ order( _ ), state( unaccepted ), flat( ReshapedItem ) ]
	<-  !random_agent( "executor( item_picker )", "retrieve( item )",
                Provider );                                                     // get a random agent to contact
		.send( Provider, cfp, retrieve( id( Id ), item( ReshapedItem ) ) ).

+!kqml_received( Sender, refuse, Content, MsgId )
	:   Content = retrieve( Id )
	&   not retrieve( Id )[ order( OrderId ), state( unaccepted ), flat( ReshapedItem ) ].

///////////////////////////// TIMEOUT:              OP #19 in order submission schema

+!kqml_received( Sender, failure, Content, MsgId )
	:   Content = retrieve( Item )
	&   retrieve( ItemId )
	<-  !random_agent( "executor( item_picker )", "retrieve( item )",
                Provider );                                                     // get a random agent to contact
        .send( Provider, cfp, retrieve( id( Id ), item( ReshapedItem ) ) ).

///////////////////////////// COMPLETED RETRIEVE:   OP #26 in order submission schema

+!kqml_received( Sender, complete, Content, MsgId )
	:   Content = retrieve( Item )
	<-  -retrieve( Item )[ order( OrderId ), state( _ ), flat( ReshapedItem ) ];
		+retrieve( Item )[ order( OrderId ), state( completed ), flat( ReshapedItem ) ];
		!check_missing( OrderId ).

/***********************************************************************************************************************
 Utils
 **********************************************************************************************************************/

///////////////////////////// GENERATE ( SIMPLE ) ORDER ID

@next_order_id[atom]
+!new_order_id( "o0" )                                                          // generate an order ID
	:   not last_order_id( N ) <- +last_order_id( 0 ).

@first_order_id[atom]
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
	<-  -+last_item_id( N +1 );
		.concat( i, N + 1, S ).

///////////////////////////// RETRIEVE

+!retrieve( OrderId, Item, RetrieveId )
	:   Item = item( id( ItemId ), quantity( Quantity ) )[ [] | Positions ]
    &   Quantity == 1
	<-  !random_agent( "executor( item_picker )", "retrieve( item )",
                Provider );                                                     // get a random agent to contact
        !concat( item( ItemId ), Positions, ReshapedItem );
        .random( Multiplier );
        +retrieve( RetrieveId )[ order( OrderId ), state( unaccepted ), flat( ReshapedItem ) ];
        .send( Provider, cfp, retrieve( id( RetrieveId ),
                item( ReshapedItem ) ) );                                       // ask item retrieve
        !!start_timer( RetrieveId, Item ).

+!retrieve( OrderId, [ Item | [] ] )
	:   Item = item( id( ItemId ), quantity( Quantity ) )[ [] | Positions ]
	&   Quantity == 1
	<-  !new_item_id( RetrieveId );
		!retrieve( OrderId, Item, RetrieveId ).

+!retrieve( OrderId, [ Item | [] ] )
	:   Item = item( id( ItemId ), quantity( Quantity ) )[ [] | Positions ]
	&   Quantity > 1
	<-  !retrieve( OrderId, [ item( id( ItemId ), quantity( 1 ) )[ [] | Positions ] ] );
		!retrieve( OrderId, [ item( id( ItemId ), quantity( Quantity -1 ) )[ [] | Positions ] ] ).

+!retrieve( OrderId, [ Head | Tail ] )
	<-  !retrieve( OrderId, [ Head ] );
		!retrieve( OrderId, Tail ).

///////////////////////////// START TIMER ( HANDLE NO RESPONSE )

+!start_timer( RetrieveId, Item )
	:   retrieve( RetrieveId )[ order( _ ), state( unaccepted ), flat( ReshapedItem ) ]
	<-  .wait( 2000 );
		!check_acceptance( RetrieveId, Item ).

+!check_acceptance( RetrieveId, Item )
    :   retrieve( RetrieveId )[ order( OrderId ), state( State ), flat( _ ) ]
    &   State = unaccepted
    <-  !retrieve( OrderId, Item, RetrieveId ).

+!check_acceptance( RetrieveId, Item )
    :   retrieve( RetrieveId )[ order( _ ), state( State ), flat( _ ) ]
    &   not State = unaccepted.

///////////////////////////// CHECK IF ANY ITEM HAS NOT BEEN RETRIEVED

@checking_all_retrieved[atomic]
+!check_missing( OrderId )
	:   order( id( OrderId ), status( _ ), client( _ ), email( Email ), address( _ ), items( Items ) )
	<-  .findall( Id, retrieve( Id )[ order( OrderId ), state( unaccepted ), flat( F ) ]
				| retrieve( Id )[ order( OrderId ), state( accepted ), flat( F ) ], I );
		.length( I, L );
		if ( L == 0 ) {
			//!!asl_actions.send_feedback( Email, 200, OrderId, Items );
			// TODO
		}.
