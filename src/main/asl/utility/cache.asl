//////////////////////////////////////////////// CACHE MESSAGE RESPONSE ////////////////////////////////////////////////

///////////////////////////// COMMUNICATION WITH JADE AGENTS

+!cached_response(Sender, in(IP, IM, ID), out(OP, OM, OD))
    <-  +cached_message(Sender, in(IP, IM, ID), out(OP, OM, OD));   // save the response to this message
        .send(Sender, OP, OM, OD).                                  // send the response

+!kqml_received(Sender, IP, IM, ID)
    :   cached_message(Sender, in(IP, IM, ID), out(OP, OM, OD))     // a response to this message has already been sent
    <-  .send(Sender, OP, OM, OD).                                  // resend the previous cached response

///////////////////////////// COMMUNICATION WITH ASL AGENTS

+!cached_response(Sender, in(IP, IM), out(OP, OM))
    <-  +cached_message(Sender, in(IP, IM), out(OP, OM));           // save the response to this message
        .send(Sender, OP, OM).                                      // send the response

+!kqml_received(Sender, IP, IM[mid(MID)], _)
    :   cached_message(Sender, in(IP, IM[mid(MID)]), out(OP, OM))   // a response to this message has already been sent
    <-  .send(Sender, OP, OM).                                      // resend the previous cached response

+!kqml_received(Sender, IP, IM[mid(MID)|L], _)
    :   cached_message(Sender, in(IP, IM[mid(MID)|L]), out(OP, OM)) // a response to this message has already been sent
    <-  .send(Sender, OP, OM).                                      // resend the previous cached response

+!kqml_received(Sender, IP, IM, MID)
    :   cached_message(Sender, in(IP, IM[mid(MID)]), out(OP, OM))   // a response to this message has already been sent
    <-  .send(Sender, OP, OM).                                      // resend the previous cached response
