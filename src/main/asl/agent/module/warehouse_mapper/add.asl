//////////////////////////////////////////////////// ITEMS ADDITION ////////////////////////////////////////////////////

@addItem[atomic]
+!kqml_received(Sender,achieve,add(Item),MsgID) <- !add(Item); .send(Sender,confirm,Msg,MsgID).
-!kqml_received(Sender,achieve,add(Item),MsgID) <- .send(Sender,failure,error(add(Item)),MsgID).

/***********************************************************************************************************************
 Utils
 **********************************************************************************************************************/

+!add(item(id(ID),position(rack(Rack),shelf(S),quantity(NewQ)))) : item(id(ID),quantity(Q),reserved(ReservedQ))
    <-  -item(id(ID),_,_)[source(self) | Positions];
        +item(id(ID),quantity(Q + NewQ),reserved(ReservedQ))[position(rack(Rack),shelf(S),quantity(NewQ)) | Positions].

+!add(item(id(ItemId),position(rack(Rack),shelf(Shelf),quantity(Quantity)))) : not item(id(ItemId),_,_)
    <-  +item(id(ItemId),quantity(Quantity),reserved(0))[position(rack(Rack),shelf(Shelf),quantity(Quantity))].