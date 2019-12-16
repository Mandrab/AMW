/* Initial beliefs and rules */

set( false ).                                                       // at start is not yet set

item( "Item 1" )[ rack(5), shelf(3) ].                              // initial known items
item( "Item 2" )[ rack(5), shelf(3) ].



/* Initial goals */

!setup.                                                             // setup



/* Plans */

+!setup
	:   set( false )
	<-  .df_register( "management( items )", "store( item )" );     // register for acquire information about items
		.df_register( "management( items )", "find( item )" );      // register for supply item's position infos
		.df_register( "management( items )", "retrieve( item )" );  // register for remove infos at item removal
		-+set( true );
		.println("Warehouse Mapper set up").                        // set process ended



+!kqml_received( Sender, askOne, Content, MsgId )                   // retrieve position of an item
    :   not .list( Content )
    <-  !find( [ Content ], Result );
        .send( Sender, tell, Result, MsgId ).



+!kqml_received( Sender, askOne, Content, MsgId )                   // retrieve position of a list of items
    :   .list( Content )
    <-  !find( Content, Result );
		.send( Sender, tell, Result, MsgId ).



+!find( [ Head, Tail ], Result )                                    // Search for the elements position in the warehouse
	:   item( LastElem )
	<-  !find( Tail, Res );
		?item( LastElem )[ rack( RackN ), shelf( ShelfN ) ];
		Result = [ item( Head )[ rack( RackN ), shelf( ShelfN ) ] | Res ].



+!find( LastElem, Result )                                          // Search position of the last element in the list
	:   item( LastElem )
	<-  ?item( LastElem )[ rack( RackN ), shelf( ShelfN ) ]
		Result = [ item( LastElem )[ rack( RackN ), shelf( ShelfN ) ] ].