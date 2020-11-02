//////////////////////////////////////////////////// ITEMS ADDITION ////////////////////////////////////////////////////

+!kqml_received(S, achieve, _, MID): cache(MID, Perf, Msg) <- .println("required cached message"); .send(S, Perf, Msg, MID).

@addItem[atomic]
+!kqml_received(S, achieve, add(Item), MsgID)
    <-  .println("required item addition");
        !feasible_slot(Item);
        !add(Item);
        +cache(MsgID, confirm, add(Item));
        .send(S, confirm, add(Item), MsgID).

-!kqml_received(S, achieve, add(Item), MID) <- .println("failure in item addition"); +cache(MID, failure, add(Item)); .send(S, failure, add(Item), MID).

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
+!add(item(ID, P)): item(ID, RQ)[H|T] <- +item(ID)[P|[H|T]].

// no item with this id already exists
+!add(item(ID, P)) <- +item(ID)[P].