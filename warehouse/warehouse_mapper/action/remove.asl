////////////////////////////////////////////////////// REMOVE ITEM /////////////////////////////////////////////////////

+!kqml_received(S, achieve, _, MID): cache(MID, Perf, M) <- .println("required cached message"); .send(S, Perf, M, MID).

@removeItem[atomic]
+!kqml_received(S, achieve, remove(Item), MID): Item = item(ID, quantity(Q))
    <-  .println("required item remotion");
        !is_sufficient(ID, Q);
        !remove(ID, Q, [OH|OT]);
        +cache(MID, confirm, remove(Item)[OH|OT]);
        .send(S, confirm, remove(Item)[OH|OT], MID).

-!kqml_received(S, achieve, remove(Item), MID)
    <-  .println("failure in item remotion");
        +cache(MID, failure, remove(Item));
        .send(S, failure, remove(Item), MID).

@removeItems[atomic]
+!kqml_received(S, achieve, remove(items)[H|T], MID)
    <-  .println("required items remotion");
        !are_sufficient([H|T]);
        !remove_all([H|T], [OH|OT]);
        +cache(MID, confirm, remove(items)[OH|OT]);
        .send(S, confirm, remove(items)[OH|OT], MID).

-!kqml_received(S, achieve, remove(items)[H|T], MID)
    <-  .println("failure in items remotion");
        +cache(MID, failure, remove(items)[H|T]);
        .send(S, failure, remove(items)[H|T], MID).

/***********************************************************************************************************************
 Utils
 **********************************************************************************************************************/

+!are_sufficient([]).
+!are_sufficient([item(ID, quantity(Q))|T]) <- !is_sufficient(ID, Q); !are_sufficient(T).

+!is_sufficient(ID, Q): item(ID)[source(self)|T] <- !count(T, O); Q <= O.

+!count([], 0).
+!count([position(_, _, quantity(Q))|T], O) <- !count(T, O1); O = Q + O1.

+!remove_all([], []).
+!remove_all([item(ID, quantity(Q))|T], R) <- !remove(ID, Q, RH); !remove_all(T, RT); .concat(RH, RT, R).

+!remove(ID, Q, [position(R, S, quantity(Q))]): item(ID)[position(R, S, quantity(Q))] <- -item(ID).
+!remove(ID, Q, [position(R, S, quantity(Q))]): item(ID)[position(R, S, quantity(Q))|P] <- -item(ID); +item(ID)[P].
+!remove(ID, Q, [position(R, S, quantity(Q))]): item(ID)[position(R, S, quantity(IQ))|P] & IQ > Q
    <- -item(ID); +item(ID)[position(R, S, quantity(IQ - Q))|P].
+!remove(ID, Q, [P|O]): item(ID)[P|Ps] <- -item(ID); +item(ID)[Ps]; !remove(ID, Q, O).