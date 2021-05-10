/////////////////////////////////////////////////// ORDER RECEPTION ////////////////////////////////////////////////////

+!kqml_received(Sender, achieve, order(C, E, A)[H|T], MID)
	<-  .println("[ORDER MANAGER] request for a new order");
	    !new_ID(order, OID);                                        // generate an id for the order
		+order(id(OID), status(check), user(C, E, A))[H|T];         // save order's info (status=checking for validity)
        !ensure_send(
            description("management(items)", "remove(item)"),       // send message to a warehouse manager
            tell, remove(items)[H|T], OID                           // ask for items reservation and positions
        );
        !cached_response(
            Sender,
            in(achieve, order(C, E, A)[H|T], MID),
            out(confirm, order(C, E, A)[H|T], OID)
        ).                                                          // cache the response and send it

/////////////////////////////////////////////////// WAREHOUSE MANAGER's response

+!kqml_received(Sender, confirm, remove(items)[H|T], MID)
    <-  .println("[ORDER MANAGER] order confirmed by warehouse");
        !response_received(MID, OID);                               // confirm ensure_send reception and get original id
        -order(id(OID), status(check), U)[_];
        +order(id(OID), status(retrieve), U)[H|T];
        !ensure_send(
            description("management(items)", "info(collection_points)"),// send message to a collection point manager
            tell, point, OID                                        // ask for items reservation and positions
        ).

+!kqml_received(Sender, failure, remove(items)[H|T], MID)
    <-  .println("[ORDER MANAGER] order refused by warehouse");
        !response_received(MID, OID);                               // confirm ensure_send reception and get original id
        -order(id(OID), status(check), U)[H|T];
        +order(id(OID), status(refused), U)[H|T].

/////////////////////////////////////////////////// COLLECTION POINT's response

+!kqml_received(Sender, confirm, point(PID), MID)
    <-  .println("[ORDER MANAGER] collection point confirmed");
        !response_received(MID, OID);                               // confirm ensure_send reception and get original id
        -order(id(OID), status(retrieve), U)[H|T];
        +order(id(OID), status(retrieve), U, point(PID))[H|T];
        !retrieve_items(OID, [H|T], PID).

+!kqml_received(Sender, failure, point, MID)
    <-  .println("[ORDER MANAGER] collection point refused").       // another request will be sent automatically
