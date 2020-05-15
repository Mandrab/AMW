requirements: [ "requirement_1", "requirement_2", "requirement_3" ]

+!main                                                                          // main plan
	<-  !start_execution;                                                       // start execution
		!!parallel_plan;                                                        // run a parallel plan
		.wait( 500 );                                                           // fake execution time
		!end_plan.                                                              // stop execution

+!start_execution
	<-  +run[ source( script ) ];                                               // start running
		.println( 'Executing script version 1 ...' ).

+!parallel_plan
	:   run[ source( script ) ]                                                 // if allow to run
	<-  .println( 'Script 1 is running ...' );                                  // doing ...
		.wait( 101 );                                                           // ...stuffs
		!parallel_plan.                                                         // restart

+!end_plan
	<-  -run[ source( script ) ];                                               // stop running
		.println( 'Script 1 executed' ).