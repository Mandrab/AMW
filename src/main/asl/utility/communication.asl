/***********************************************************************************************************************
 Communication Utils
***********************************************************************************************************************/

+!random_agent(Service, Type, Provider)
	<-  .df_search(Service, Type, Providers);                       // search the agents
        .shuffle(Providers, Providers2);                            // shuffle the obtained list
        .member(Provider, Providers2).                              // get the first agent in the list
