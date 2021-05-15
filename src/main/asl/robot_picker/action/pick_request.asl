///////////////////////////////////////////////////// ITEM PICKING /////////////////////////////////////////////////////

// TODO implement comfirmation (with caching) in order manager
+!kqml_received(Sender, confirm, retrieve(P, PID)[mid(MID)], _)     // confirmation of message reception
    <-  !response_received(MID).

+!kqml_received(Sender, achieve, retrieve(P, PID)[mid(MID)], _)     // request of item picking
    <-  .println("[ROBOT PICKER] request for item retrieval");
        !set_pick(P, PID, Sender, MID);
        !pick.                                                      // start picking

//////////////////////////////////////////////////// UTILITY PLANS /////////////////////////////////////////////////////

@setPickTask[atomic]
+!set_pick(P, PID, Sender, MID)
    :   not pick(_, _)                                              // not already picking and ...
    &   not execute(_)                                              // not already executing something
    <-  +pick(P, PID)[client(Sender), mid(MID)].                    // set pick target data

+!pick
    :   pick(Item, PID)[client(Client), mid(MID)]
	<-  .wait(500);                                                 // fake execution time
	    -pick(Item, PID);                                           // task completed
		!ensure_send(
		    Client,
		    confirm, retrieve(Item, PID),                           // confirm task completion
		    MID
        ).                                                          // msgid is unique from order_manager
