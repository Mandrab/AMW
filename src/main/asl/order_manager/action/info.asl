////////////////////////////////////////////////////// ORDER INFO //////////////////////////////////////////////////////

+!kqml_received(Sender, achieve, info(Client, Email), MID)          // request for orders information
    <-  .println("[ORDER MANAGER] required orders info");
        .findall(order(ID, S), order(ID, S, user(Client, Email, _)), L1);   // orders to be gathered
        .findall(order(ID, S), order(ID, S, user(Client, Email, _), _), L2);// orders that have a collection point
        .concat(L1, L2, L);                                         // join all found orders information
        .send(Sender, tell, L, MID).
