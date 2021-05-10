//////////////////////////////////////////////////// ITEMS ADDITION ////////////////////////////////////////////////////

@addItem[atomic]
+!kqml_received(S, achieve, add(Item), MsgID)
    <-  .println("[WAREHOUSE MAPPER] request for item addition");
        !feasible_slot(Item);                                       // succeed if slot can contain this kind of item
        !add(Item);                                                 // add the item in that position
        +cache(MsgID, confirm, add(Item));                          // cache the response for that request
        .send(S, confirm, add(Item), MsgID).                        // confirm storing

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
