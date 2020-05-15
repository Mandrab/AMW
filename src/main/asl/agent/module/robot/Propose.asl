///////////////////////////// PROPOSE FOR TASK

@propose[atomic]
+!propose(Sender, Msg, MsgID) : state(available)                    // propose for task (item picking or job execution)
	<-  -+state(pending);                                           // update state to wait confirm
		.send(Sender, propose, Msg, MsgID).                         // propose to accept the work

@propose_fail[atomic]
-!propose(Sender, Msg, MsgID) <- .send(Sender, refuse, Msg, MsgID).