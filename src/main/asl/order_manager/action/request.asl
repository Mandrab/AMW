/////////////////////////////////////////////////// ORDER RECEPTION ////////////////////////////////////////////////////

+!kqml_received(Sender, achieve, order(C, E, A)[H|T], MID)
	<-  .println("[ORDER MANAGER] request for a new order");
	    !new_ID(order, OID);                                        // generate an id for the order
		+order(id(OID), status(check), user(C, E, A))[H|T];         // save order's info (status=checking for validity)
        !ensure_send(
            description("management(items)", "remove(item)"),       // send message to a warehouse manager
            achieve, remove(items, OID)[H|T]                        // ask for items reservation and positions
        );
        !cached_response(
            Sender,
            in(achieve, order(C, E, A)[H|T], MID),
            out(confirm, order(C, E, A)[H|T])
        ).                                                          // cache the response and send it

/////////////////////////////////////////////////// WAREHOUSE MANAGER's response

+!kqml_received(Sender, confirm, remove(items, OID)[mid(MID)|[H|T]], _)
    <-  .println("[ORDER MANAGER] order confirmed by warehouse");
        !response_received(MID);                                    // confirm ensure_send reception and get original id
        -order(id(OID), status(check), U);
        +order(id(OID), status(retrieve), U)[H|T];
        !ensure_send(
            description("management(items)", "info(collection_points)"),// send message to a collection point manager
            achieve, point(OID)                                     // ask for items reservation and positions
        ).

+!kqml_received(Sender, failure, remove(items, OID)[mid(MID)], _)
    <-  .println("[ORDER MANAGER] order refused by warehouse");
        !response_received(MID);                                    // confirm ensure_send reception and get original id
        -order(id(OID), status(check), U)[H|T];
        +order(id(OID), status(refused), U)[H|T].

/////////////////////////////////////////////////// COLLECTION POINT's response

+!kqml_received(Sender, confirm, point(PID, _, _)[mid(MID)], _)
    <-  .println("[ORDER MANAGER] collection point confirmed");
        !response_received(MID);                                    // confirm ensure_send reception and get original id
        -order(id(OID), status(retrieve), U)[H|T];
        +order(id(OID), status(retrieve), U, point(PID))[H|T];
        !retrieve_items(OID, [H|T], PID).

+!kqml_received(Sender, failure, point, _)
    <-  .println("[ORDER MANAGER] collection point refused").       // another request will be sent automatically
