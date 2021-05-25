/////////////////////////////////////////////////// COMMAND ADDITION ///////////////////////////////////////////////////

@commandAddition[atomic]
+!kqml_received(Sender, achieve, add(Command), MID)                 // require command addition
	:   Command = command(ID, _, _)[ script(_) ]
	&   not command(ID, _, _)
	<-  .println("[COMMAND MANAGER] request command addition");
	    +Command;
	    !cached_response(
	        Sender,
	        in(achieve, add(Command)[mid(MID)]),
	        out(confirm, add(Command)[mid(MID)])
        ).                                                          // cache confirmation and send
