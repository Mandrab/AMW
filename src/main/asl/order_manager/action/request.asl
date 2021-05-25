/////////////////////////////////////////////////// ORDER RECEPTION ////////////////////////////////////////////////////

+!kqml_received(Sender, achieve, order(C, E, A)[H|T], MID)          // request order submission
	<-  .println("[ORDER MANAGER] request for a new order");
	    !new_ID(order, OID);                                        // generate an id for the order
		+order(id(OID), status(check), user(C, E, A));              // save order's info (status=checking for validity)
        !ensure_send(
            description("management(items)", "remove(item)"),       // send message to a warehouse manager
            achieve, remove(items, OID)[H|T]                        // ask for items reservation and positions
        );
        !cached_response(
            Sender,
            in(achieve, order(C, E, A)[H|T], MID),
            out(confirm, order(C, E, A)[H|T], MID)                  // confirm order submission reception
        ).                                                          // cache the response and send it

/////////////////////////////////////////////////// WAREHOUSE MANAGER's response

+!kqml_received(Sender, confirm, remove(items, OID)[mid(MID)|L], _) // item removal request
    <-  .println("[ORDER MANAGER] order confirmed by warehouse");
        !response_received(MID);                                    // confirm ensure_send reception
        !order_position(OID, L);                                    // set position of the items
        !ensure_send(
            description("management(items)", "info(collection_points)"),// send message to a collection point manager
            achieve, point(OID)                                     // ask for items reservation and positions
        ).

+!kqml_received(Sender, failure, remove(items, OID)[mid(MID)], _)   // order items not available
    <-  .println("[ORDER MANAGER] order refused by warehouse");
        !response_received(MID);                                    // confirm ensure_send reception and get original id
        !order_fail(OID).                                           // set order status as failed

/////////////////////////////////////////////////// COLLECTION POINT's response

+!kqml_received(Sender, confirm, point(OID, PID, _, _)[mid(MID)], _)// collection point confirmation
    <-  .println("[ORDER MANAGER] collection point confirmed");
        !response_received(MID);                                    // confirm ensure_send response reception
        !order_point(OID, PID, [H|T]);                              // save collection point and get items positions
        !retrieve_items([H|T], PID).                                // ask for item retrieval

// collection point reservation failed. Another request will be sent automatically
+!kqml_received(Sender, failure, point(OID), _) <- .println("[ORDER MANAGER] collection point refused").

//////////////////////////////////////////////////// UTILITY PLANS /////////////////////////////////////////////////////

@updateOrderPosition[atomic]
+!order_position(OID, [H|T])                                        // save items positions
    :   order(id(OID), status(check), U)
    <-  .abolish(order(id(OID), _, _));
        +order(id(OID), status(retrieve), U)[H|T].

@orderFailure[atomic]                                               // set order as failed
+!order_fail(OID): order(id(OID), _, U)[H|T] <- .abolish(order(id(OID),_,_)); +order(id(OID), status(refused), U)[H|T].

@updateOrderPoint[atomic]
+!order_point(OID, PID, [H|T])                                      // set collection point for order
    :   order(id(OID), status(retrieve), U)[H|T]
    <-  .abolish(order(id(OID), _, _));
        +order(id(OID), status(retrieve), U, point(PID))[H|T].
