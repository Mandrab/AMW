////////////////////////////////////////////////////// INFO ITEMS //////////////////////////////////////////////////////

@infoItems[atomic]
+!kqml_received(Sender, achieve, info(warehouse), MsgID)            // send the warehouse state (items info & position)
    <-  .findall(item(id(ItemId),quantity(QT),reserved(R))[position(rack(RK),shelf(S),quantity(Q))],
                item(id(ItemId),quantity(QT),reserved(R))[position(rack(RK),shelf(S),quantity(Q))], L);
        !reshape(L, Res);
        .send(Sender, tell, Res, MsgID).

/***********************************************************************************************************************
 Utils
 **********************************************************************************************************************/

/////////////////////////////
/* TODO

        +!reshape([Head], [item(id(ItemId),reserved(ReservedNumber))[Pos]])
            :   Head = item(id(ItemId),quantity(Quantity),reserved(ReservedNumber))[Pos].

        +!reshape([Head | Tail], [item(id(ItemId),reserved(ReservedNumber))[Pos] | Res])
            :   Head = item(id(ItemId),quantity(Quantity),reserved(ReservedNumber))[Pos]
            <-  !reshape(Tail,Res).
*/

+!reshape([Head | Tail], Result) : Head = item(id(ItemId),quantity(Quantity),reserved(ReservedNumber))[ Pos ]
	<-  if (not .empty(Tail)) { !reshape(Tail,Res); }
		else { Res = []; }
		Result = [item(id(ItemId),reserved(ReservedNumber))[Pos] | Res].