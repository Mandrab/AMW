!setup.

+!setup : not set <- .df_register("executor(item_picker)", "retrieve(item)"); +set.

+!kqml_received(Sender,cfp,retrieve(id(ID),item(ReshapedItem)),MsgID) <- .send(Sender,propose,retrieve(ID)).
+!kqml_received(Sender,accept_proposal,retrieve(ID),MsgID) <- .wait(1000); .send(Sender,complete,retrieve(ID)).