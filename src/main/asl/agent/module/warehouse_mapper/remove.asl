////////////////////////////////////////////////////// REMOVE ITEM /////////////////////////////////////////////////////

@removeItem[atomic]
+!kqml_received(Sender,achieve,remove(Item),MsgID) <- !remove(Item); .send(Sender,confirm,Msg,MsgID).
-!kqml_received(Sender,achieve,remove(Item),MsgID) <- .send(Sender,failure,error(remove(Item)),MsgID).

@removeReservedItem[atomic]
+!kqml_received(Sender,achieve,remove_reserved(Item),MsgID)
    <-  +removing_reserved;
        !remove(Item);
        .send(Sender,confirm,Msg,MsgID).
-!kqml_received(Sender,achieve,remove_reserved(Item),MsgID) <- .send(Sender,failure,error(remove_reserved(Item)),MsgID).

/***********************************************************************************************************************
 Utils
 **********************************************************************************************************************/

+!remove(item(id(ID),position(rack(R),shelf(S),quantity(Removed)))) : removing_reserved
    <-  -removing_reserved;
        ? item(id(ID),quantity(Q),reserved(Res))[source(self) | Positions] & Removed <= Res;
        !remove(position(rack(R),shelf(S),quantity(Removed)),Positions,Result);
        -item(id(ID),_,_);
        if (not .empty(Result)) { +item(id(ID),quantity(Q-Removed),reserved(Res-Removed))[[] | Result] }.

+!remove(item(id(ID),position(rack(R),shelf(S),quantity(Removed)))) : not removing_reserved
    <-  ? item(id(ID),quantity(Q),reserved(Res))[source(self) | Positions] & Removed <= Q-Res;
        !remove(position(rack(R),shelf(S),quantity(Removed)),Positions,Result);
        -item(id(ID),_,_);
        if (not .empty(Result)) { +item(id(ID),quantity(Q-Removed),reserved(Res))[[] | Result] }.

+!remove(position(R,S,quantity(Q1)),[position(R,S,quantity(Q2))|T],[position(R,S,quantity(Q2-Q1))|T]) : Q1 < Q2.
+!remove(P,[P|T],T).
+!remove(P,[H|T],[H|R]) <- !remove(P,T,R).