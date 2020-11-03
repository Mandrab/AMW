/////////////////////////////////////////////////// ORDER RECEPTION ////////////////////////////////////////////////////

+!kqml_received(Sender, achieve, order(C, E, A)[H|T], MsgID)
	<-  !new_ID(order, OID);                                        // generate an id for the order
		+order(id(OID), status(check), user(C, E, A))[H|T];         // save order's info (status=checking for validity)
		!random_agent("management(items)", "remove(item)", Provider);
        .send(Provider, achieve, remove(items)[H|T], OID);          // ask for items reservation and positions
        .send(Sender, confirm, order(client(C), E, A)[H|T], OID).