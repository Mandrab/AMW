///////////////////////////////////////////// COLLECTION POINT RESERVATION /////////////////////////////////////////////

+!kqml_received(Sender, achieve, point(ID)[mid(MID)], _)            // request for a collection point
	<-  .println("[COLLECTION POINT MANAGER] collection point request");
	    !reserve(ID, PID, X, Y);                                    // reserve point
	    !cached_response(
            Sender,
            in(achieve, point(ID)[mid(MID)]),
            out(confirm, point(ID, pid(PID), x(X), y(Y))[mid(MID)])
        ).                                                          // cache the response and send it

///////////////////////////// POINT SET FREE

+!kqml_received(Sender, tell, free(ID)[mid(MID)], _)                // free of collection point
    <-  .println("[COLLECTION POINT MANAGER] collection point free");
        !free(ID);                                                  // free cp assigned to this order/id
        !cached_response(
            Sender,
            in(tell, free(ID)[mid(MID)]),
            out(confirm, free(ID)[mid(MID)])
        ).                                                          // cache the response and send it

//////////////////////////////////////////////////// UTILITY PLANS /////////////////////////////////////////////////////

@reservationAlreadyPresent[atomic]
+!reserve(ID, PID, X, Y): point(PID)[x(X), y(Y), reservedFor(ID)].  // a point is already assigned to this order

@reservation[atomic]
+!reserve(ID, PID, X, Y)
    :   not point(_)[_, _, reservedFor(ID)]                         // no point is already assigned to this order
    &   point(PID)[x(X), y(Y)]
    &   not point(PID)[x(X), y(Y), reservedFor(_)]                  // available collection point exists
	<-  -point(PID);
	    +point(PID)[x(X), y(Y), reservedFor(ID)].                   // reserve point for given order

@free[atomic]
+!free(ID)
    :   point(PID)[x(X), y(Y), reservedFor(ID)]                     // point is assigned to this order
    <-  -point(PID);
        +point(PID)[x(X), y(Y), state(available)].                  // free the point
