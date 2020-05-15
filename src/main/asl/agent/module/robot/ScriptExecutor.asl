////////////////////////////////////////////////// SCRIPT EXECUTION //////////////////////////////////////////////////

implements[ "script_executor" ].

{ include("Propose.asl") }
{ include("CheckAcceptance.asl") }
{ include("PlansRemover.asl") }

///////////////////////////// JOB EXECUTION REQ

+!kqml_received(Sender, cfp, Content, MsgID)                        // request of job execution
	:   Content = execute(script(S)[ [] | Requirements ])
	&   implements[ source(self) | Labels ] & action.implement(Labels, Requirements)
    <-  +script(S)[ client(Sender), msg_id(MsgID) ]
        !propose(Sender, execute(script(S)), MsgID)                 // propose to exec the job
        .wait(3000)                                                 // max time to wait for confirm
        !check_acceptance(Sender, execute(script)[ cause(request_timeout) ], MsgID, script(S)). // check the proposal
                                                                    // acceptance

+!kqml_received(Sender, cfp, Content, MsgID) : Content = execute(script(S)[_])  // request of job execution
    <-  .send(Sender, refuse, execute(script)[ err_code(407) ], MsgID). // refuse to run the script

///////////////////////////// ACCEPTED PROPOSAL

@accept_execution_prop[atomic]
+!kqml_received(Sender, accept_proposal, execute(script), MsgID)    // receive confirm of item picking
	:   script(Script)[ client(Sender), msg_id(MsgID) ] & state(pending)
    <-  -+state(unavailable);                                       // set as unavailable for tasks
        -activity(_);
        action.labelize(Script, PlansString);
        .term2string(Plans, PlansString);
		.add_plan(Plans, script, begin);
        +activity(executing)[ client(Sender), script(Script) ].     // pick item for client