/***********************************************************************************************************************
 Communication Utils
***********************************************************************************************************************/

messageID(0).

@new_mID[atomic] +!new_ID(OT) <- -messageID(N); .concat(mid, N+1, O); .term2string(OT, O); +messageID(N+1).

+!random_agent(Service, Type, Provider)
	<-  .df_search(Service, Type, Providers);                       // search the agents
        .shuffle(Providers, Providers2);                            // shuffle the obtained list
        .member(Provider, Providers2).                              // get the first agent in the list

+!ensure_send(description(Service, Type), Performative, Message, MID)
    <-  !random_agent(Service, Type, Receiver);                     // get an agent that can satisfy the request
        !ensure_send(Receiver, Performative, Message, MID).

+!ensure_send(Receiver, Performative, Message, MID1)
    <-  !new_ID(MID2);                                              // get a new, unique message id (one is often order)
        .concat(MID1, MID2, MID);                                   // generate a message id as the combination of the 2
        .term2string(TMID, MID);                                    // make string an atom
        !ensure_send(Receiver, Performative, Message, TMID, unique).

+!ensure_send(Receiver, Performative, Message, MID, unique)
    <-  .at("now +2 seconds", {                                     // schedule a timeout event
            +timeout(Receiver, Performative, Message, MID)          // save message data
        });
        +message(Receiver, Performative, Message, MID);             // save message data
        .send(Receiver, Performative, Message, MID).                // send the message

+timeout(Receiver, Performative, Message, MID)
    :   message(Receiver, Performative, Message, MID)               // timeout elapsed and message hasn't yet a response
    <-  !ensure_send(Receiver, Performative, Message, MID, unique). // resend the message

-timeout(_, _, _, _).                                               // message got a response

+!response_received(MID) <- -message(_, _, _, MID).                 // remove message waiting flag

+!response_received(TMID, TOID)
    <-  .term2string(TMID, MID);                                    // make atom a string
        .reverse(MID, RMID);                                        // uniqueness of id is at the end: 'xxxmidyyy'
        .nth(I, RMID, "m");                                         // find last 'mid' string: 'yyydimxxx'
        .length(MID, L);                                            // find mid length
        .delete(0, I+1, RMID, OMID);                                // remove uniqueness from mid: 'yyydim' -> 'xxx'
        .reverse(OMID, OID);                                        // reverse again the message id
        .term2string(TOID, OID);                                    // make atom a string
        -message(_, _, _, MID).                                     // remove message waiting flag
