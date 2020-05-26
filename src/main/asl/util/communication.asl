/***********************************************************************************************************************
 Communication Utils
***********************************************************************************************************************/

+!random_agent(Service, Type, Provider)
	<-  .df_search(Service, Type, Providers);                                   // search the robot agent(s)
        .random(Multiplier);                                                    // get a random number [ 0 - 1 ]
        .length(Providers, ProvidersNum);                                       // get number of providers
        AgentNum = math.round(Multiplier * (ProvidersNum -1));                  // cal the agent num in the list
        .nth(AgentNum, Providers, Provider).                                    // get the agent