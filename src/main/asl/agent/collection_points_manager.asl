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

+!setup : not set
	<-  .df_register("management(items)", "info(collection_points)"); // register as collection points dispatcher
		+set.                                              // set process ended

/////////////////////////////////////////////// COLLECTION POINT REQUEST ///////////////////////////////////////////////

///////////////////////////// COLLECTION POINT REQUEST

+!kqml_received(Sender, cfp, point(OrderID), MsgID)
	<-  !reserve(OrderID, X, Y);
	    .send(Sender, propose, point(OrderID)[X, Y], MsgID);
        .wait(3000);
        !check_accepted(OrderID).

-!kqml_received(Sender, cfp, point(OrderID), MsgID)
    <-  .send(Sender, failure, point(OrderID), MsgID);
        !free(OrderID).

///////////////////////////// POINT ACCEPTED

@accept_proposal[atomic]
+!kqml_received(_, accept_proposal, point(OrderID), _)
	<-  -+point(ID)[X, Y, state(reserved)[OrderID]].

@free_point[atomic]
+!kqml_received(_, tell, free(OrderID), _) <- !free(OrderID).   // TODO confirm?

//////////////////////////////////////////////////// UTILITY PLANS /////////////////////////////////////////////////////

///////////////////////////// PROPOSE POINT

@propose[atomic]
+!reserve(OrderID, X, Y) : point(PointID)[X, Y, state(available)]
	<-  -+point(PointID)[X, Y, state(pending)[OrderID]].

@free[atomic]
+!free(OrderID) : point(PointID)[_, _, state(pending)[OrderID]]
    <-  -+point(PointID)[X, Y, state(available)].

@accepted_proposal[atomic]
+!check_accepted(OrderID) : point(_)[_, _, state(reserved)[OrderID]].