///////////////////////////////////////////////////// ITEM PICKING /////////////////////////////////////////////////////
//TODO remove item RESERVED
implements[ "item_picker" ].

{ include("propose.asl") }
{ include("check_acceptance.asl") }

///////////////////////////// PICKING REQUEST

+!kqml_received(Sender, cfp, Content, MsgID)                        // request of item picking
	:   Content = retrieve(id(Id), item(Item))
    <-  !propose(Sender, retrieve(Id), MsgID, Item);                // propose to retrieve the item
        .wait(3000);                                                // max time to wait for confirm
        !check_acceptance(Sender, retrieve(Id)[ cause(request_timeout) ], MsgID).   // check if an acceptance has came

-!kqml_received(Sender, cfp, Content, MsgID)                        // failure of plan (e.g. when no available)
	:   Content = retrieve(id(Id), item(item(Item)[ [] | Positions ]))
	<-  .send(Sender, refuse, retrieve(Id)).                        // refuse to retrieve item

///////////////////////////// REFUSED PROPOSAL

@client_reject[atomic]
+!kqml_received(Sender, reject_proposal, retrieve(Item), MsgID)     // clients refuse proposal
	:   state(pending)
    <-  -+state(available).                                         // become available again

///////////////////////////// ACCEPTED PROPOSAL

@client_accept[atomic]
+!kqml_received(Sender, accept_proposal, retrieve(ID), MsgID)       // receive confirm of item picking
	:   state(pending)[Item]
    <-  -+state(unavailable);                                       // set as unavailable for tasks
        -activity(_);
        +activity(picking)[client(Sender),item(id(ID),item(Item))]. // pick item for client

///////////////////////////// CONFIRM ITEM REMOVAL

+!kqml_received(Sender,confirm,remove(Item),MsgID).
+!kqml_received(Sender,failure,error(remove(ID)),MsgID) <- +error(remove(ID)).  // SEVERE should not happen

+!remove(item(ID),position(rack(R),shelf(S),quantity(_)))
    <-  !random_agent("management(items)","remove(item)",Provider);
        .send(Provider,achieve,remove_reserved(item(id(ID),position(rack(R),shelf(S),quantity(1)))));
        ? not error(remove(ID)).

-!remove(item(ID),position(rack(R),shelf(S),quantity(_))) <- !remove(item(ID),position(rack(R),shelf(S),quantity(1))).
