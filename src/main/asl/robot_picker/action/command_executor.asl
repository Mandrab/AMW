////////////////////////////////////////////////// COMMANDS EXECUTION //////////////////////////////////////////////////

+!kqml_received(Sender, achieve, command(ID), MID)                  // request of job execution
    <-  .println("[ROBOT PICKER] request command execution");
        !set_execute(command(ID), Sender, MID);                     // set execution task
        !ensure_send(
            description("management(commands)", "info(commands)"),
            achieve, command(ID)                                    // ask for command script
        ).

+!kqml_received(Sender, tell, script(S)[mid(MID)], _)               // receive command script
    <-  .println("[ROBOT PICKER] command script obtained");
        !response_received(MID);                                    // set message as responded (ensure_send)
        !execute(S).                                                // execute script

-!kqml_received(_, tell, script(S), _)                              // execution failed
    :   execute(Command)[client(Client), mid(MID)]
    <-  .println("[ROBOT PICKER] error executing script");
        .send(Client, failure, Command, MID).

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
	    !cached_response(
	        Client,
            in(achieve, Command, MID),
            out(confirm, Command, MID)                              // confirm task completion
	    ).

@remove_plans[atomic]
+!remove_plans(IDX)                                                 // remove plans from specified
	:   .concat(l, IDX, Label) & .term2string(TLabel, Label)        // remove plans from this idx
	&   .plan_label(P, TLabel)                                      // plan with this label exists
	<-  .remove_plan(TLabel, script);                               // remove plan with specified label
		!remove_plans(IDX + 1).                                     // remove next plan

-!remove_plans(IDX).                                                // no more plans to remove
