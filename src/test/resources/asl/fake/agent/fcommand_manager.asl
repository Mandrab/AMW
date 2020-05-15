set(false).

command(id("Command1"), name("command 1 name"), description("descr command 1")) [
		variant(v_id("vid0.0.0.1"), requirements[], script(
				"[{@l1 +!main <- .println('Executing script ...');.wait(500);!b}, {@l2 +!b <- .println('Script executed')}]")) ].

!setup.

+!setup : set(false)
	<-  .df_register("management(commands)", "request(command)");
	    .include("util/literal.asl");
		-+set(true).

+!kqml_received(Sender, achieve, Content, MsgID)                  // send the warehouse state (items info & position)
	:   Content = request(command_id(CommandID))
	&   command(id(CommandID), name(N), description(D))[ source(self) | Variants ]
    <-  !concat(command(CommandID), Variants, Msg);
        .send(Sender, tell, Msg, MsgID).                          // ask if able to run the specified script

+!kqml_received(Sender, achieve, Content, MsgID) <- .send(Sender, failure, Content, MsgID).