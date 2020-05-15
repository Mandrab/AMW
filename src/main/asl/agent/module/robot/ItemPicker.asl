///////////////////////////////////////////////////// ITEM PICKING /////////////////////////////////////////////////////

implements[ "item_picker" ].

{ include("Propose.asl") }
{ include("CheckAcceptance.asl") }

///////////////////////////// PICKING REQUEST

+!kqml_received(Sender, cfp, Content, MsgID)                        // request of item picking
	:   Content = retrieve(id(Id), item(item(Item)[ [] | Positions ]))
    <-  !propose(Sender, retrieve(Id), MsgID);                      // propose to retrieve the item
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
+!kqml_received(Sender, accept_proposal, retrieve(Item), MsgID)     // receive confirm of item picking
	:   state(pending)
    <-  -+state(unavailable);                                       // set as unavailable for tasks
        -activity(_);
        +activity(picking)[ client(Sender), item(Item) ].           // pick item for client