//////////////////////////////////////////////////// RETRIEVE ITEMS ////////////////////////////////////////////////////

+!retrieve_items([], _).
+!retrieve_items([H|T], PID): H = position(_, _, _) <- !retrieve(H, PID); !retrieve_items(T, PID).

+!retrieve(P, PID)
    <-  !ensure_send(
            description("executor(item_picker)", "retrieve(item)"), // send message to a collection point manager
            achieve, retrieve(P, point(PID))                        // ask for items reservation and positions
        ).

/////////////////////////////////////////////////// ROBOT PICKER's response

@itemRetrieved[atomic]
+!kqml_received(Sender, confirm, retrieve(P, point(PID))[mid(MID)], _)
    <-  !response_received(MID);                                    // confirm ensure_send reception
        !item_retrieved(Sender, P, PID, MID);
        !cached_response(
            Sender,
            in(confirm, retrieve(P, point(PID))[mid(MID)]),
            out(confirm, retrieve(P, point(PID))[mid(MID)])
        ).                                                          // cache the response and send it

+!item_retrieved(Sender, P, PID, MID)
    :   order(id(OID), status(retrieve), U, point(PID))[P|L]
    &   L = [H|T]                                                   // other items has to be retrieved
    &   H = position(_, _, _)
    <-  .println("[ORDER MANAGER] item retrieved");
        .abolish(order(id(OID), status(retrieve), U, point(PID)));
        +order(id(OID), status(retrieve), U, point(PID))[H|T].

+!item_retrieved(Sender, P, PID, MID)
    :   order(id(OID), status(retrieve), U, point(PID))[P]
    <-  .println("[ORDER MANAGER] last item retrieved");
        .abolish(order(id(OID), status(retrieve), U, point(PID))[P|L]);
        +order(id(OID), status(completed), U, point(PID));
        !ensure_send(
            description("management(items)", "info(collection_points)"),
            tell, free(OID)
        ).// TODO TEST

+!kqml_received(Sender, confirm, free(OID)[mid(MID)], _) <- !response_received(MID).// TODO TEST

// A failure should happen only if the robot is busy. To simplify logic, let's retry until it is available.
+!kqml_received(Sender, failure, retrieve(P, point(PID)), OID) <- .println("[ORDER MANAGER] robot refused retrieval").
