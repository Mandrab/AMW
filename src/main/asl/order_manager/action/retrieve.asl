//////////////////////////////////////////////////// RETRIEVE ITEMS ////////////////////////////////////////////////////

+!retrieve_items(OID, [], PID).
+!retrieve_items(OID, [H|T], PID) <- !retrieve(OID, H, PID); !retrieve_items(OID, T, PID).

+!retrieve(OID, P, PID)
    <-  !random_agent("executor(item_picker)", "retrieve(item)", RP);
        .send(RP, evaluate, retrieve(P, point(PID)), OID).// TODO change MID: equal for all the item

/////////////////////////////////////////////////// ROBOT PICKER's response

+!kqml_received(Sender, confirm, retrieve(P, point(PID)), OID)
    :   order(id(OID), S, U, point(PID))[P|L]
    &   L = [H|T]
    &   H = item(_, _)
    <-  .println("[ORDER MANAGER] item retrieved");
        -order(id(OID), S, U, point(PID));
        +order(id(OID), S, U, point(PID))[H|T].

+!kqml_received(Sender, confirm, retrieve(P, point(PID)), OID)
    :   order(id(OID), S, U, point(PID))[P]
    <-  .println("[ORDER MANAGER] last item retrieved");
        -order(id(OID), _, U, point(PID));
        +order(id(OID), status(completed), U, point(PID)).

/*
+!kqml_received(Sender, confirm, retrieve(P, point(PID)), OID).
+!kqml_received(Sender, failure, retrieve(P, point(PID)), OID)
    <-  .
*/