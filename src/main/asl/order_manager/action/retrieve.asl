//////////////////////////////////////////////////// RETRIEVE ITEMS ////////////////////////////////////////////////////

+!retrieve_items(OID, [], PID).
+!retrieve_items(OID, [H|T], PID) <- !retrieve(OID, H, PID); !retrieve_items(OID, T, PID).

+!retrieve(OID, P, PID)
    <-  !random_agent("executor(item_picker)", "retrieve(item)", A);
        .send(A, evaluate, retrieve(P, point(PID)), OID).// TODO change MID: equal for all the item
/*
+!kqml_received(Sender, confirm, retrieve(P, point(PID)), OID)
    :   order(id(OID), S, U, point(PID))[P];
    <-  -order(id(OID), _, U, point(PID))[P|T];
        +order(id(OID), status(completed), U, point(PID))[[]|T].
+!kqml_received(Sender, confirm, retrieve(P, point(PID)), OID)
    <-  -order(id(OID), S, U, point(PID))[P|T];
        +order(id(OID), S, U, point(PID))[[]|T].
+!kqml_received(Sender, confirm, retrieve(P, point(PID)), OID).
+!kqml_received(Sender, failure, retrieve(P, point(PID)), OID)
    <-  .*/