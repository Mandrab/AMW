////////////////////////////////////////////////// COMMANDS EXECUTION //////////////////////////////////////////////////

// TODO implement comfirmation (with caching) in order manager
+!kqml_received(Sender, confirm, command(ID)[mid(MID)], _)          // confirmation of message reception
    <-  .println("[ROBOT PICKER] execution confirmation received by applicant");
        !response_received(MID).

+!kqml_received(Sender, achieve, command(ID), MID)                  // request of job execution
    <-  .println("[ROBOT PICKER] request command execution");
        !set_execute(command(ID), Sender, MID);
        !ensure_send(
            description("management(commands)", "info(commands)"),
            achieve, command(ID)
        ).

+!kqml_received(Sender, tell, script(S)[mid(MID)], _)
    <-  .println("[ROBOT PICKER] command script obtained");
        !response_received(MID);
        !execute(S).

//////////////////////////////////////////////////// UTILITY PLANS /////////////////////////////////////////////////////

@setExecutionTask[atomic]
+!set_execute(Command, Sender, MID)
    :   not pick(_, _)                                              // not already picking and ...
    &   not execute(_)                                              // not already executing something
    <-  +execute(Command)[client(Sender), mid(MID)].                // set pick target data

+!execute(Script)
    :   execute(Command)[client(Client), mid(MID)]
	<-  .term2string(TScript, Script);
        .add_plan(TScript, script, begin);
	    !main[source(script)];                                      // run the main intention of the script
        !remove_plans(0);                                           // remove all plans with label in the form of "lN"
	    -execute(Command);                                          // task completed
		!ensure_send(
		    Client,
		    confirm, Command,                                       // confirm task completion
		    MID
        ).                                                          // msgid is unique from order_manager

@remove_plans[atomic]
+!remove_plans(IDX)
	:   .concat(l, IDX, Label) & .term2string(TLabel, Label)
	&   .plan_label(P, TLabel)
	<-  .remove_plan(TLabel, script);
		!remove_plans(IDX + 1).

-!remove_plans(IDX).
