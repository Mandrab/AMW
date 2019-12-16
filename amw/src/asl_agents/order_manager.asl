/* Initial beliefs and rules */

set( false ).                                                       // at start is not yet set



/* Initial goals */

!setup.                                                             // setup



/* Plans */

+!setup
	:   set( false )
	<-  .df_register( "management( orders )", "accept( order )" );  // register service as order acceptor
		-+set( true );
		.println("Order Manager set up").                           // set process ended



+!kqml_received( Sender, cfp, Content, MsgId )
    :   .literal( Content )
    <-  asl_actions.order_specs( Content, Client, Address, Items ); // retrieve informations from the call content
        .df_search( "management( items )", "find( item )", Providers );
        if ( .empty( Providers ) ) {
            .send( Sender, tell, "Error: no existing provider", MsgId );
        }
        .nth( 0, Providers, Provider );
        .send( Providers, askOne, Items ).                          // ask the provider for the order's item position

+!kqml_received( Sender, tell, Content, MsgId )
	<-  .println( Content ).                                        // TODO print the position returned by provider
