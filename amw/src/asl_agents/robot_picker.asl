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

///////////////////////////// AGENT SETUP

+!setup
	:   set( false )
	<-  .df_register( "executor( item_picker )", "retrieve( item )" );  // register for pick items
		.df_register( "executor( command )", "exec( command )" );   // register for pick items
		-+set( true );                                              // set process ended
		+activity( default );                                        // setup default activity
        +state( available );                                        // set as available to achieve unordinary operations
		!work.                                                      // start working


///////////////////////////////////////////////////////// WORK /////////////////////////////////////////////////////////

///////////////////////////// DEFAULT JOB

+!work
	:   activity( default )                                         // only if achieving default activity
	<-  .println( "Doing stuffs ..." );
		.wait( 5000 );                                              // fake execution time
		!work.                                                      // restart to work

///////////////////////////// PICKING JOB

+!work
	:   activity( picking )[ client( Client ), item( Item ) ]       // only if picking
	<-  .println( "Picking ..." );
		.wait( 5000 );                                              // fake execution time
		.send( Client, complete, retrieve( Item ) );                // confirm task completion
        -+activity( default );                                      // setup default activity
        -+state( available );                                       // set as available to achieve unordinary operations
        !work.                                                      // restart to work

+!work : activity( _ ).

//////////////////////////////////////////////////// COMMUNICATION /////////////////////////////////////////////////////

///////////////////////////// PICKING REQUEST:      OP #15 in purchase sequence schema

+!kqml_received( Sender, cfp, Content, MsgId )                      // request of item picking
	:   Content = retrieve( id( Id ), item( item( Item )[ [] | Positions ] ) )
    <-  !propose_retrieve( Sender, Id );                            // propose to retrieve the item
        .wait( 5000 );                                              // max time to wait for confirm
        !check_acceptance( Sender, Id ).                            // check if an acceptance has came

-!kqml_received( Sender, cfp, Content, MsgId )                      // failure of plan (e.g. when no available)
	:   Content = retrieve( id( Id ), item( item( Item )[ [] | Positions ] ) )
	<-  .send( Sender, refuse, retrieve( Id ) ).                    // refuse to retrieve item

///////////////////////////// PROPOSE TO PICK:      OP #17 in purchase sequence schema

@propose_retrieve[atomic]
+!propose_retrieve( Sender, Id )                                    // propose to retrieve item
	:   state( available )                                          // if i'm not doing other unstoppable things
	<-  -+state( pending );                                         // update state to wait confirm
		.send( Sender, propose, retrieve( Id ) ).                   // propose to accept the work

///////////////////////////// NO RESPONSE:          OP #19 in purchase sequence schema

@check_accepted[atomic]
+!check_acceptance( Sender, Id )                                    // the propose has been accepted
	:   not state( pending ).                                       // do nothing here

@check_unaccepted[atomic]
+!check_acceptance( Sender, Id )                                    // the propose hasn't been accepted
	:   state( pending )                                            // if i got no response ...
	<-  -+state( available )                                        // ... stop waiting and reset as available
        .send( Sender, failure, retrieve( Id )
                [ cause( request_timeout ) ] ).                     // send timeout failure

///////////////////////////// REFUSED PROPOSAL:     OP #21 in purchase sequence schema

@client_reject[atomic]
+!kqml_received( Sender, reject_proposal, Content, MsgId )          // clients refuse proposal
	:   Content = retrieve( Item )
	&   state( pending )
    <-  -+state( available ).                                       // become available again

///////////////////////////// ACCEPTED PROPOSAL:    OP #24 in purchase sequence schema

@client_accept[atomic]
+!kqml_received( Sender, accept_proposal, Content, MsgId )          // receive confirm of item picking
	:   Content = retrieve( Item )
	&   state( pending )
    <-  -+state( unavailable );                                     // set as unavailable for tasks
        -activity( _ );
        +activity( picking )[ client( Sender ), item( Item ) ].     // pick item for client