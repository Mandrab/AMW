////////////////////////////////////////////////// COMMANDS EXECUTION //////////////////////////////////////////////////

{ include("CheckAcceptance.asl") }
{ include("PlansRemover.asl") }
{ include("util/communication.asl") }                               // include communication utils plans

///////////////////////////// COMMAND EXECUTION

+!kqml_received(Sender, cfp, Content, MsgID)                        // request of job execution
	:   Content = execute(command_id(CommandID))
    <-  !change_status(state(available), pending, Result);
		if (Result) {
			+execution_request(command(CommandID), client(Sender), msg_id(MsgID))[ status(unaccepted) ];
			!random_agent("management(commands)", "request(command)", Agent);
			.term2string(CommandID, CID);
            .send(Agent, achieve, request(command_id(CID)));        // require command's versions
            .wait(3000);                                            // max time to wait for confirm
            !command_request_timeout(CommandID, Sender, MsgID);
		} else { .send(Sender, refuse, execution(command_id(CommandID))[ err_code(503) ], MsgID); }.

/////////////////////////////

@command_request_timeout[atomic]
+!command_request_timeout(CommandID, Client, MsgID)
	:   execution_request(command(CommandID), client(Client), msg_id(MsgID))[ status(unaccepted) | [] ]
    <-  -execution_request(command(CommandID), client(Client), msg_id(MsgID));
        -+state(available);
        .send(Client, refuse, execute(command_id(CommandID))[ err_code(404) ], MsgID).

-!command_request_timeout(CommandID, Client, MsgID).

///////////////////////////// GET COMMAND INFO

+!kqml_received(Sender, tell, Content, MsgID)                       // receive confirm of item picking
	:   Content = command(CID)[ [ ] | Variants ]
	<-  !update_request(CommandID, unaccepted, pending, Result);
		if (Result) {
			!get_feasible(Variants, Feasible);
			!update_request(CommandID, pending, accepted, Feasible, R);
			!cfp_response(CommandID);
		}.

/////////////////////////////
//??????????????????????????????????????????????????
+!kqml_received(Sender, failure, Content, MsgID) <- !change_status(state(pending), available, Result).

///////////////////////////// EXECUTION REQUEST ACCEPTANCE

@add_request[atomic]
+!add_request(CommandID, Client, MsgID, true)
	:   not execution_request(command(CommandID), client(Client), msg_id(MsgID))
	<-  +execution_request(command(CommandID), client(Client), msg_id(MsgID))[ status(unaccepted) ].

@add_request_fail[atomic]
-!add_request(CommandID, Client, MsgID, false).

///////////////////////////// EXECUTION REQUEST STATUS UPDATE

@update_status_request[atomic]
+!update_request(CommandID, OldStatus, Status, true)
	:   execution_request(command(CommandID), client(Client), msg_id(MsgID))[ status(OldStatus) ]
	<-  -execution_request(command(CommandID), client(Client), msg_id(MsgID))[ status(OldStatus) ];
		+execution_request(command(CommandID), client(Client), msg_id(MsgID))[ status(Status) ].

@update_status_request_fail[atomic]
-!update_request(CommandID, OldStatus, Status, false).

///////////////////////////// EXECUTION REQUEST VERSION UPDATE

@update_version_request[atomic]
+!update_request(CommandID, OldStatus, Status, Version, true)
	:   execution_request(command(CommandID), client(Client), msg_id(MsgID))[ status(OldStatus) ]
	<-  -execution_request(command(CommandID), client(Client), msg_id(MsgID))[ status(OldStatus) ];
		+execution_request(command(CommandID), client(Client), msg_id(MsgID))[ status(Status), version(Version) ].

@update_version_request_fail[atomic]
-!update_request(CommandID, OldStatus, Status, Version, false).

///////////////////////////// RESPONSE TO CLIENT

@cfp_response[atomic]
+!cfp_response(CID)
	:   execution_request(command(CID), client(Client), msg_id(MsgID))[ status(accepted), version(Feasible) ]
	&   not .ground(Feasible)                                       // no script found
	<-  -execution_request(command(CID), client(Client), msg_id(MsgID));
		-+state(available);
		.send(Client, refuse, execute(command_id(CommandID))[ err_code(404) ], MsgID).

+!cfp_response(CID)
	:   execution_request(command(CID), client(Client), msg_id(MsgID))[ status(accepted), version(Feasible) ]
	<-  .send(Client, propose, execute(command_id(CID)), MsgID);    // propose to exec the job
        .wait(5000);                                                // TODO max time to wait for confirm
        !check_acceptance(Client, execute(command_id(CID))[ cause(request_timeout) ], MsgID,
                execution_request(command(CID), client(Client), msg_id(MsgID))[ status(accepted), version(Feasible) ]).             // check the proposal acceptance

+!get_feasible([ Head | [] ], Feasible)
	:   Head = variant(v_id(ID), requirements[ [] | Requirements ], script(Script))
	&   implements[ source(self) | Labels ]
	<-  !provided(Requirements, Labels, Provided);
		if (Provided) { Feasible = Script; }.

+!get_feasible([ Head | Tail ], Feasible)
	<-  !get_feasible([ Head ], Feasible);
		if (not .ground(Feasible)) { !get_feasible(Tail, Feasible); }.

+!provided(Requirements, Label, true) : not .ground(Requirements) | .empty(Requirements).

+!provided([ Req | [] ], [ Label | [] ], true) : Req == Label.

+!provided([ Req | [] ], [ Label | [] ], false).

+!provided([ Req | [] ], [ Label | Tail ], Result)
	<-  !provided([ Req ], [ Label ], HeadRes);
		!provided([ Req ], Tail, TailRes);
		.eval(Result, HeadRes | TailRes).

+!provided([ Req | RTail ], Labels, Result)
	<-  !provided([ Req ], Labels, HeadRes);
		!provided(RTail, Labels, TailRes);
		.eval(Result, HeadRes & TailRes).

///////////////////////////// ACCEPTED PROPOSAL

@accept_execution_propTODO2[atomic]
+!kqml_received(Sender, accept_proposal, Content, MsgID)            // receive confirm of item picking
	:   Content = execute(command_id(ID))
	&   state(pending)
    <-  ?execution_request(command(ID), client(Client), msg_id(M))[ status(accepted), version(S) ];
        -+state(unavailable);                                       // set as unavailable for tasks
        -activity(_);
        labelize(S, Labelized);
        .term2string(Script, Labelized);
        .add_plan(Script, script, begin);
        +activity(executing)[ client(Client), script(Labelized) ].  // pick item for client

//////////////////////////////////////////////////// UTILITY PLANS /////////////////////////////////////////////////////

///////////////////////////// ATOMIC STATUS CHANGE

@change_status[atomic]
+!change_status(Cond, Status, true) : Cond <- -+state(Status).

@change_status_fail[atomic]
-!change_status(Previous, Status, false).
