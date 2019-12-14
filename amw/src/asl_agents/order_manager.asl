
/* Initial beliefs and rules */

set( false ).

/* Initial goals */

!setup.

/* Plans */

+!setup : set( C ) & not C <-
	.df_register( "management( orders )", "accept( order )" ); // register service as order acceptor
	-+set( true );
	.println("Order Manager set up").                          // set process ended
/*
+!kqml_received( Sender, cfp, Content, MsgId )
    : .literal( Content ) & Content( Requirer, Address )[ Items ]
    <-  !accomplish_order( Content );
        .send( Sender, tell, "Order taken over", MsgId ).

+!kqml_received( Sender, cfp, Content, MsgId )
    : not ( .literal( Content ) & Content( Requirer, Address )[ Items ] )
    <-  .println( "Wrong submission" );
        .send( Sender, tell, "Wrong submission", MsgId ).

-!kqml_received( Sender, cfp, Content, MsgId ) <- .send( Sender, tell, "No such action", MsgId ).
/*
+!kqml_received( Sender, cfp, Content, MsgId ) <-
    new_input( true )[ L ];
    .concat( L, Content, L );
    -+new_input( true )[ L ];
    .println( L );
    .send( Sender, propose, Content, MsgId );
    .println("send procedure started").*/

-!kqml_received <- .println("kqml failed").
