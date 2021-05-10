///////////////////////////////////////////////////// ITEM PICKING /////////////////////////////////////////////////////

+!kqml_received(Sender, tell, retrieve(P, PID), OID)                // request of item picking
    :   pick(_)                                                     // already picking or ...
    |   execute(_)                                                  // already executing something
    <-  .send(Sender, failure, retrieve(P, PID)).                   // fail the required task

+!kqml_received(Sender, tell, retrieve(P, PID), OID)                // request of item picking
    <-  +pick(P, PID)[client(Sender)];                              // generate the event for picking
        .send(Sender, confirm, retrieve(P, PID), MID).              // accept request

//////////////////////////////////////////////////// UTILITY PLANS /////////////////////////////////////////////////////

+pick(item(id(ID),item(Item)))[client(Client)]
	<-  .println("[ROBOT PICKER] picking items");
	    Item = item(id(IID))[H|T];                                  // TODO docu che in teoria potrebbe non esserci l'oggetto e nel caso dovrebbe cercare da qualche altra parte
	    !!remove(item(IID),_);                                      // TODO la posizione non è spacificata perchè non è implementata la ricerca di cui sopra
	    .wait(1000);                                                // fake execution time
		.send(Client,complete,retrieve(Item));                      // confirm task completion
        -+activity(default);                                        // setup default activity
        -+state(available).                                         // set as available to achieve unordinary operations
/*
+!remove(item(ID),position(rack(R),shelf(S),quantity(_)))
    <-  !random_agent("management(items)","remove(item)",Provider);
        .send(Provider,achieve,remove_reserved(item(id(ID),position(rack(R),shelf(S),quantity(1)))));
        ? not error(remove(ID)).

-!remove(item(ID),position(rack(R),shelf(S),quantity(_))) <- !remove(item(ID),position(rack(R),shelf(S),quantity(1))).
*/