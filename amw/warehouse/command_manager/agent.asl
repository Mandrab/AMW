/***********************************************************************************************************************
 Initial beliefs and rules
 **********************************************************************************************************************/

set( false ).                                                       // at start is not yet set

command( id( "Command1" ), name( "command 1 name" ), description( "descr command 1" ) )
	[ variant( v_id( "v0.0.1" ), requirements[ "req1", "req2", "req3" ], script( "TODO" ) ) ].

/***********************************************************************************************************************
 Initial goals
 **********************************************************************************************************************/

!setup.                                                             // setup

/***********************************************************************************************************************
 Plans
 **********************************************************************************************************************/

+!setup
	:   set( false )
	<-  .df_register( "management( commands )", "add( command )" ); // register for commands adder
		.df_register( "management( commands )", "request( command )" ); // register for commands dispatcher
		.df_register( "management( commands )", "info( commands )" );   // register for commands store
		-+set( true ).                                              // set process ended

+!kqml_received( Sender, cfp, Content, MsgId )                      // send the warehouse state (items info & position)
	:   Content = info( commands )
    <-  .findall( command( id( Id ), name( Name ), description( Descr ) )
                [ variant( v_id( VersionID ), requirements[ RH | RT ], script( Script ) ) ],
                command( id( Id ), name( Name ), description( Descr ) )
                [ variant( v_id( VersionID ), requirements[ RH | RT ], script( Script ) ) ], L );
        .send( Sender, propose, L, MsgId ).

+!kqml_received( Sender, cfp, Content, MsgId )                      // send the warehouse state (items info & position)
	:   Content = request( command_id( CommandId ), version_id( VersionId ) )
    <-  .term2string( CID, CommandId );
        .term2string( VID, VersionId );
        ?command( id( CID ), name( N ), description( D ) )
                [ variant( v_id( VID ), requirements[ RH | RT ], script( S ) ) ];
        .df_search( "executor( command )", "exec( command )",
                Providers );                                        // search for agents able to exec commands
        .nth( 0, Providers, Provider );                             // get the first agent
        .send( Provider, cfp, command[ RH | RT ] ).                 // ask if able to run the specified script