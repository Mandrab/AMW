/////////////////////////////////////////////////// SCRIPT EXECUTION ///////////////////////////////////////////////////

+execute(script)[client(Client)]
	<-  !main[source(script)];                                      // run the main intention of the script
		!remove_plans(0);                                           // remove all plans with label in the form of "lN"
		-+activity(default);                                        // at end, setup default activity
        -+state(available).                                         // set as available to achieve unordinary operations
