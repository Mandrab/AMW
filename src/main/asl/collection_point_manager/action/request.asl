///////////////////////////////////////////// COLLECTION POINT RESERVATION /////////////////////////////////////////////

+!kqml_received(Sender, achieve, point(OID)[mid(MID)], _)
	<-  .println("[COLLECTION POINT MANAGER] collection point request");
	    !reserve(OID, PID, X, Y);
	    !cached_response(
            Sender,
            in(achieve, point(OID)[mid(MID)]),
            out(confirm, point(pid(PID), x(X), y(Y))[mid(MID)])
        ).                                                          // cache the response and send it

///////////////////////////// POINT SET FREE

@free_point[atomic]
+!kqml_received(Sender, tell, free(OID)[mid(MID)], _)
    <-  .println("[COLLECTION POINT MANAGER] collection point free");
        !free(OID);
        !cached_response(
            Sender,
            in(tell, free(OID)[mid(MID)]),
            out(confirm, free(OID)[mid(MID)])
        ).                                                          // cache the response and send it

//////////////////////////////////////////////////// UTILITY PLANS /////////////////////////////////////////////////////

@reservationAlreadyPresent[atomic]
+!reserve(OID, PID, X, Y)
    :   point(PID)[x(X), y(Y), reservedFor(OID)].                   // a point is already assigned to this order

@reservation[atomic]
+!reserve(OID, PID, X, Y)
    :   not point(_)[_, _, reservedFor(OID)]                        // no point is already assigned to this order
    &   point(PID)[x(X), y(Y)]
    &   not point(PID)[x(X), y(Y), reservedFor(_)]                  // available collection point exists
	<-  -point(PID);
	    +point(PID)[x(X), y(Y), reservedFor(OID)].                  // reserve point for given order

@free[atomic]
+!free(OrderID)
    :   point(PointID)[x(X), y(Y), reservedFor(OrderID)]
    <-  -point(PointID);
        +point(PointID)[x(X), y(Y), state(available)].
