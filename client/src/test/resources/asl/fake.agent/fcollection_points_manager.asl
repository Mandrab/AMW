!setup.

+!setup : not set <- .df_register("management(items)", "info(collection_points)"); +set.

+!kqml_received(Sender,cfp,point(OrderID),MsgID)
	<-  +point(1)[x(1),y(1),state(pending),by(OrderID)];
	    .send(Sender,propose,point(OrderID)[x(1),y(1)],MsgID);
        .wait(3000);
        ? not point(1)[x(1),y(1),state(pending),by(OrderID)].

-!kqml_received(Sender,cfp,point(OrderID),MsgID) <- .println("ERROR!! NO CONFIRM OBTAINED BY ORDER MANAGER").

@accept_proposal[atomic]
+!kqml_received(_,accept_proposal,point(OrderID),_) : point(PointID)[x(X),y(Y),state(pending),by(OrderID)]
	<-  -point(PointID); +point(PointID)[x(X),y(Y),state(reserved),by(OrderID)].

@free_point[atomic]
+!kqml_received(Sender,tell,free(OrderID),MsgID) <- .send(Sender,confirm,free(OrderID),MsgID).   // TODO confirm?