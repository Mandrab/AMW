////////////////////////////////////////////////////// REMOVE ITEM /////////////////////////////////////////////////////

@removeItem[atomic]
+!kqml_received(Sender, tell, remove(Item), MID)
    :   Item = item(ID, quantity(Q))
    <-  .println("[WAREHOUSE MAPPER] required item removal");
        !is_sufficient(ID, Q);
        !remove(ID, Q, [OH|OT]);
        !cached_response(
            Sender,
            in(tell, remove(Item), MID),
            out(confirm, remove(Item)[OH|OT], MID)
        ).                                                          // cache the response and send it

@removeItems[atomic]
+!kqml_received(Sender, tell, remove(items)[H|T], MID)
    <-  .println("[WAREHOUSE MAPPER] required items removal");
        !are_sufficient([H|T]);
        !remove_all([H|T], [OH|OT]);
        !cached_response(
            Sender,
            in(tell, remove(items)[H|T], MID),
            out(confirm, remove(items)[OH|OT], MID)
        ).                                                          // cache the response and send it

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

+!remove(ID, 0, []).                                                // no retrieval

+!remove(ID, Q, [position(R, S, quantity(Q))])
    :   item(ID)[position(R, S, quantity(Q))|T]                     // item has exact quantity in one point
    &   .empty(T)                                                   // no more quantity in warehouse
    <-  -item(ID).                                                  // remove the item from the warehouse

+!remove(ID, Q, [position(R, S, quantity(Q))])
    :   item(ID)[position(R, S, quantity(GQ))|T]
    &   .empty(T)                                                   // no more quantity in warehouse
    &   GQ > Q                                                      // item has greater quantity in one point
    <-  -item(ID);
        +item(ID)[position(R, S, quantity(GQ - Q))].                // decrease the item in that point

+!remove(ID, Q1, [position(R, S, quantity(Q2))|O])
    :   item(ID)[position(R, S, quantity(Q2))|[H|T]]
    &   Q2 <= Q1                                                    // item has lesser or equal quantity in one point
    <-  -item(ID);                                                  // remove the item from the point
        +item(ID)[H|T];                                             // restore other positions
        !remove(ID, Q1 - Q2, O).                                    // retrieve remaining items

+!remove(ID, Q, [position(R, S, quantity(Q))])
    :   item(ID)[position(R, S, quantity(GQ))|T]
    &   GQ > Q                                                      // item has greater quantity in one point
    <-  -item(ID);
        +item(ID)[position(R, S, quantity(GQ - Q))|T].              // decrease the item in that point
