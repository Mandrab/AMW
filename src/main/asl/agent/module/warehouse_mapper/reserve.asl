////////////////////////////////////////////////////// REMOVE ITEM /////////////////////////////////////////////////////

@reserveItems[atomic]
+!kqml_received(Sender,achieve,Content,MsgID)                       // receive the intention of pick item(s)
	:   Content = retrieve(order_id(OrderId))[[] | Items]
	<-  !is_set(Items);
	    !sufficient(Items);                                         // check if all the elements exists (in quantity)
        !reserve(Items,Positions);                                  // try to reserve the items
        !concat(order_id(OrderId),Positions,Msg);
        .send(Sender,confirm,Msg,MsgID).
-!kqml_received(Sender,achieve,retrieve(order_id(OrderId))[_],MsgID) <- .send(Sender,failure,order_id(OrderId),MsgID).

/***********************************************************************************************************************
 Utils
 **********************************************************************************************************************/

///////////////////////////// CHECK SET OF ITEMS

+!different(item(id(ItemID1),_), item(id(ItemID2),_)) <- .eval(false, ItemID1 == ItemID2).

+!not_in(A,[]).
+!not_in(A,[B|C]) <- !different(A, B); !not_in(A,C).

+!is_set([]).
+!is_set([_]).
+!is_set([A|B]) <- !not_in(A,B); !is_set(B).

///////////////////////////// CHECK SUFFICIENT QUANTITY

+!sufficient(item(id(ItemID), quantity(RequiredQ))) : item(id(ItemID), quantity(StoredQ), reserved(ReservedQ))
    <-  .eval(true, RequiredQ <= StoredQ - ReservedQ).
+!sufficient([]).
+!sufficient([H | []]) <- !sufficient(H).
+!sufficient([H | T]) <- !sufficient(H); !sufficient(T).

///////////////////////////// RESERVE ITEMS

+!reserve(item(id(ItemID), quantity(RequiredQ)), Output) : item(id(ItemID),_,_)[source(self) | Positions]
    <-  !concat(item(id(ItemID)), Positions, Output);
        -item(id(ItemID), quantity(StoredQ), reserved(ReservedQ))[source(self) | Positions];
        +item(id(ItemID), quantity(StoredQ), reserved(ReservedQ + RequiredQ))[source(self) | Positions].
// TODO? reserve([], []).
+!reserve([H | []], [Output]) <- !reserve(H, Output).
+!reserve([H | T], [Output1 | Output2]) <- !reserve(H, Output1); !reserve(T, Output2).