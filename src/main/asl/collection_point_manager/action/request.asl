///////////////////////////////////////////// COLLECTION POINT RESERVATION /////////////////////////////////////////////

+!kqml_received(Sender, tell, point, OID)
	<-  .println("[COLLECTION POINT MANAGER] collection point request");
	    !reserve(OID, PID, X, Y);
	    +cache(OID, confirm, point(pid(PID), x(X), y(Y)));
	    .send(Sender, confirm, point(pid(PID), x(X), y(Y)), OID).

///////////////////////////// POINT SET FREE

@free_point[atomic]
+!kqml_received(Sender, tell, free, OID)
    <-  !free(OID);
        +cache(OID, confirm, order(C, E, A)[H|T]);
        .send(Sender, confirm, free(OrderID), OID).

//////////////////////////////////////////////////// UTILITY PLANS /////////////////////////////////////////////////////

@reservation[atomic]
+!reserve(OID, PID, X, Y)
    :   point(PID)[x(X), y(Y)]                                      // available collection point exists
	<-  -point(PID);
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
