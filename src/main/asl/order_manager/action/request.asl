/////////////////////////////////////////////////// ORDER RECEPTION ////////////////////////////////////////////////////

+!kqml_received(Sender, achieve, order(C, E, A)[H|T], MsgID)
	<-  .println("[ORDER MANAGER] request for a new order");
	    !new_ID(order, OID);                                        // generate an id for the order
		+order(id(OID), status(check), user(C, E, A))[H|T];         // save order's info (status=checking for validity)
		!random_agent("management(items)", "remove(item)", A2);     // find a warehouse manager agent
        .send(A2, achieve, remove(items)[H|T], OID);                // ask for items reservation and positions
        +cache(MsgID, confirm, order(C, E, A)[H|T]);
        .send(Sender, confirm, order(C, E, A)[H|T], OID).

-!kqml_received(Sender, achieve, O, MsgID) <- .send(Sender, failure, O, MsgID). // send failure but not cache response

/////////////////////////////////////////////////// WAREHOUSE MANAGER's response

+!kqml_received(Sender, confirm, remove(items)[H|T], OID)
    <-  .println("[ORDER MANAGER] order confirmed by warehouse");
        -order(id(OID), status(check), U)[_];
        +order(id(OID), status(retrieve), U)[H|T];
        !random_agent("management(items)", "info(collection_points)", A);
        .send(A, achieve, point, OID).

+!kqml_received(Sender, failure, remove(items)[H|T], OID)
    <-  .println("[ORDER MANAGER] order refused by warehouse");
        -order(id(OID), status(check), U)[H|T];
        +order(id(OID), status(refused), U)[H|T].

/////////////////////////////////////////////////// COLLECTION POINT's response

+!kqml_received(Sender, confirm, point(PID), OID)
    <-  .println("[ORDER MANAGER] collection point confirmed");
        -order(id(OID), status(check), U)[H|T];
        +order(id(OID), status(retrieve), U, point(PID))[H|T];
        !retrieve_items(OID, [H|T], PID).

+!kqml_received(Sender, failure, remove(items)[H|T], OID)
    <-  .println("[ORDER MANAGER] collection point refused");
        .wait(2500);
        .send(Sender, achieve, point, OID).