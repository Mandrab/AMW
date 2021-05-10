///////////////////////////////////////////////////// ITEM PICKING /////////////////////////////////////////////////////

+!kqml_received(Sender, tell, retrieve(P, PID), MID)                // request of item picking
    <-  .println("[ROBOT PICKER] request for item retrieval");
        !set_pick(P, PID, Sender, MID);
        !cached_response(
            Sender,
            in(tell, retrieve(P, PID), MID),
            out(confirm, retrieve(P, PID), MID)                     // accept request
        );                                                          // cache the response and send it
        !pick.                                                      // start picking

//////////////////////////////////////////////////// UTILITY PLANS /////////////////////////////////////////////////////

@setTask[atomic]
+!set_pick(P, PID, Sender, MID)
    :   not pick(_, _)                                              // not already picking and ...
    &   not execute(_, _)                                           // not already executing something
    <-  +pick(P, PID)[client(Sender), mid(MID)].                    // set pick target data

+!pick
    :   pick(Item, PID)[client(Client), mid(MID)]
	<-  .wait(250);                                                 // fake execution time
	    -pick(Item, PID);                                           // task completed
		!ensure_send(
		    Client,
		    confirm, retrieve(Item, PID),                           // confirm task completion
		    MID, unique
        ).                                                          // msgid is unique from order_manager
        // TODO HE IS WAITING THE CONFIRMATION MESSAGE FROM ORDER MANAGER!!!
/*
+!remove(item(ID),position(rack(R),shelf(S),quantity(_)))
    <-  !random_agent("management(items)","remove(item)",Provider);
        .send(Provider,achieve,remove_reserved(item(id(ID),position(rack(R),shelf(S),quantity(1)))));
        ? not error(remove(ID)).

-!remove(item(ID),position(rack(R),shelf(S),quantity(_))) <- !remove(item(ID),position(rack(R),shelf(S),quantity(1))).
*/