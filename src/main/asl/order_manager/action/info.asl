////////////////////////////////////////////////////// ORDER INFO //////////////////////////////////////////////////////

+!kqml_received(Sender, achieve, info(Client, Email), MID)
    <-  .println("[ORDER MANAGER] required orders info")
        .findall(order(ID, S), order(ID, S, user(Client, Email, _)), L)
        .send(Sender, tell, L, MID).
