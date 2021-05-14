/////////////////////////////////////////////////// COMMAND ADDITION ///////////////////////////////////////////////////

@commandAddition[atomic]
+!kqml_received(Sender, achieve, add(Command), _)
	:   Command = command(ID, _, _)[ script(_) ]
	&   not command(ID, _, _)
	<-  .println("[COMMAND MANAGER] request command addition");
	    +Command;
	    .send(Sender, confirm, add(Command)[mid(MID)]).
