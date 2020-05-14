requirements: [ "cid0.0.0.2" ]

+!main                                                                          // main plan
	<-  !setup[ source( script ) ];                                             // start execution
		!exec_task[ source( script ) ].                                         // exec the task

+!setup
	<-  +run[ source( script ) ];                                               // start running
		.println( "Executing script version 2 ..." ).

+!exec_task
	:   run[ source( script ) ]                                                 // if allow to run
	<-  .random( N );
		if ( N > 0.75 ) {
			!end_task[ source( script ) ];
		} else {
			.println( "Script 2 is running ..." );                              // doing ...
			.wait( 101 );                                                       // ... stuffs
			!exec_task;                                                         // restart
		}.

+!end_task
	<-  -run[ source( script ) ];                                               // stop running
		.println( 'Script 2 executed' ).