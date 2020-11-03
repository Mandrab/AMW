////////////////////////////////////////////////////// ORDER INFO //////////////////////////////////////////////////////

+!kqml_received(Sender, achieve, info(Client, Email), MID)
    <-  .println("required orders info");
        .findall(order(ID, S), order(ID, S, user(Client, _, Email)), L);
        .send(Sender, tell, R, MID).
