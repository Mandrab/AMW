//////////////////////////////////////////////// CACHE MESSAGE RESPONSE ////////////////////////////////////////////////

+!cached_response(Sender, in(IP, IM, II), out(OP, OM, OI))
    <-  +cached_message(Sender, in(IP, IM, II), out(OP, OM, OI));   // save the response to this message
        .send(Sender, OP, OM, OI).                                  // send the response

+!kqml_received(Sender, IP, IM, II)
    :   cached_message(Sender, in(IP, IM, II), out(OP, OM, OI))     // a response to this message has already been sent
    <-  .send(Sender, OP, OM, OI).                                  // resend the previous cached response
