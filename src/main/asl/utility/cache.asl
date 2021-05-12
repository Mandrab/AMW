//////////////////////////////////////////////// CACHE MESSAGE RESPONSE ////////////////////////////////////////////////

+!cached_response(Sender, in(IP, IM, II), out(OP, OM))
    <-  +cached_message(Sender, in(IP, IM, II), out(OP, OM));       // save the response to this message
        .send(Sender, OP, OM).                                      // send the response

+!kqml_received(Sender, IP, IM, II)
    :   cached_message(Sender, in(IP, IM, II), out(OP, OM))         // a response to this message has already been sent
    <-  .send(Sender, OP, OM).                                      // resend the previous cached response
