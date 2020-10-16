/***********************************************************************************************************************
 Pre-Processing Directives
 **********************************************************************************************************************/

{ include("state/collection_points.asl") }

/***********************************************************************************************************************
 Initial goals
 **********************************************************************************************************************/

!setup.                                                             // setup

/***********************************************************************************************************************
 Plans
 **********************************************************************************************************************/

///////////////////////////// AGENT SETUP

+!setup : not set <- .df_register("management(items)", "info(collection_points)"); +set.

/////////////////////////////////////////////// COLLECTION POINT REQUEST ///////////////////////////////////////////////

///////////////////////////// COLLECTION POINT REQUEST

+!kqml_received(Sender, cfp, point(OrderID), MsgID)
	<-  !reserve(OrderID,X,Y);
	    .send(Sender, propose, point(OrderID)[x(X),y(Y)], MsgID);
        .wait(3000);
        !check_accepted(Sender, OrderID, MsgID).

-!kqml_received(Sender, cfp, point(OrderID), MsgID) <- .send(Sender, refuse, point(OrderID), MsgID).

///////////////////////////// POINT ACCEPTED

@accept_proposal[atomic]
+!kqml_received(_,accept_proposal,point(OrderID),_) : point(PointID)[x(X),y(Y),state(pending),by(OrderID)]
	<-  -point(PointID); +point(PointID)[x(X),y(Y),state(reserved),by(OrderID)].

@free_point[atomic]
+!kqml_received(Sender, tell, free(OrderID), MsgID) <- !free(OrderID); .send(Sender, confirm, free(OrderID), MsgID).   // TODO confirm?

//////////////////////////////////////////////////// UTILITY PLANS /////////////////////////////////////////////////////

///////////////////////////// PROPOSE POINT

@propose[atomic]
+!reserve(OrderID, X, Y) : point(PointID)[x(X),y(Y),state(available)]
	<-  -point(PointID); +point(PointID)[x(X),y(Y),state(pending),by(OrderID)].

@free[atomic]
+!free(OrderID) : point(PointID)[x(X),y(Y),by(OrderID)]
    <-  -point(PointID); +point(PointID)[x(X),y(Y),state(available)].

@accepted_proposal[atomic] +!check_accepted(_,OrderID,_) : point(PointID)[state(reserved),by(OrderID)].
@timeout_proposal[atomic] +!check_accepted(Sender, OrderID, MsgID)
    <-  !free(OrderID);
        .send(Sender, failure, point(OrderID), MsgID).