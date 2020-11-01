////////////////////////////////////////////////////// INFO ITEMS //////////////////////////////////////////////////////

@info_items[atomic]
+!kqml_received(Sender, achieve, info(warehouse), MsgID)            // send the warehouse state (items info & position)
    <-  .findall(Item[H|T], Item[source(self)|[H|T]], Items);
        .send(Sender, tell, Items, MsgID).