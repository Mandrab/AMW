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
		+activity( default );                                       // setup default activity
        +state( available );                                        // set as available to achieve unordinary operations
		!work.                                                      // start working


///////////////////////////////////////////////////////// JOBS /////////////////////////////////////////////////////////

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

///////////////////////////// CUSTOM JOB ( COMMAND )

+!work
	:   activity( executing )[ client( Client ), script( Script ) ] // only if picking
	<-  !main[ source( script ) ];                                  // run the task
		-+activity( default );                                      // setup default activity
        -+state( available );                                       // set as available to achieve unordinary operations
        !work.                                                      // restart to work

+!work : activity( _ ).

///////////////////////////////////////////////////// ITEM PICKING /////////////////////////////////////////////////////

///////////////////////////// PICKING REQUEST:      OP #15 in order submission schema

+!kqml_received( Sender, cfp, Content, MsgId )                      // request of item picking
	:   Content = retrieve( id( Id ), item( item( Item )[ [] | Positions ] ) )
    <-  !propose( Sender, retrieve( Id ), MsgId );                  // propose to retrieve the item
        .wait( 5000 );                                              // max time to wait for confirm
        !check_acceptance( Sender, retrieve( Id )
                [ cause( request_timeout ) ], MsgId ).              // check if an acceptance has came

-!kqml_received( Sender, cfp, Content, MsgId )                      // failure of plan (e.g. when no available)
	:   Content = retrieve( id( Id ), item( item( Item )[ [] | Positions ] ) )
	<-  .send( Sender, refuse, retrieve( Id ) ).                    // refuse to retrieve item

///////////////////////////// REFUSED PROPOSAL:     OP #21 in order submission schema

@client_reject[atomic]
+!kqml_received( Sender, reject_proposal, Content, MsgId )          // clients refuse proposal
	:   Content = retrieve( Item )
	&   state( pending )
    <-  -+state( available ).                                       // become available again

///////////////////////////// ACCEPTED PROPOSAL:    OP #24 in order submission schema

@client_accept[atomic]
+!kqml_received( Sender, accept_proposal, Content, MsgId )          // receive confirm of item picking
	:   Content = retrieve( Item )
	&   state( pending )
    <-  -+state( unavailable );                                     // set as unavailable for tasks
        -activity( _ );
        +activity( picking )[ client( Sender ), item( Item ) ].     // pick item for client

////////////////////////////////////////////////// SCRIPT EXECUTION //////////////////////////////////////////////////

///////////////////////////// JOB EXECUTION REQ:    OP #5 in command submission schema

+!kqml_received( Sender, cfp, Content, MsgId )                      // request of job execution
	:   Content = execute( script( S )[ [] | Requirements ] )
    <-  !feasible( Requirements, Result );
        if ( Result ) {                                             // i'm able to run the script
            +script( S )[ client( Sender ), msg_id( MsgId ) ];
            .println("feasible");!propose( Sender, execute( script( S ) ), MsgId );      // propose to exec the job
            .wait( 5000 );                                          // max time to wait for confirm
            !check_acceptance( Sender, execute( script )[ cause( request_timeout ) ], MsgId,
                    script( S ) );                                  // check the proposal acceptance
        } else { .println("not feasible");.send( Sender, refuse,
                execute( script )[ err_code( 407 ) ], MsgId ); }.   // refuse to run the script

+!feasible( [ Head | [] ], Result ) <- Result = true.   // TODO check fasibility

+!feasible( [ Head | Tail ], Result )
	<-  !feasible( [ Head ], HeadRes );
		!feasible( Tail, TailRes );
		.eval( Result, HeadRes & TailRes ).

-!feasible( [ H | T ], Result ) <- Result = false.

///////////////////////////// ACCEPTED PROPOSAL

@accept_execution_prop[atomic]
+!kqml_received( Sender, accept_proposal, Content, MsgId )          // receive confirm of item picking
	:   Content = execute( script )
	&   script( S )[ client( Sender ), msg_id( MsgId ) ]
	&   state( pending )
    <-  -+state( unavailable );                                     // set as unavailable for tasks
        -activity( _ );
        .term2string( Script, S );
		.add_plan( Script, script, begin );
        +activity( executing )[ client( Sender ), script( S ) ].    // pick item for client

/*
////////////////////////////////////////////////// COMMANDS EXECUTION //////////////////////////////////////////////////

///////////////////////////// JOB EXECUTION REQ:    OP #5 in command submission schema

+!kqml_received( Sender, cfp, Content, MsgId )                      // request of job execution
	:   Content = execute( job( JID )[ [] | Versions ] )
    <-  !get_feasible( Versions, Feasible )                         // try to get an executable version
        if ( .empty( Feasible ) )
            { .println("not feasible");.send( Sender, refuse,
                    execute( job( id ) )[ err_code( 407 ) ], MsgId ); }    // refuse the proposal for the job
	    else {
	        .println( Feasible );
	        !propose( Sender, execute( job( JID ), version( Feasible ) ), MsgId );       // propose to exec the job
	        .wait( 5000 );                                          // max time to wait for confirm
	        !check_acceptance( Sender, execute( script )
	                [ cause( request_timeout ) ], MsgId );          // check the proposal acceptance
        }.                                                          // no version of the script is executable

+!get_feasible( [ Head | [] ], Variant )
	:   Head = variant( id( ID ), requirements[ [] | Req ], script( S ) )
	<-  Variant = Head.   // TODO

+!get_feasible( [ Head | Tail ], Variant )
	<-  !get_feasible( [ Head ], Variant );
		if ( .empty( Variant ) )
			{ !get_feasible( [ Tail ], Variant ); }.

///////////////////////////// ACCEPTED PROPOSAL

@accept_execution_prop[atomic]
+!kqml_received( Sender, accept_proposal, Content, MsgId )          // receive confirm of item picking
	:   Content = execute( job( ID ) )
	&   state( pending )
    <-  -+state( unavailable );                                     // set as unavailable for tasks
        -activity( _ );
        +activity( executing )[ client( Sender ), job( ID ) ].     // pick item for client
*/
/////////////////////////////////////////////////////// GENERALS ///////////////////////////////////////////////////////

///////////////////////////// PROPOSE FOR TASK

@propose[atomic]
+!propose( Sender, Msg, MsgId )                                     // propose for task (item picking or job execution)
	:   state( available )                                          // if i'm not doing other unstoppable things
	<-  -+state( pending );                                         // update state to wait confirm
		.send( Sender, propose, Msg, MsgId ).                       // propose to accept the work

///////////////////////////// CHECK PROPOSAL RESPONSE

@accepted[atomic]
+!check_acceptance( Sender, Id, MsgId )                             // the propose has been accepted
	:   not state( pending ).                                       // do nothing here

@unaccepted[atomic]
+!check_acceptance( Sender, Msg, MsgId )                            // the propose hasn't been accepted
	:   state( pending )                                            // if i got no response ...
	<-  -+state( available );                                       // ... stop waiting and reset as available
        .send( Sender, failure, Msg, MsgId ).                       // send timeout failure

@accepted_with_delete[atomic]
+!check_acceptance( Sender, Id, MsgId, Annot )                      // the propose has been accepted
	:   not state( pending ).                                       // do nothing here

@unaccepted_with_delete[atomic]
+!check_acceptance( Sender, Msg, MsgId, Annot )                     // the propose hasn't been accepted
	:   state( pending )                                            // if i got no response ...
	<-  -+state( available );                                       // ... stop waiting and reset as available
		-Annot;                                                     // delete annotation
        .send( Sender, failure, Msg, MsgId ).                       // send timeout failure