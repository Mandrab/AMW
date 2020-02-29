requirements: [ "req1", "req2", "req3" ] +!main                                                          // main plan
	<-  .println( 'Executing script version 1...' );
		.wait( 500 );
		!b.

@l4[atomic]
+!b
	<-  .println( 'Script executed' ).  s(5). +!c <- .println( 'wow' ).