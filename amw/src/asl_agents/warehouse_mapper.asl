/* Initial beliefs and rules */

set( false ).                                                       // at start is not yet set

item( "Item 1" )[ rack(5), shelf(3), quantity(1) ].                 // initial known items
item( "Item 2" )[ rack(5), shelf(1), quantity(2) ].



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



+!kqml_received( Sender, askOne, [ Unique | [ Operation | Item ] ], MsgId ) // find item(s) position
    :   Operation == find
    <-  if ( .list( Item ) ) { !find( Item, Result ); }             // retrieve position of an item
        else { !find( [ Item ], Result ); }                         // retrieve position of a list of items
        .send( Sender, tell, [ Unique | Result ] ).                 // send the item(s) infos

-!kqml_received( Sender, askOne, [ Unique | [ Operation | Item ] ], MsgId )
    :   Operation == find
    <-  .send( Sender, tell,  ).


+!kqml_received( Sender, cfp, [ Operation | Item ], MsgId )         // store some item(s)
	:   Operation == store
	<-  if ( .list( Item ) ) { !store( Item, Position ); }          // store the element(s) and GET the position(s)
		else { !add( [ Item ], Position ); }
        .send( Sender, cfp, "Item(s) added", MsgId ).



+!find( [ Name | Tail ], Result )                                   // Search for the elements position in the warehouse TODO make atomic
	:   not .empty( Head )
	<-  if ( not .empty( Tail ) ) { !find( Tail, Res ); }           // if isn't the last element, recursively find the others
		else { Res = []; }
		?item( Name )[ rack( RackN ), shelf( ShelfN ), quantity( QuantityN ) ];
		Result = [ [ Name, RackN, ShelfN, QuantityN ] | Res ].



+!store( [ Head | Tail ], Position )
	<-  if ( not .empty( Tail ) ) { !find( Tail, Res ); }
		?item( Head )[ rack( RackN ), shelf( ShelfN ), quantity( ActQuantityN ) ];
		?Head( quantity( QuantityN ) );
		-+item( Head )[ rack( RackN ), shelf( ShelfN ), quantity( ActQuantityN + QuantityN ) ];
		Position = [ item( Head )[ rack( RackN ), shelf( ShelfN ) ] | Res ].