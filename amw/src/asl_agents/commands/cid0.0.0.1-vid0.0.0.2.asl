requirements: [ "req1", "req3", "req4" ]

+!main                                                          // main plan
	<-  .println( 'Executing script version 2...' );
		.wait( 500 );
		!b.

+!b
	<-  .println( 'Script executed' ).  s(5). +!c <- .println( 'wow' ).