/***********************************************************************************************************************
 Initial beliefs and rules
 **********************************************************************************************************************/

set( false ).                                                       // at start is not yet set

// known collection points
point( 0 )[ x( 50 ), y( 50 ), state( available ) ].
point( 1 )[ x( 50 ), y( 70 ), state( available ) ].
point( 2 )[ x( 50 ), y( 90 ), state( available ) ].
point( 3 )[ x( 50 ), y( 110 ), state( available ) ].
point( 4 )[ x( 50 ), y( 130 ), state( available ) ].
point( 5 )[ x( 50 ), y( 150 ), state( available ) ].




/***********************************************************************************************************************
 Initial goals
 **********************************************************************************************************************/

!setup.                                                             // setup




/***********************************************************************************************************************
 Plans
 **********************************************************************************************************************/

///////////////////////////// AGENT SETUP

+!setup : set( false )
	<-  .df_register( "management( items )", "info( collection_points )" ); // register as collection points dispatcher
		-+set( true ).                                              // set process ended




/////////////////////////////////////////////// COLLECTION POINT REQUEST ///////////////////////////////////////////////

///////////////////////////// COLLECTION POINT REQUEST

+!kqml_received( Sender, cfp, collection_point( OrderID ), MsgID )
	<-  !propose( Sender, PointID, OrderID );
        .wait( 5000 );
        !check_acceptance( Sender, collection_point( OrderID )[ err_code( request_timeout ) ], OrderID ).

-!kqml_received( Sender, cfp, collection_point( OrderID ), MsgID )
	<-  .send( Sender, failure, collection_point( OrderID ) ).

///////////////////////////// POINT ACCEPTED

@accept_proposal[atomic]
+!kqml_received( Sender, accept_proposal, Content, MsgID )         // receive a collection point request
	:   Content = OrderID[ x( XPos ), y( YPos ), state( pending ) ]
	<-  -+point( ID )[ x( XPos ), y( YPos ), state( reserved )[ OrderID ] ].

// TODO reject_proposal


//////////////////////////////////////////////////// UTILITY PLANS /////////////////////////////////////////////////////

///////////////////////////// PROPOSE POINT

@propose[atomic]
+!propose( Sender, PointID, OrderID )
	:   point( PointID )[ x( XPos ), y( YPos ), state( available ) ]
	<-  -+point( PointID )[ x( XPos ), y( YPos ), state( pending )[ OrderID ] ];
        .send( Sender, propose, order_id( OrderID )[ x( XPos ), y( YPos ) ] ).

/////////////////////////////

@accepted_proposal[atomic]
+!check_acceptance( Sender, Id, OrderID )                             // the propose has been accepted
	:   point( ID )[ x( XPos ), y( YPos ), state( reserved ) ].     // do nothing here

@unaccepted_proposal[atomic]
+!check_acceptance( Sender, Msg, OrderID )                            // the propose hasn't been accepted
	:   point( ID )[ x( XPos ), y( YPos ), state( pending )[ OrderID ] ]  // if i got no response ...
	<-  -+point( ID )[ x( XPos ), y( YPos ), state( available ) ];  // ... stop waiting and reset as available
        .send( Sender, failure, Msg ).                       // send timeout failure