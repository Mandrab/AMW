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
	<-  .df_search( "management( items )", "retrieve( item )", Providers );
		.nth( 0, Providers, Provider );
        .send( Providers, cfp, retrieve("items") ).

+!kqml_received( Sender, propose, Content, MsgId )
	:   Content == ack("positions")
	<-  .df_search( "executor( item_picker )", "retrieve( item )", Providers );
    	.nth( 0, Providers, Provider );
    	.send( Provider, cfp, retrieve( itemX ) ).

+!kqml_received( Sender, tell, Content, MsgId )
	:   Content == error("no items")
	<-  .println( Content );
		/* TODO resend error */.

+!kqml_received( Sender, accept, Content, MsgId )
	:   Content = retrieve( Item )
	<-  //.println( Content );
		.send( Sender, confirm, retrieve( Item ) ).

+!kqml_received( Sender, complete, Content, MsgId )
	:   Content = retrieve( Item )
	<-  .println( "Picking complete" );
		/* TODO */.
