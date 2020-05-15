/***********************************************************************************************************************
 Initial beliefs and rules
 **********************************************************************************************************************/

set(false).                                                       // at start is not yet set

// initial known items and positions:
item(id("Item 1"), quantity(20), reserved(0)) [
		position(rack(5), shelf(3), quantity(5)),
		position(rack(5), shelf(2), quantity(8)),
		position(rack(6), shelf(3), quantity(7)) ].
item(id("Item 2"), quantity(1), reserved(1)) [
		position(rack(2), shelf(4), quantity(1)) ].
item(id("Item 3"), quantity(1), reserved(0)) [
		position(rack(2), shelf(5), quantity(1)) ].

/***********************************************************************************************************************
 Initial goals
 **********************************************************************************************************************/

!setup.                                                             // setup

/***********************************************************************************************************************
 Plans
 **********************************************************************************************************************/

+!setup : set(false)
	<-  .df_register("management(items)", "info(warehouse)"); // register as warehouse's infos dispatcher
		.df_register("management(items)", "store(item)");     // register for acquire information about items
		.df_register("management(items)", "retrieve(item)");  // register for remove infos at item removal
		.include("util/literal.asl");                            // include literal utils plans
		-+set(true).                                              // set process ended

// OPERATION #3 in order submission schema
@processOrder[atomic] // TODO or @up[atomic]
+!kqml_received(Sender, achieve, Content, MsgId)                  // receive the intention of pick item(s)
	:   Content = retrieve(order_id(OrderId))[ [] | Items ]
	<-  !sufficient(Items, Sufficient);                           // check if all the elements exists (in quantity)
        if (not Sufficient) {                                     // if at least an item doesn't exist, send error msg
            .send(Sender, failure, error(order_id(OrderId), error_code(404)));
        } else {
            !reserve(Items, Positions);                           // try to reserve the items
            if (Positions = failed) {                             // if i get a conflict error, send it back
                .send(Sender, failure, error(order_id(OrderId), error_code(409)));
            } else {
                !concat(confirmation(order_id(OrderId)), Positions, Msg);
                .send(Sender, confirm, Msg);
            }
        }.

// OPERATION #4 in order submission schema
+!sufficient([ Item | [] ], Result)                               // check if there's a sufficient quantity of product
	:   Item = item(id(ItemId), quantity(RequiredQ))
	<-  .eval(Result, item(id(ItemId), quantity(StoredQ), reserved(ReservedQ))
			& RequiredQ <= StoredQ - ReservedQ).                   // return result

+!sufficient([ Item | Tail ], Result)                             // check if the prods's quantities are sufficient
	<-  !sufficient([ Item ], HeadRes);                           // check for the first element
		!sufficient(Tail, TailRes);                               // check for the remaining elements
		.eval(Result, HeadRes == true & TailRes == true).         // return result

// OPERATION #9 in order submission schema
+!reserve([ Item | [] ], [ Result ])
	:   Item = item(id(ItemId), quantity(RequiredQ))
	&   item(id(ItemId), quantity(StoredQ), reserved(ReservedQ))
	&   RequiredQ <= StoredQ - ReservedQ
	<-  .findall(P, item(id(ItemId), _, _)[ P, source(self) ], Positions);
		!concat(item(id(ItemId)), Positions, Result);
		!concat(item(id(ItemId), quantity(StoredQ), reserved(ReservedQ + RequiredQ)), Positions, R);
		-item(id(ItemId), quantity(StoredQ), reserved(ReservedQ)); +R.

+!reserve([ Item | [] ], Result)
	:   Item = item(id(ItemId), quantity(RequiredQ))
    &   (not item(id(ItemId), quantity(StoredQ), reserved(ReservedQ)) | StoredQ - ReservedQ < RequiredQ)
	<-  Result = failed.

+!reserve([ Item | Tail ], Result)
	<-  !reserve([ Item ], HeadRes);                              // reserve the item in the head
		if(HeadRes = failed) { Result = failed; }                 // if fail, then fail all
		else {                                                      // if reserve is success
			!reserve(Tail, TailRes);                              // reserve remaining items
			if (TailRes = failed) {                               // if fail, restore head and fail all
				Result = failed;
				!release(Item);
			} else { Result = [ HeadRes, TailRes ]; }               // if tail reservation success, set result
		}.

+!release(Item)
	:   Item = item(id(ItemId), quantity(RequiredQ))
	&   item(id(ItemId), quantity(Quantity), reserved(ReservedQ))
    <-  -+item(id(ItemId), quantity(Quantity), reserved(ReservedQ - RequiredQ)).

+!kqml_received(Sender, cfp, Content, MsgId)                      // send the warehouse state (items info & position)
	:   Content = info(warehouse)
    <-  .findall(item(id(ItemId), quantity(QT), reserved(R))
                [ position(rack(RK), shelf(S), quantity(Q)) ],
                item(id(ItemId), quantity(QT), reserved(R))
                [ position(rack(RK), shelf(S), quantity(Q)) ], L);
        !reshape(L, Res);
        .send(Sender, propose, Res, MsgId).

/***********************************************************************************************************************
 Utils
 **********************************************************************************************************************/

+!reshape([ Head | Tail ], Result)
	:   Head = item(id(ItemId), quantity(Quantity), reserved(ReservedNumber))[ Pos ]
	<-  if (not .empty(Tail)) { !reshape(Tail, Res); }
		else { Res = []; }
		Result = [ item(id(ItemId), reserved(ReservedNumber))[ Pos ] | Res ].
