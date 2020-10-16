available.

!setup.

+!setup : not set <- .df_register("executor(item_picker)", "retrieve(item)"); +set.

+!kqml_received(Sender,cfp,retrieve(id(ID),item(ReshapedItem)),MsgID) : available
    <-  -available
        .send(Sender,propose,retrieve(ID));
        .wait(1000);
        +available.
+!kqml_received(Sender,cfp,retrieve(id(ID),item(ReshapedItem)),MsgID) <- .send(Sender,refuse,retrieve(ID)).
+!kqml_received(Sender,accept_proposal,retrieve(ID),MsgID) <- .wait(500); .send(Sender,complete,retrieve(ID)).