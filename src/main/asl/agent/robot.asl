/***********************************************************************************************************************
 Initial beliefs and rules
***********************************************************************************************************************/

set(false).                                                       // at start is not yet set
implements[ "cid0.0.0.2" ].                                         // implements send mail

/***********************************************************************************************************************
 Initial goals
***********************************************************************************************************************/

!setup.                                                             // setup

/***********************************************************************************************************************
 Plans
***********************************************************************************************************************/

///////////////////////////// AGENT SETUP

+!setup : set(false)
	<-  .df_register("executor(item_picker)", "retrieve(item)");  // register for pick items
		.df_register("executor(command)", "exec(command)");   // register for pick items
		.include("util/communication.asl");                      // include communication utils plans
		.include("util/literal.asl");                            // include communication utils plans
		+activity(default);                                       // setup default activity
        +state(available);                                        // set as available to achieve unordinary operations
        -+set(true);                                              // set process ended
		!work.                                                      // start working



///////////////////////////////////////////////////////// JOBS /////////////////////////////////////////////////////////

///////////////////////////// DEFAULT JOB

+!work : activity(default)                                          // only if achieving default activity
	<-  .println("Doing stuffs ...");
		.wait(1000);                                                // fake execution time
		!work.                                                      // restart to work

///////////////////////////// PICKING JOB

+!work
	:   activity(picking)[ client(Client), item(Item) ]       // only if picking
	<-  .println("Picking ...");
		.wait(1000);                                              // fake execution time
		.send(Client, complete, retrieve(Item));                // confirm task completion
        -+activity(default);                                      // setup default activity
        -+state(available);                                       // set as available to achieve unordinary operations
        !work.                                                      // restart to work

///////////////////////////// SCRIPT EXECUTION

+!work
	:   activity(executing)[ client(Client), script(Script) ] // only if executing script
	<-  !main[source(script)];                                  // run the main intention of the script
		!remove_plans(1);
		-+activity(default);                                      // at end, setup default activity
        -+state(available);                                       // set as available to achieve unordinary operations
        !work.                                                      // restart to work

+!work : activity(_).



///////////////////////////////////////////////////// ITEM PICKING /////////////////////////////////////////////////////

///////////////////////////// PICKING REQUEST:      OP #15 in order submission schema

+!kqml_received(Sender, cfp, Content, MsgID)                      // request of item picking
	:   Content = retrieve(id(Id), item(item(Item)[ [] | Positions ]))
    <-  !propose(Sender, retrieve(Id), MsgID);                      // propose to retrieve the item
        .wait(3000);                                              // max time to wait for confirm
        !check_acceptance(Sender, retrieve(Id)[ cause(request_timeout) ], MsgID).              // check if an acceptance has came

-!kqml_received(Sender, cfp, Content, MsgID)                      // failure of plan (e.g. when no available)
	:   Content = retrieve(id(Id), item(item(Item)[ [] | Positions ]))
	<-  .send(Sender, refuse, retrieve(Id)).                    // refuse to retrieve item

///////////////////////////// REFUSED PROPOSAL:     OP #21 in order submission schema

@client_reject[atomic]
+!kqml_received(Sender, reject_proposal, retrieve(Item), MsgID) // clients refuse proposal
	:   state(pending)
    <-  -+state(available).                                       // become available again

///////////////////////////// ACCEPTED PROPOSAL:    OP #24 in order submission schema

@client_accept[atomic]
+!kqml_received(Sender, accept_proposal, retrieve(Item), MsgID) // receive confirm of item picking
	:   state(pending)
    <-  -+state(unavailable);                                     // set as unavailable for tasks
        -activity(_);
        +activity(picking)[ client(Sender), item(Item) ].     // pick item for client



////////////////////////////////////////////////// SCRIPT EXECUTION //////////////////////////////////////////////////

///////////////////////////// JOB EXECUTION REQ:    OP #5 in command submission schema

+!kqml_received(Sender, cfp, Content, MsgID)                      // request of job execution
	:   Content = execute(script(S)[ [] | Requirements ])
	&   implements[ source(self) | Labels ] & action.implement(Labels, Requirements)
    <-  +script(S)[ client(Sender), msg_id(MsgID) ]
        !propose(Sender, execute(script(S)), MsgID)               // propose to exec the job
        .wait(3000)                                                 // max time to wait for confirm

        // check the proposal acceptance
        !check_acceptance(Sender, execute(script)[ cause(request_timeout) ], MsgID, script(S)).

// request of job execution
+!kqml_received(Sender, cfp, Content, MsgID) : Content = execute(script(S)[_])
    <-  .send(Sender, refuse, execute(script)[ err_code(407) ], MsgID).   // refuse to run the script

///////////////////////////// ACCEPTED PROPOSAL

@accept_execution_prop[atomic]
+!kqml_received(Sender, accept_proposal, execute(script), MsgID)          // receive confirm of item picking
	:   script(Script)[ client(Sender), msg_id(MsgID) ] & state(pending)
    <-  -+state(unavailable);                                               // set as unavailable for tasks
        -activity(_);
        action.labelize(Script, PlansString);
        .term2string(Plans, PlansString);
		.add_plan(Plans, script, begin);
        +activity(executing)[ client(Sender), script(Script) ].    // pick item for client



////////////////////////////////////////////////// COMMANDS EXECUTION //////////////////////////////////////////////////

///////////////////////////// COMMAND EXECUTION REQ:OP #5 in command submission schema

+!kqml_received(Sender, cfp, Content, MsgID)                      // request of job execution
	:   Content = execute(command_id(CommandID))
    <-  !change_status(state(available), pending, Result);
		if (Result) {
			+execution_request(command(CommandID), client(Sender), msg_id(MsgID))[ status(unaccepted) ];
			!random_agent("management(commands)", "request(command)", Agent);
			.term2string(CommandID, CID);
            .send(Agent, achieve, request(command_id(CID)));// require command's versions
            .wait(3000);                                             // max time to wait for confirm
            !command_request_timeout(CommandID, Sender, MsgID);
		} else { .send(Sender, refuse, execution(command_id(CommandID))[ err_code(503) ], MsgID); }.

/////////////////////

@command_request_timeout[atomic]
+!command_request_timeout(CommandID, Client, MsgID)
	:   execution_request(command(CommandID), client(Client), msg_id(MsgID))[ status(unaccepted) | [] ]
    <-  -execution_request(command(CommandID), client(Client), msg_id(MsgID));
        -+state(available);
        .send(Client, refuse, execute(command_id(CommandID))[ err_code(404) ], MsgID).

-!command_request_timeout(CommandID, Client, MsgID).

///////////////////////////// GET COMMAND INFOS

+!kqml_received(Sender, tell, Content, MsgID)          // receive confirm of item picking
	:   Content = command(CID)[ [ ] | Variants ]
	<-  !update_request(CommandID, unaccepted, pending, Result);
		if (Result) {
			!get_feasible(Variants, Feasible);
			!update_request(CommandID, pending, accepted, Feasible, R);
			!cfp_response(CommandID);
		}.

/////////////////////////////
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
	&   not .ground(Feasible)                                     // no script found
	<-  -execution_request(command(CID), client(Client), msg_id(MsgID));
		-+state(available);
		.send(Client, refuse, execute(command_id(CommandID))[ err_code(404) ], MsgID).

+!cfp_response(CID)
	:   execution_request(command(CID), client(Client), msg_id(MsgID))[ status(accepted), version(Feasible) ]
	<-  .send(Client, propose, execute(command_id(CID)), MsgID);    // propose to exec the job
        .wait(5000);                                      // TODO max time to wait for confirm
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
+!kqml_received(Sender, accept_proposal, Content, MsgID)          // receive confirm of item picking
	:   Content = execute(command_id(ID))
	&   state(pending)
    <-  ?execution_request(command(ID), client(Client), msg_id(M))[ status(accepted), version(S) ];
        -+state(unavailable);                                     // set as unavailable for tasks
        -activity(_);
        labelize(S, Labelized);
        .term2string(Script, Labelized);
        .add_plan(Script, script, begin);
        +activity(executing)[ client(Client), script(Labelized) ].     // pick item for client



/////////////////////////////////////////////// COMMANDS IMPLEMENTATIONS ///////////////////////////////////////////////

///////////////////////////// COMMAND 0.0.0.2

+!send(Email, Code, Msg)[ source("cid0.0.0.2") ]
	<-  send_feedback(Email, Code, Msg).

//////////////////////////////////////////////////// UTILITY PLANS /////////////////////////////////////////////////////

///////////////////////////// REMOVE SCRIPT'S PLANS

@remove_plans[atomic]
+!remove_plans(IDX)
	:   .concat(l, IDX, Label) & .term2string(TLabel, Label)
	&   .plan_label(P, TLabel)
	<-  .remove_plan(TLabel, script);
		!remove_plans(IDX + 1).

-!remove_plans(IDX).

///////////////////////////// ATOMIC STATUS CHANGE

@change_status[atomic]
+!change_status(Cond, Status, true) : Cond <- -+state(Status).

@change_status_fail[atomic]
-!change_status(Previous, Status, false).

///////////////////////////// PROPOSE FOR TASK

@propose[atomic]
+!propose(Sender, Msg, MsgID) : state(available)                    // propose for task (item picking or job execution)
	                                                                // if i'm not doing other unstoppable things
	<-  -+state(pending);                                           // update state to wait confirm
		.send(Sender, propose, Msg, MsgID).                         // propose to accept the work

@proposeFail[atomic]
-!propose(Sender, Msg, MsgID) <- .send(Sender, refuse, Msg, MsgID).

///////////////////////////// CHECK PROPOSAL RESPONSE

@accepted[atomic]
+!check_acceptance(Sender, Id, MsgID) : not state(pending).         // the propose has been accepted

@unaccepted[atomic]
+!check_acceptance(Sender, Msg, MsgID)                            // the propose hasn't been accepted
	:   state(pending)                                            // if i got no response ...
	<-  -+state(available);                                       // ... stop waiting and reset as available
        .send(Sender, failure, Msg, MsgID).                       // send timeout failure

@accepted_with_delete[atomic]
+!check_acceptance(Sender, Id, MsgID, Annot)                      // the propose has been accepted
	:   not state(pending).                                       // do nothing here

@unaccepted_with_delete[atomic]
+!check_acceptance(Sender, Msg, MsgID, Annot)                     // the propose hasn't been accepted
	:   state(pending)                                            // if i got no response ...
	<-  -+state(available);                                       // ... stop waiting and reset as available
		-Annot;                                                     // delete annotation
        .send(Sender, failure, Msg, MsgID).                       // send timeout failure