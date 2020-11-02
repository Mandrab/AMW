////////////////////////////////////////////////////// REMOVE ITEM /////////////////////////////////////////////////////

+!kqml_received(S, achieve, _, MID): cache(MID, Perf, Msg) <- .println("required cached message"); .send(S, Perf, Msg, MID).

@removeItem[atomic]
+!kqml_received(S, achieve, remove(Item), MID): Item = item(ID, quantity(Q))
    <-  .println("required item remotion");
        !is_sufficient(ID, Q);
        !remove(ID, Q, [OH|OT]);
        +cache(MID, confirm, remove(Item)[OH|OT]);
        .send(S, confirm, remove(Item)[OH|OT], MID).

-!kqml_received(S, achieve, remove(Item), MID) <- .println("failure in item remotion"); +cache(MID, failure, remove(Item)); .send(S,failure,remove(Item),MID).

/***********************************************************************************************************************
 Utils
 **********************************************************************************************************************/

+!is_sufficient(ID, Q): item(ID)[source(self)|T] <- !count(T, O); Q < O.

+!count([], 0).
+!count([position(_, _, quantity(Q))|T], O) <- !count(T, O1); O = Q + O1.

+!remove(ID, Q, [position(R, S, quantity(Q))]): item(ID)[position(R, S, quantity(Q))|P]
    <-  -item(ID); +item(ID)[P].
+!remove(ID, Q, [position(R, S, quantity(Q))]): item(ID)[position(R, S, quantity(IQ))|P] & IQ > Q
    <-  -item(ID); +item(ID)[position(R, S, quantity(IQ - Q))|P].
+!remove(ID, Q, [position(R, S, quantity(IQ))|O]): item(ID)[position(R, S, quantity(IQ))|P]
    <-  -item(ID); +item(ID)[P];
        !remove(ID, Q, O).