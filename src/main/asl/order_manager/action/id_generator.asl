//////////////////////////////////////////////////// ID GENERATION /////////////////////////////////////////////////////

orderCounter(0).                                                    // orders number counter

@new_oID[atomic]
+!new_ID(order, OT)                                                 // generate new unique order id
    <-  -orderCounter(N);
        .concat(odr, N+1, O1);                                      // get next id for order
        .my_name(A);                                                // get order manager name
        .concat(A, O1, O2);                                         // makes the id unique
        .term2string(OT, O2);
        +orderCounter(N+1).
