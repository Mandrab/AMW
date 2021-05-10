///////////////////////////////////////////// COLLECTION POINT RESERVATION /////////////////////////////////////////////

+!kqml_received(Sender, tell, point, OID)
	<-  .println("[COLLECTION POINT MANAGER] collection point request");
	    !reserve(OID, PID, X, Y);
	    !cached_response(
            Sender,
            in(tell, point, OID),
            out(confirm, point(pid(PID), x(X), y(Y)), OID)
        ).                                                          // cache the response and send it

///////////////////////////// POINT SET FREE

@free_point[atomic]
+!kqml_received(Sender, tell, free, OID)
    <-  !free(OID);
        !cached_response(
            Sender,
            in(tell, free, OID),
            out(confirm, free, OID)
        ).                                                          // cache the response and send it

//////////////////////////////////////////////////// UTILITY PLANS /////////////////////////////////////////////////////

@reservation[atomic]
+!reserve(OID, PID, X, Y)
    :   not point(_)[_, _, reservedFor(OID)]                        // no point is already assigned to this order
    &   point(PID)[x(X), y(Y)]
    &   not point(PID)[x(X), y(Y), reservedFor(_)]                  // available collection point exists
	<-  .println(aaa);-point(PID);
	    +point(PID)[x(X), y(Y), reservedFor(OID)].                  // reserve point for given order

@free[atomic]
+!free(OrderID)
    :   point(PointID)[x(X), y(Y), reservedFor(OrderID)]
    <-  -point(PointID);
        +point(PointID)[x(X), y(Y), state(available)].

@accepted_proposal[atomic]
+!check_accepted(_,OrderID,_)
    :   point(PointID)[state(reserved),by(OrderID)].

@timeout_proposal[atomic]
+!check_accepted(Sender, OrderID, MsgID)
    <-  !free(OrderID);
        .send(Sender, failure, point(OrderID), MsgID).
