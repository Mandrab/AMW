//////////////////////////////////////////////////// COMMANDS INFO /////////////////////////////////////////////////////

@commandRequest[atomic]
+!kqml_received(Sender, achieve, command(ID)[mid(MID)], _)          // request for command information
	:   command(ID, _, _)[ script(S) ]
    <-  .println("[COMMAND MANAGER] request command script");
        .send(Sender, tell, script(S)[mid(MID)]).

@commandsRequest[atomic]
+!kqml_received(Sender, achieve, info(commands), MID)               // request for commands information
	<-  .println("[COMMAND MANAGER] request commands info");
	    .findall(command(I, N, D)[ script(S) ], command(I, N, D)[ script(S) ], L);
        .send(Sender, tell, L, MID).
