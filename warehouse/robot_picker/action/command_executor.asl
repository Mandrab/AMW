////////////////////////////////////////////////// COMMANDS EXECUTION //////////////////////////////////////////////////

{ include("check_acceptance.asl") }
{ include("plans_remover.asl") }
{ include("utilities/communication.asl") }                          // include communication utils plans
{ include("usable_version_finder.asl") }

///////////////////////////// COMMAND EXECUTION

+!kqml_received(Sender, cfp, execute(command_id(CommandID)), MsgID) // request of job execution
    <-  .println("Request to execute a command");
        !change_status(state(available), pending);
        .term2string(CommandID, CID);
        +execution_request(command(CID), client(Sender), msg_id(MsgID))[ status(unaccepted) ];
        !random_agent("management(commands)", "request(command)", Agent);
        .send(Agent, achieve, request(command_id(CommandID)));        // require command's versions
        .wait(3000);                                            // max time to wait for confirm
        !command_request_timeout(CID, Sender, MsgID).

-!kqml_received(Sender, cfp, Content, MsgID)
    <-  .send(Sender, refuse, execution(command_id(CommandID))[ err_code(503) ], MsgID).

/////////////////////////////

@command_request_timeout[atomic]
+!command_request_timeout(CommandID, Client, MsgID)
	:   execution_request(command(CommandID), client(Client), msg_id(MsgID))[ status(unaccepted) |_]
    <-  -execution_request(command(CommandID), client(Client), msg_id(MsgID));
        -+state(available);
        .send(Client, refuse, execute(command_id(CommandID))[ err_code(404) ], MsgID).

-!command_request_timeout(CommandID, Client, MsgID).// TODO

///////////////////////////// GET COMMAND INFO

+!kqml_received(Sender, tell, Content, MsgID)                       // receive confirm of item picking
	:   Content = command(CID)[ [ ] | Variants ]
	<-  .println("Received command's variants");
	    .term2string(CID, CommandID);
	    !update_request(CommandID, unaccepted, pending);
        !get_feasible(Variants, Feasible);
        !update_request(CommandID, pending, accepted, Feasible);
        !cfp_response(CommandID).

/////////////////////////////
//TODO ??????????????????????????????????????????????????
+!kqml_received(Sender, failure, request(command_id(ID)), MsgID) <- !change_status(state(pending), available).

///////////////////////////// EXECUTION REQUEST ACCEPTANCE

@add_request[atomic]
+!add_request(CommandID, Client, MsgID, true)
	:   not execution_request(command(CommandID), client(Client), msg_id(MsgID))
	<-  +execution_request(command(CommandID), client(Client), msg_id(MsgID))[ status(unaccepted) ].

@add_request_fail[atomic]
-!add_request(CommandID, Client, MsgID, false).

///////////////////////////// EXECUTION REQUEST STATUS UPDATE

@update_status_request[atomic]
+!update_request(CommandID, OldStatus, Status)
	:   execution_request(command(CommandID), client(Client), msg_id(MsgID))[ status(OldStatus) ]
	<-  .println(CommandID);-execution_request(command(CommandID), client(Client), msg_id(MsgID))[ status(OldStatus) ];
		+execution_request(command(CommandID), client(Client), msg_id(MsgID))[ status(Status) ].

///////////////////////////// EXECUTION REQUEST VERSION UPDATE

@update_version_request[atomic]
+!update_request(CommandID, OldStatus, Status, Version)
	:   execution_request(command(CommandID), client(Client), msg_id(MsgID))[ status(OldStatus) ]
	<-  -execution_request(command(CommandID), client(Client), msg_id(MsgID))[ status(OldStatus) ];
		+execution_request(command(CommandID), client(Client), msg_id(MsgID))[ status(Status), version(Version) ].

///////////////////////////// RESPONSE TO CLIENT

@cfp_response[atomic]
+!cfp_response(CommandID)
	:   execution_request(command(CommandID), client(Client), msg_id(MsgID))[ status(accepted), version(Feasible) ]
	&   not .ground(Feasible)                                       // no script found
	<-  .term2string(CID, CommandID);
	    -execution_request(command(CommandID), client(Client), msg_id(MsgID));
		-+state(available);
		.send(Client, refuse, execute(command_id(CID))[ err_code(404) ], MsgID).

+!cfp_response(CommandID)
	:   execution_request(command(CommandID), client(Client), msg_id(MsgID))[ status(accepted), version(Feasible) ]
	<-  .term2string(CID, CommandID);
	    .send(Client, propose, execute(command_id(CID)), MsgID);    // propose to exec the job
        .wait(5000);                                                // TODO max time to wait for confirm
        !check_acceptance(Client, execute(command_id(CommandID))[ cause(request_timeout) ], MsgID,
                execution_request(command(CommandID), client(Client), msg_id(MsgID))[ status(accepted), version(Feasible) ]).             // check the proposal acceptance

///////////////////////////// ACCEPTED PROPOSAL

@accept_execution_propTODO2[atomic]
+!kqml_received(Sender, accept_proposal, Content, MsgID)            // receive confirm of item picking
	:   Content = execute(command_id(ID))
	&   state(pending)
    <-  .println("Accepted proposal of execution");
        .term2string(ID, CommandID);
        ?execution_request(command(CommandID), client(Client), msg_id(M))[ status(accepted), version(Script) ];
        -+state(unavailable);                                       // set as unavailable for tasks
        -activity(_);
        .term2string(ScriptTerm, Script);
        .add_plan(ScriptTerm, script, begin);
        +activity(executing)[ client(Client), script(Labelized) ].  // pick item for client

//////////////////////////////////////////////////// UTILITY PLANS /////////////////////////////////////////////////////

///////////////////////////// ATOMIC STATUS CHANGE

@change_status[atomic]
+!change_status(Cond, Status) : Cond <- -+state(Status).
