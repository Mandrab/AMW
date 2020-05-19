/***********************************************************************************************************************
 Pre-Processing Directives
 **********************************************************************************************************************/

{ include("literal.asl") }                                          // include utilities for works on literals
{ include("state/warehouse.asl") }

/***********************************************************************************************************************
 Initial goals
 **********************************************************************************************************************/

!setup.                                                             // setup

/***********************************************************************************************************************
 Plans
 **********************************************************************************************************************/

+!setup : not set
	<-  .df_register("management(items)", "info(warehouse)");       // register as warehouse's infos dispatcher
		.df_register("management(items)", "store(item)");           // register for acquire information about items TODO
		.df_register("management(items)", "retrieve(item)");        // register for remove infos at item removal
		+set.                                                       // set setup-process ended

@processOrder[atomic]
+!kqml_received(Sender, achieve, Content, MsgID)                  // receive the intention of pick item(s)
	:   Content = retrieve(order_id(OrderId))[ [] | Items ]
	<-  !is_set(Items);
	    !sufficient(Items);                           // check if all the elements exists (in quantity)
        !reserve(Items, Positions);                           // try to reserve the items
        !concat(confirmation(order_id(OrderId)), Positions, Msg);
        .send(Sender, confirm, Msg, MsgID).
-!kqml_received(Sender, achieve, retrieve(order_id(OrderId))[_], MsgID)
    <-  .send(Sender, failure, error(order_id(OrderId)), MsgID).

+!kqml_received(Sender, achieve, add(Item), MsgID) <- !add(Item); .send(Sender, confirm, Msg, MsgID).
-!kqml_received(Sender, achieve, add(Item), MsgID) <- .send(Sender, failure, error(add(Item)), MsgID).

@infoItems[atomic]
+!kqml_received(Sender, achieve, info(warehouse), MsgID)         // send the warehouse state (items info & position)
    <-  .findall(item(id(ItemId), quantity(QT), reserved(R))
                [ position(rack(RK), shelf(S), quantity(Q)) ],
                item(id(ItemId), quantity(QT), reserved(R))
                [ position(rack(RK), shelf(S), quantity(Q)) ], L);
        !reshape(L, Res);
        .send(Sender, tell, Res, MsgID).

/***********************************************************************************************************************
 Utils
 **********************************************************************************************************************/

@add[atomic]
+!add(item(id(ItemId), position(rack(Rack), shelf(Shelf), quantity(NewQuantity))))
    :   item(id(ItemId), quantity(Quantity), reserved(ReservedQ))
    <-  -item(id(ItemId), _, _)[source(self)|Positions];
        +item(id(ItemId), quantity(Quantity + NewQuantity), reserved(ReservedQ))
            [position(rack(Rack), shelf(Shelf), quantity(NewQuantity)) | Positions].

@addNew[atomic]
+!add(item(id(ItemId), position(rack(Rack), shelf(Shelf), quantity(Quantity)))) : not item(id(ItemId), _, _)
    <-  +item(id(ItemId), quantity(Quantity), reserved(0))[position(rack(Rack), shelf(Shelf), quantity(Quantity))].

///////////////////////////// TODO

+!release(Item)
	:   Item = item(id(ItemId), quantity(RequiredQ))
	&   item(id(ItemId), quantity(Quantity), reserved(ReservedQ))
    <-  -+item(id(ItemId), quantity(Quantity), reserved(ReservedQ - RequiredQ)).

///////////////////////////// CHECK SET OF ITEMS

+!not_in(A,[]).
+!not_in(A,[B|C]) <- !different(A, B); !not_in(A,C).

+!is_set([]).
+!is_set([_]).
+!is_set([A|B]) <- !not_in(A,B); !is_set(B).

+!different(item(id(ItemID1),_), item(id(ItemID2),_)) <- .eval(false, ItemID1 == ItemID2).

/////////////////////////////

+!sufficient(item(id(ItemID), quantity(RequiredQ))) : item(id(ItemID), quantity(StoredQ), reserved(ReservedQ))
    <-  .eval(true, RequiredQ <= StoredQ - ReservedQ).
+!sufficient([]).
+!sufficient([H | []]) <- !sufficient(H).
+!sufficient([H | T]) <- !sufficient(H); !sufficient(T).

/////////////////////////////

+!reserve(item(id(ItemID), quantity(RequiredQ)), Output) : item(id(ItemID), _, _)[source(self) | Positions]
    <-  !concat(item(id(ItemID)), Positions, Output);
        -item(id(ItemID), quantity(StoredQ), reserved(ReservedQ))[source(self) | Positions];
        +item(id(ItemID), quantity(StoredQ), reserved(ReservedQ + RequiredQ))[source(self) | Positions].
// TODO? reserve([], []).
+!reserve([H | []], [Output]) <- !reserve(H, Output).
+!reserve([H | T], [Output1 | Output2]) <- !reserve(H, Output1); !reserve(T, Output2).

/////////////////////////////

+!reshape([ Head | Tail ], Result)
	:   Head = item(id(ItemId), quantity(Quantity), reserved(ReservedNumber))[ Pos ]
	<-  if (not .empty(Tail)) { !reshape(Tail, Res); }
		else { Res = []; }
		Result = [ item(id(ItemId), reserved(ReservedNumber))[ Pos ] | Res ].
