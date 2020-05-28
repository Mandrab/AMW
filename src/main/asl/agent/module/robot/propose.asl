///////////////////////////// PROPOSE FOR TASK

@propose_and_save_info[atomic]
+!propose(Sender, Msg, MsgID, Info) : state(available)              // propose for task (item picking or job execution)
	<-  -state(available);
	    +state(pending)[Info];                                      // update state to wait confirm
		.send(Sender, propose, Msg, MsgID).                         // propose to accept the work

-!propose(Sender, Msg, MsgID, Info) <- .send(Sender, refuse, Msg, MsgID).

@propose[atomic]
+!propose(Sender, Msg, MsgID) : state(available)                    // propose for task (item picking or job execution)
	<-  -+state(pending);                                           // update state to wait confirm
		.send(Sender, propose, Msg, MsgID).                         // propose to accept the work

-!propose(Sender, Msg, MsgID) <- .send(Sender, refuse, Msg, MsgID).