/////////////////////////////////////////////////// ORDER RECEPTION ////////////////////////////////////////////////////

+!kqml_received(Sender, achieve, order(C, E, A)[H|T], MsgID)
	<-  .println("new order request");
	    !new_ID(order, OID);                                        // generate an id for the order
		+order(id(OID), status(check), user(C, E, A))[H|T];         // save order's info (status=checking for validity)
		!random_agent("management(items)", "remove(item)", Provider);
        .send(Provider, achieve, remove(items)[H|T], OID);          // ask for items reservation and positions
        .send(Sender, confirm, order(client(C), E, A)[H|T], OID).

+!kqml_received(Sender, confirm, remove(items)[H|T], OID)
    <-  .println("order confirmed by warehouse");
        -order(id(OID), status(check), U)[H|T];
        +order(id(OID), status(retrieve), U)[H|T];
        // TODO require collection point
        // TODO require retrieve robot
        .

+!kqml_received(Sender, failure, remove(items)[H|T], OID)
    <-  .println("order refused by warehouse");
        -order(id(OID), status(check), U)[H|T];
        +order(id(OID), status(refused), U)[H|T].
