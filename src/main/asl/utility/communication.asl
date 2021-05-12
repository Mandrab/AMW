/***********************************************************************************************************************
 Communication Utils
***********************************************************************************************************************/

messageID(0).

@new_mID[atomic]
+!new_ID(OT)
    <-  -messageID(N);
        .concat(mid, N+1, O);
        .term2string(OT, O);
        +messageID(N+1).

+!random_agent(Service, Type, Provider)
	<-  .df_search(Service, Type, Providers);                       // search the agents
        .shuffle(Providers, Providers2);                            // shuffle the obtained list
        .member(Provider, Providers2).                              // get the first agent in the list

+!ensure_send(description(Service, Type), Performative, Message)
    <-  !random_agent(Service, Type, Receiver);                     // find an agent that can satisfy the request
        !ensure_send(Receiver, Performative, Message).

+!ensure_send(Receiver, Performative, Message)
    <-  !new_ID(MID);                                               // generate a new message id
        !ensure_send(Receiver, Performative, Message, MID).

+!ensure_send(Receiver, Performative, Message, MID)
    <-  .at("now +2 seconds", {                                     // schedule a timeout event
            +timeout(Receiver, Performative, Message, MID)          // save message data
        });
        +waiting(MID);                                              // save waiting knowledge
        .send(Receiver, Performative, Message[mid(MID)]).           // send the message

+timeout(Receiver, Performative, Message, MID)
    :   waiting(MID)                                                // timeout elapsed and message hasn't yet a response
    <-  !ensure_send(Receiver, Performative, Message, MID).         // resend the message

-timeout(_, _, _, _).                                               // message got a response

+!response_received(MID) <- -waiting(MID).                          // remove message waiting flag
