//////////////////////////////////////////////////// ID GENERATION /////////////////////////////////////////////////////

orderCounter(0).
itemCounter(0).

@new_oID[atomic] +!new_ID(order,O) <- -orderCounter(N); .concat(odr,N+1,O); +orderCounter(N+1).
@new_iID[atomic] +!new_ID(item,O) <- -itemCounter(N); .concat(itm,N+1,O); +itemCounter(N+1).

///////////////////////////// GENERATE (SIMPLE) ORDER ID

/*TODO
@first_order_id[atom]
+!new_order_id("o0")                                                          // generate an order ID
	:   not last_order_id(N) <- +last_order_id(0).

@next_order_id[atom]
+!new_order_id(S)                                                             // generate an order ID
	:   last_order_id(N)
	<-  -+last_order_id(N + 1);
		.concat(o, N + 1, S).

///////////////////////////// GENERATE (SIMPLE) ITEM ID

@next_item_id[atom]
+!new_item_id("i0")                                                           // generate an ID for a item retrieve
	:   not last_item_id(N) <- +last_item_id(0).

@first_item_id[atom]
+!new_item_id(S)                                                              // generate an ID for a item retrieve
	:   last_item_id(N)
	<-  -+last_item_id(N + 1);
		.concat(i, N + 1, S).
*/