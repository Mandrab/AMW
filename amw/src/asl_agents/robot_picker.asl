/* Initial beliefs and rules */

set( false ).                                                       // at start is not yet set

/* Initial goals */

!setup.                                                             // setup

/* Plans */

+!setup
	:   set( false )
	<-  .df_register( "executor( item_picker )", "retrieve( item )" );  // register for pick items
		-+set( true );                                                  // set process ended
		+activity( working );
		!work.



+!work
	:   activity( working )
	<-  .println( "Working hard!!!" );
		.wait( 5000 );
		!work.

+!work
	:   activity( picking )[ client( Client ), item( Item ) ]
	<-  .println( "Picking!!!" ); .wait( 5000 );
		.send( Client, complete, retrieve( Item ) );            // confirm completion
        -+activity( working );                                  // restart working
        !work.

// OPERATION #8 in purchase sequence schema
+!kqml_received( Sender, cfp, Content, MsgId )                  // receive request for item pick
	:   Content = retrieve( Item ) & activity( working )
    <-  -+activity( waiting );
        .send( Sender, accept, retrieve( Item ) );              // accept the work
        .wait( 5000 );                                          // wait confirm
        if ( activity( waiting ) ) { -+activity( working ); }.  // if it takes too long, restart working

// OPERATION #10 in purchase sequence schema
+!kqml_received( Sender, confirm, Content, MsgId )              // receive confirm for pick work
	:   Content = retrieve( Item ) & activity( waiting )
    <-  -+activity( picking )[ client( Sender ), item( Item ) ].// pick item for client

+!kqml_received( Sender, confirm, Content, MsgId )              // if i receive the confirm for the work too late
	:   Content = retrieve( Item ) & not activity( waiting )
    <-  .send( Sender, tell, error( "confirm took too long" ) );// send error
        -+activity( working ).