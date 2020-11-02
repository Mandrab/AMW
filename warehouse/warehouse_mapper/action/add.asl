//////////////////////////////////////////////////// ITEMS ADDITION ////////////////////////////////////////////////////

+!kqml_received(S, achieve, _, MID): cache(MID, Perf, Msg) <- .send(S, Perf, Msg, MID).

@addItem[atomic]
+!kqml_received(S, achieve, add(Item), MsgID)
    <-  !feasible_slot(Item);
        !add(Item);
        +cache(MsgID, confirm, add(Item));
        .send(S, confirm, add(Item), MsgID).

-!kqml_received(S, achieve, add(Item), MID) <- +cache(MID, failure, add(Item)); .send(S, failure, add(Item), MID).

/***********************************************************************************************************************
 Utils
 **********************************************************************************************************************/

// free slot
+!feasible_slot(item(_, position(R, S, _))): not _[position(R, S, _)|_].

// item of same type in slot
+!feasible_slot(item(ID, position(R, S, _))): item(ID)[position(R, S, _)|_].

// update an entry of the existing item
+!add(item(ID, position(R, S, quantity(NQ)))): item(ID)[position(R, S, quantity(OQ)) | T]
    <-  -item(ID); +item(ID)[position(R, S, quantity(NQ + OQ)) | T].

// add an entry of the existing item
+!add(item(ID, P)): item(ID, RQ)[L] <- +item(ID)[P|L].

// no item with this id already exists
+!add(item(ID, P)) <- +item(ID)[P].