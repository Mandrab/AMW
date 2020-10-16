///////////////////////////// CHECK PROPOSAL RESPONSE (TIMEOUT CONTROL)

@accepted[atomic]
+!check_acceptance(Sender, ID, MsgID) : not state(pending).         // the propose has been accepted

@unaccepted[atomic]
+!check_acceptance(Sender, Msg, MsgID) : state(pending)             // the propose hasn't been accepted
	<-  -+state(available);                                         // stop waiting and reset as available
        .send(Sender, failure, Msg, MsgID).                         // send timeout failure

@accepted_and_delete[atomic]
+!check_acceptance(Sender, ID, MsgID, Annot) : not state(pending).  // the propose has been accepted

@unaccepted_and_delete[atomic]
+!check_acceptance(Sender, Msg, MsgID, Annot) : state(pending)      // the propose hasn't been accepted (no response).
	<-  -+state(available);                                         // stop waiting and reset as available
		-Annot;                                                     // delete annotation
        .send(Sender, failure, Msg, MsgID).                         // send timeout failure