/***********************************************************************************************************************
 Initial beliefs and rules
 **********************************************************************************************************************/

set( false ).                                                       // at start is not yet set

command( id( "Command1" ), name( "command 1 name" ), description( "descr command 1" ) )
	[ variant( requirements( [] ), script( "TODO" ) ) ].

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
                  	[ variant( requirements( Req ), script( Script ) ) ],
                    command( id( Id ), name( Name ), description( Descr ) )
                    [ variant( requirements( Req ), script( Script ) ) ], L);
        .send( Sender, propose, L, MsgId ).