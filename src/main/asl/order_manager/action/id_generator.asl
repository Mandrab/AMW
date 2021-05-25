//////////////////////////////////////////////////// ID GENERATION /////////////////////////////////////////////////////

orderCounter(0).

@new_oID[atomic]
+!new_ID(order, OT)
    <-  -orderCounter(N);
        .concat(odr, N+1, O);
        .term2string(OT, O);
        +orderCounter(N+1).
