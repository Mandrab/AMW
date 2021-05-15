////////////////////////////////////////////////////// INFO ITEMS //////////////////////////////////////////////////////

@info_items[atomic]
+!kqml_received(Sender, achieve, info(warehouse), MID)              // send the warehouse state (items info & position)
    <-  .println("[WAREHOUSE MAPPER] required warehouse info");
        .findall(item(X)[H|T], item(X)[source(self)|[H|T]], Items);
        .send(Sender, tell, Items, MID).
