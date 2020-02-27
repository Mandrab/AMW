/***********************************************************************************************************************
 Initial beliefs and rules
***********************************************************************************************************************/

set( false ).                                                       // at start is not yet set
implemented_plans_id[ "req1", "req3", "req4" ].

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
		.include( "utils/communication.asl" );                      // include communication utils plans
		+activity( default );                                       // setup default activity
        +state( available );                                        // set as available to achieve unordinary operations
        -+set( true );                                              // set process ended
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

///////////////////////////// SCRIPT EXECUTION

+!work
	:   activity( executing )[ client( Client ), script( Script ) ] // only if executing script
	<-  !main[ source( script ) ];                                  // run the main intention of the script
		-+activity( default );                                      // at end, setup default activity
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
+!kqml_received( Sender, reject_proposal, retrieve( Item ), MsgId ) // clients refuse proposal
	:   state( pending )
    <-  -+state( available ).                                       // become available again

///////////////////////////// ACCEPTED PROPOSAL:    OP #24 in order submission schema

@client_accept[atomic]
+!kqml_received( Sender, accept_proposal, retrieve( Item ), MsgId ) // receive confirm of item picking
	:   state( pending )
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
            !propose( Sender, execute( script( S ) ), MsgId );      // propose to exec the job
            .wait( 5000 );                                          // max time to wait for confirm
            !check_acceptance( Sender, execute( script )[ cause( request_timeout ) ], MsgId,
                    script( S ) );                                  // check the proposal acceptance
        } else { .send( Sender, refuse,
                execute( script )[ err_code( 407 ) ], MsgId ); }.   // refuse to run the script

+!feasible( [ Head | [] ], Result ) <- Result = true.   // TODO check fasibility

+!feasible( [ Head | Tail ], Result )
	<-  !feasible( [ Head ], HeadRes );
		!feasible( Tail, TailRes );
		.eval( Result, HeadRes & TailRes ).

-!feasible( [ H | T ], Result ) <- Result = false.

///////////////////////////// ACCEPTED PROPOSAL

@accept_execution_prop[atomic]
+!kqml_received( Sender, accept_proposal, execute( script ), MsgId )          // receive confirm of item picking
	:   script( S )[ client( Sender ), msg_id( MsgId ) ]
	&   state( pending )
    <-  -+state( unavailable );                                     // set as unavailable for tasks
        -activity( _ );
        .term2string( Script, S );
		.add_plan( Script, script, begin );
        +activity( executing )[ client( Sender ), script( S ) ].    // pick item for client



////////////////////////////////////////////////// COMMANDS EXECUTION //////////////////////////////////////////////////

///////////////////////////// COMMAND EXECUTION REQ:OP #5 in command submission schema

+!kqml_received( Sender, cfp, Content, MsgId )                      // request of job execution
	:   Content = execute( command_id( CommandID ) )
    <-  .println(ciao);
        !status_if( state( available ), pending, Result );
		if ( Result ) {
			+execution_request( command( CommandID ), client( Sender ), msg_id( MsgId ) )[ status( unaccepted ) ];
			!random_agent( "management( commands )", "request( command )", Agent );
            .send( Agent, achieve, request( command_id( CommandID ) ) );// require command's versions
            .wait( 5000 );                                              // max time to wait for confirm
            !command_request_timeout( CommandID, Sender, MsgId );
		} else
			{ .send( Sender, refuse, execution( command_id( CommandID ) )[ err_code( 503 ) ] ); }.

@command_request_timeout[atomic]
+!command_request_timeout( CommandID, Client, MsgId )
	:   execution_request( command( CommandID ), client( Client ), msg_id( MsgId ) )[ status( unaccepted ) ]
    <-  -execution_request( command( CommandID ), client( Client ), msg_id( MsgId ) );
        -+state( available );
        .send( Client, refuse, execute( command_id( CommandID ) )[ err_code( 404 ) ], MsgId ).

///////////////////////////// GET COMMAND INFOS

@get_command[atomic]
+!kqml_received( Sender, tell, Content, MsgId )          // receive confirm of item picking
	:   Content = command( CID )[ [] | Variants ] & .term2string( CommandID, CID )
	&   execution_request( command( CommandID ), client( Client ), msg_id( ReqMsgId ) )[ status( unaccepted ) ]
	<-  -execution_request( command( CommandID ), client( Client ), msg_id( ReqMsgId ) )[ status( unaccepted ) ];
		!get_feasible( Variants, Feasible );
		+execution_request( command( CommandID ), client( Client ), msg_id( ReqMsgId ) )[ status( accepted ), version( Feasible ) ].

@execution_request_no_version[atomic]
+execution_request( command( CID ), client( Client ), msg_id( MsgId ) )[ status( accepted ), version( Feasible ) ]
	:   not .ground( Feasible )
	<-  .println("no feas");
		-execution_request( command( CID ), client( Client ), msg_id( MsgId ) );
		-+state( available );
		.send( Client, refuse, execute( command_id( CommandID ) )[ err_code( 404 ) ], MsgId ).

+execution_request( command( CID ), client( Client ), msg_id( MsgId ) )[ status( accepted ), version( Feasible ) ]
	<-  .send( Client, propose, execute( command_id( CID ) ), MsgId );    // propose to exec the job
        .wait( 5000 );                                      // max time to wait for confirm
        !check_acceptance( Client, execute( command_id( CID ) )[ cause( request_timeout ) ], MsgId,
                execution_request( command( CID ), client( Client ), msg_id( MsgId ) )[ status( accepted ), version( Feasible ) ] ).             // check the proposal acceptance

+!get_feasible( [ Head | [] ], Feasible )
	:   Head = variant( v_id( ID ), requirements[ [] | Requirements ], script( Script ) )
	&   implemented_plans_id[ source( self ) | Labels ]
	<-  !provided( Requirements, Labels, Provided );
		if ( Provided ) { Feasible = Script; }.

+!get_feasible( [ Head | Tail ], Feasible )
	<-  !get_feasible( [ Head ], Feasible );
		if ( not .ground( Feasible ) ) { !get_feasible( Tail, Feasible ); }.

+!provided( [ Req | [] ], [ Label | [] ], true ) : Req == Label.

+!provided( [ Req | [] ], [ Label | [] ], false ).

+!provided( [ Req | [] ], [ Label | Tail ], Result )
	<-  !provided( [ Req ], [ Label ], HeadRes );
		!provided( [ Req ], Tail, TailRes );
		.eval( Result, HeadRes | TailRes ).

+!provided( [ Req | RTail ], Labels, Result )
	<-  !provided( [ Req ], Labels, HeadRes );
		!provided( RTail, Labels, TailRes );
		.eval( Result, HeadRes & TailRes ).

///////////////////////////// ACCEPTED PROPOSAL

@accept_execution_propTODO2[atomic]
+!kqml_received( Sender, accept_proposal, Content, MsgId )          // receive confirm of item picking
	:   Content = execute( command_id( ID ) )
	&   state( pending )
    <-  .println(Content);-+state( unavailable );                                     // set as unavailable for tasks
        -activity( _ );
        +activity( executing )[ client( Sender ), job( ID ) ].     // pick item for client

/////////////////////////////////////////////////////// GENERALS ///////////////////////////////////////////////////////

///////////////////////////// STATUS

@status_if[atomic]
+!status_if( Cond, Status, true )
	:   Cond
	<-  -+state( Status ).

@status_if_fail[atomic]
-!status_if( Previous, Status, false ).

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