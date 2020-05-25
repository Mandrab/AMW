!setup.                                                             // setup

///////////////////////////// AGENT SETUP

+!setup : not set
	<-  .df_register("executor(item_picker)", "retrieve(item)");
        +state(available);
        +set.

+!kqml_received(Sender, cfp, retrieve(id(ID), item(ReshapedItem)), MsgID)
	<-  .println(aaaa);.send(Provider, propose, retrieve(id(ID), item(ReshapedItem))).