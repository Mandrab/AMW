/***********************************************************************************************************************
 Initial beliefs and rules
***********************************************************************************************************************/

set( false ).                                                       // at start is not yet set

/***********************************************************************************************************************
 Initial goals
***********************************************************************************************************************/

!setup.                                                             // setup

/***********************************************************************************************************************
 Plans
***********************************************************************************************************************/

+!setup
	:   set( false )
	<-  .df_register( "executor( item_picker )", "retrieve( item )" );  // register for pick items
		.df_register( "executor( command )", "exec( command )" );       // register for pick items
		-+set( true );                                                  // set process ended
		+activity( working );
		+state( available );
		!work.



+!work
	:   activity( working )
	<-  .println( "Doing stuffs ..." );
		.wait( 5000 );
		!work.

-!work.



@picking[atomic]
+activity( picking )[ client( Client ), item( Item ) ]
	<-  .println( "Picking!!!" );
		.wait( 5000 );
		.send( Client, complete, retrieve( Item ) );            // confirm completion
        -+activity( working );                                  // restart working
        !work.



// OPERATION #15 in purchase sequence schema
+!kqml_received( Sender, cfp, Content, MsgId )                  // request of item picking
	:   Content = retrieve( item( Item )[ [] | Positions ] )
    <-  !propose_retrieve( Sender, Item );                      // propose to retrieve the item
        .wait( 5000 );                                          // max time to wait for confirm
        !check_acceptance( Sender, Item ).                      // check if an acceptance has came

-!kqml_received( Sender, cfp, Content, MsgId )                  // failure of plan (e.g. when no available)
	:   Content = retrieve( item( Item )[ [] | Positions ] )
	.//<-  .send( Sender, refuse, retrieve( ItemId ) ).            // refuse to retrieve item

@propose_retrieve[atomic]
+!propose_retrieve( Sender, ItemId )
	:   state( available )                                      // if i'm not doing other unstoppable things
	<-  -+state( pending );                                     // update state to wait confirm
		.send( Sender, propose, retrieve( ItemId ) ).           // propose to accept the work

@check_accepted[atomic]
+!check_acceptance( Sender, Item )                              // the propose has been accepted
	:   state( unavailable ) & activity( picking ).             // do nothing here

@check_unaccepted[atomic]
+!check_acceptance( Sender, Item )                              // the propose hasn't been accepted
	:   state( pending )                                        // if i got no response ...
	<-  .println( "timeout" );
        -+state( available )                                    // ... stop waiting and reset as available
        .send( Sender, failure, retrieve( Item )
                [ cause( request_timeout ) ] ).                 // send timeout failure

@client_accept[atomic]
+!kqml_received( Sender, accept_proposal, Content, MsgId )      // receive confirm of item picking
	:   Content = retrieve( Item )
	&   state( pending )
    <-  -+state( unavailable );
        -+activity( picking )[ client( Sender ), item( Item ) ].// pick item for client

@client_reject[atomic]
+!kqml_received( Sender, reject_proposal, Content, MsgId )      // clients refuse proposal
	:   Content = retrieve( Item )
	&   state( pending )
    <-  -+state( available ).                                   // become available again