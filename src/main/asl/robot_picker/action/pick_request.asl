///////////////////////////////////////////////////// ITEM PICKING /////////////////////////////////////////////////////

+!kqml_received(Sender, tell, retrieve(P, PID), OID)                // request of item picking
    :   pick(_)                                                     // already picking or ...
    |   execute(_)                                                  // already executing something
    <-  .println("[ROBOT PICKER] item retrieval not available");
        .send(Sender, failure, retrieve(P, PID), OID).              // fail the required task

+!kqml_received(Sender, tell, retrieve(P, PID), MID)                // request of item picking
    <-  .println("[ROBOT PICKER] item retrieval");
        +pick(P, PID)[client(Sender), mid(MID)];                    // generate the event for picking
        !cached_response(
            Sender,
            in(tell, retrieve(P, PID), MID),
            out(confirm, retrieve(P, PID), MID)                     // accept request
        ).                                                          // cache the response and send it

//////////////////////////////////////////////////// UTILITY PLANS /////////////////////////////////////////////////////

+pick(Item, PID)[client(Client), mid(MID)]
	<-  .wait(500);                                                 // fake execution time
        -pick(Item, PID)                                            // task completed
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