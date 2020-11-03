/***********************************************************************************************************************
 Pre-Processing Directives
 **********************************************************************************************************************/

{ include("utilities/literal.asl") }
{ include("utilities/communication.asl") }
{ include("action/id_generator.asl") }
{ include("action/info.asl") }
{ include("action/request.asl") }

/***********************************************************************************************************************
 Initial goals
***********************************************************************************************************************/

!setup.                                                             // start setup

/***********************************************************************************************************************
 Plans
***********************************************************************************************************************/

///////////////////////////// AGENT SETUP

@setup[atomic]
+!setup : not set
    <-  .df_register("management(orders)", "accept(order)");
        .df_register("management(orders)", "info(orders)");
        +set.// register service as order acceptor
/*
////////////////////////////////////////// COLLECTION POINTS MANAGER RESPONSE //////////////////////////////////////////

///////////////////////////// PROPOSAL ACCEPT
// TODO ONE EVENT FOR REASONING CYCLE. NO EVENT GENERATED IN CONCURRENT PART. -> NO NEED OF ATOMIC
+!kqml_received(Sender,propose,Content,MsgID)                    // receive items position and reservation confirm
	:   Content = point(OrderID)[x(XPos),y(YPos)]
	&   order(id(OrderID),status(checking_gather_point),ClientInfo,items(Items))
	<-  -+order(id(OrderID),status(retrieving),ClientInfo,items(Items));
        .send(Sender,accept_proposal,point(OrderID),MsgID);
        !mark_items(OrderID,Items,RIDs);
        !retrieve(OrderID,RIDs).                                 // retrieve all the items

+!kqml_received(Sender,propose,Content,MsgID) : Content = point(OrderID)[x(_),y(_)] <- .send(Sender,tell,free(OrderID),MsgID).
+!kqml_received(Sender,refuse,Content,MsgID) : Content = point(OrderID).      // 'gather' will automatically retry

//////////////////////////////////////////////////// ROBOT RESPONSE ////////////////////////////////////////////////////

///////////////////////////// PROPOSAL ACCEPT

@retrieve_proposal[atomic]
+!kqml_received(Sender,propose,retrieve(ID),MsgID) : retrieve(ID)[_,_,accepted(A),remaining(Q)] & A < Q
	<-  -retrieve(ID)[order(OrderID),Item,accepted(A),Remaining];
	    +retrieve(ID)[order(OrderID),Item,accepted(A+1),Remaining];
	    .send(Sender,accept_proposal,retrieve(ID),MsgID).

+!kqml_received(Sender,propose,retrieve(ID),MsgID) <- .send(Sender,reject_proposal,retrieve(ID)).
+!kqml_received(_,refuse,retrieve(ID),_).                               // managed by timeout

///////////////////////////// FAILURE

@fail_retrieve[atomic]
+!kqml_received(Sender,failure,retrieve(RID),MsgID) : retrieve(RID)[order(OrderID),_,accepted(A),_]
	<-  -retrieve(RID)[order(OrderID),Item,accepted(A),Quantity];
        +retrieve(RID)[order(OrderID),Item,accepted(A-1),Quantity];.println(aa);
        !retrieve(OrderID, [RID]).

///////////////////////////// COMPLETED

@complete_retrieve[atomic]
+!kqml_received(Sender,complete,retrieve(RID),MsgID)
	<-  -retrieve(RID)[order(OrderID),Item,accepted(A),remaining(Q)];
		+retrieve(RID)[order(OrderID),Item,accepted(A-1),remaining(Q-1)];
		!check_missing(OrderID).

/***********************************************************************************************************************
 Utils
 **********************************************************************************************************************

//////////////////////////////////////////////// ORDER'S ITEMS GATHERING ///////////////////////////////////////////////

///////////////////////////// RESHAPE

+!reshape_item(item(id(ID),quantity(Q)),[PH|_],Reshaped) : PH = item(id(ID))[[] | Positions]
    <-  !concat(item(id(ID),quantity(Q)),Positions,Reshaped).
+!reshape_item(item(id(ID),quantity(Q)),[_|PT],Reshaped) <- !reshape_item(item(id(ID),quantity(Q)),PT,Reshaped).
+!reshape_items([],_,[]).
+!reshape_items([IH|IT],Positions,[R1|R2]) <- !reshape_item(IH,Positions,R1); !reshape_items(IT,Positions,R2).

///////////////////////////// GATHER

+!gather(OID)
    <-  !random_agent("management(items)","info(collection_points)",Provider);
        .send(Provider,cfp,point(OrderID));
        .wait(5000);
        ? not order(id(OrderID),status(checking_gather_point),_,_).
-!gather(OrderID) <- !gather(OrderID).

///////////////////////////// RETRIEVE

+!retrieve(_,_,0).
+!retrieve(RID,Item,Times)
    <-  !random_agent("executor(item_picker)","retrieve(item)",Provider);
        .send(Provider,cfp,retrieve(id(RetrieveID),item(ReshapedItem)));
        !retrieve(RID,Item,Times-1).
+!retrieve(OrderID,RID) : retrieve(RID)[order(OrderID),item(Item),accepted(A),remaining(R)]
    <-  !concat(item(ItemID),Positions,ReshapedItem);
        !retrieve(RID,ReshapedItem,R-A);
        .wait(2000);
        ? retrieve(RID)[_,_,accepted(K),remaining(J)] & J <= K.
-!retrieve(OrderID,RID) <- !retrieve(OrderID,RID);.

+!retrieve(OrderID,[I]) <- !retrieve(OrderID,I).
+!retrieve(OrderID,[IH|IT]) <- !!retrieve(OrderID,IH); !retrieve(OrderID,IT).

+!mark_items(OrderID,Item,RID) : Item = item(_,quantity(Q))[_|_]
    <-  !new_ID(item,RID); +retrieve(RID)[order(OrderID),item(Item),accepted(0),remaining(Q)].
+!mark_items(OrderID,[Item],[RID]) <- !mark_items(OrderID,Item,RID).
+!mark_items(OrderID,[IH|IT],[R1|R2]) <- !mark_items(OrderID,IH,R1); !mark_items(OrderID,IT,R2).

///////////////////////////// CHECK IF ANY ITEM HAS NOT BEEN RETRIEVED

@checking_all_retrieved[atomic]
+!check_missing(OrderID) : order(id(OrderID),status(_),_,_)
	<-  .findall(Q,retrieve(ID)[order(OrderID),_,_,remaining(Q)], L);
	    !all_zero(L);
	    -order(id(OrderID),_,ClientInfo,Items);
        +order(id(OrderID),status(completed),ClientInfo,Items);
        .println("picking ended").
-!check_missing(OrderID).
+!check_missing(OrderID).

+!all_zero([0]).
+!all_zero([0|T]) <- !all_zero(T). */