//////////////////////////////////////////////////// COMMANDS INFO /////////////////////////////////////////////////////

@commandRequest[atomic]
+!kqml_received(Sender, achieve, command(ID), _)
	:   command(ID, _, _)[ script(S) ]
    <-  .println("[COMMAND MANAGER] request command script");
        .send(Sender, tell, script(S)[mid(MID)]).

@commandsRequest[atomic]
+!kqml_received(Sender, achieve, info(commands), _)
	<-  .println("[COMMAND MANAGER] request commands info");
	    .findall(command(ID, N, D)[ script(S) ], command(ID, N, D)[ script(S) ], L);
        .send(Sender, tell, L[mid(MID)]).
