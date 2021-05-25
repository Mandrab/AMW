//////////////////////////////////////////////////// ITEMS ADDITION ////////////////////////////////////////////////////

@addItem[atomic]
+!kqml_received(Sender, achieve, add(Item), MID)                    // request item addition
    <-  .println("[WAREHOUSE MAPPER] request for item addition");
        !feasible_slot(Item);                                       // succeed if slot can contain this kind of item
        !add(Item);                                                 // add the item in that position
        !cached_response(
            Sender,
            in(achieve, add(Item), MID),
            out(confirm, add(Item), MID)                            // confirm storing
        ).                                                          // cache the response and send it

/***********************************************************************************************************************
 Utils
 **********************************************************************************************************************/

+!feasible_slot(item(_, position(R, S, _)))
    :   not _[position(R, S, _)|_].                                 // no item in this position

+!feasible_slot(item(ID, position(R, S, _)))
    :   item(ID)[position(R, S, _)|_].                              // an item of same type is in slot

+!add(item(ID, position(R, S, quantity(NQ))))
    :   item(ID)[position(R, S, quantity(OQ)) | T]
    <-  -item(ID);                                                  // delete the existing item
        +item(ID)[position(R, S, quantity(NQ + OQ)) | T].           // add the new item with the updated quantity

+!add(item(ID, P)): item(ID, RQ)[H|T] <- +item(ID)[P|[H|T]].        // add a new position for the existing item

+!add(item(ID, P)) <- +item(ID)[P].                                 // no item with this id already exists: just add it
