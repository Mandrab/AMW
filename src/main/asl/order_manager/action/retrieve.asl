//////////////////////////////////////////////////// RETRIEVE ITEMS ////////////////////////////////////////////////////

+!retrieve_items(OID, [], PID).
+!retrieve_items(OID, [H|T], PID) <- !retrieve(OID, H, PID); !retrieve_items(OID, T, PID).

+!retrieve(OID, P, PID)
    <-  !ensure_send(
            description("executor(item_picker)", "retrieve(item)"), // send message to a collection point manager
            achieve, retrieve(P, point(PID))                        // ask for items reservation and positions
        ).

/////////////////////////////////////////////////// ROBOT PICKER's response

@itemRetrieved[atomic]
+!kqml_received(Sender, confirm, retrieve(P, point(PID))[mid(EMID)], MID)
    :   order(id(OID), S, U, point(PID))[P|L]
    &   L = [H|T]
    &   H = item(_, _)                                              // other items has to be retrieved
    <-  .println("[ORDER MANAGER] item retrieved");
        !response_received(EMID);                                    // confirm ensure_send reception and get original id
        -order(id(OID), S, U, point(PID));
        +order(id(OID), S, U, point(PID))[H|T];
        !cached_response(
            Sender,
            in(confirm, retrieve(P, point(PID)), MID),
            out(confirm, retrieve(P, point(PID))[mid(EMID)])
        ).                                                          // cache the response and send it

@lastItemRetrieved[atomic]
+!kqml_received(Sender, confirm, retrieve(P, point(PID))[mid(EMID)], MID)
    :   order(id(OID), S, U, point(PID))[P]
    <-  .println("[ORDER MANAGER] last item retrieved");
        !response_received(EMID);                                   // confirm ensure_send reception and get original id
        -order(id(OID), _, U, point(PID));
        +order(id(OID), status(completed), U, point(PID));
        !cached_response(
            Sender,
            in(confirm, retrieve(P, point(PID)), MID),
            out(confirm, retrieve(P, point(PID))[mid(EMID)])
        );                                                          // cache the response and send it
        !ensure_send(
            description("management(items)", "info(collection_points)"),
            tell, free(OID)
        ).// TODO TEST

// A failure should happen only if the robot is busy.
// To avoid problems, let's retry until it is available.
+!kqml_received(Sender, failure, retrieve(P, point(PID)), OID)
    <-  .println("[ORDER MANAGER] robot refused retrieval").
