//////////////////////////////////////////////////// ID GENERATION /////////////////////////////////////////////////////

orderCounter(0).
itemCounter(0).

@new_oID[atomic] +!new_ID(order, OT) <- -orderCounter(N); .concat(odr, N+1, O); .term2string(OT, O); +orderCounter(N+1).
@new_iID[atomic] +!new_ID(item, OT) <- -itemCounter(N); .concat(itm, N+1, O); .term2string(OT, O); +itemCounter(N+1).