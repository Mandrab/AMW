{ include("literal.asl") }                                          // include utilities for works on literals

!setup.                                                             // setup

+!setup : not set <- .df_register("management(items)", "retrieve(item)"); +set.

@processOrder[atomic]
+!kqml_received(Sender, achieve, Content, MsgID)
    :   Content = retrieve(order_id(OrderID))[[] | Items]
	<-  !concat(order_id(OrderID), [ item(id("Item1"))[position(rack(5), shelf(3), quantity(5)), position(rack(5), shelf(2), quantity(8)), position(rack(6), shelf(3), quantity(7))], item(id("Item2")) [position(rack(2), shelf(4), quantity(1))], item(id("Item3")) [position(rack(2), shelf(4), quantity(1))] ], M);
	    .send(Sender, confirm, M, MsgID).

-!kqml_received(Sender, achieve, retrieve(order_id(OrderID))[_], MsgID)
    <-  .send(Sender, failure, order_id(OrderID), MsgID).