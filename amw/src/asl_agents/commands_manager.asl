/***********************************************************************************************************************
 Initial beliefs and rules
***********************************************************************************************************************/

set( false ).                                                       // at start is not yet set
/*
command( id( "Command1" ), name( "command 1 name" ), description( "descr command 1" ) ) [
		variant( v_id( "v0.0.1" ), requirements[ "req1", "req2", "req3" ], script( "" ) ),
		variant( v_id( "v0.0.2" ), requirements[ "req1", "req3" ], script(
				"[  {+!main <- .println( 'Executing script ...' );.wait( 5000 ); !b}, {+!b <- .println( 'Script executed' ) }]" ) ) ].
*/
/***********************************************************************************************************************
 Initial goals
***********************************************************************************************************************/

!setup.                                                             // setup

/***********************************************************************************************************************
 Plans
***********************************************************************************************************************/

+!setup
	:   set( false )
	<-  .df_register( "management( commands )", "add( command )" ); // register for commands adder
		.df_register( "management( commands )", "request( command )" ); // register for commands dispatcher
		.df_register( "management( commands )", "info( commands )" );   // register for commands store
		asl_actions.load_commands( Commands );
		!add( Commands );
		.include( "utils/literal.asl" );
		-+set( true ).                                              // set process ended

+!add( [ Command | [] ] )
	<-  +Command.

+!add( [ Command | Commands ] )
	<-  +Command; !add( Commands ).

+!kqml_received( Sender, cfp, add( Command ), MsgId )
	:   command( id( CID ), name( CName ), description( CDescr ) )[ Versions ]
	<-  +Command.

+!kqml_received( Sender, cfp, info( commands ), MsgId )                      // send the warehouse state (items info & position)
	<-  .findall( command( id( Id ), name( Name ), description( Descr ) )
                [ variant( v_id( VersionID ), requirements[ RH | RT ], script( Script ) ) ],
                command( id( Id ), name( Name ), description( Descr ) )
                [ variant( v_id( VersionID ), requirements[ RH | RT ], script( Script ) ) ], L );
        .send( Sender, propose, L, MsgId ).

+!kqml_received( Sender, achieve, Content, MsgId )                  // send the warehouse state (items info & position)
	:   Content = request( command_id( CommandId ) )
    &   command( id( CommandId ), name( _ ), description( _ ) )[ source( self ) | Variants ]
    <-  !concat( command( CommandId ), Variants, Msg );
        .send( Sender, tell, Msg, MsgId ).                          // ask if able to run the specified script