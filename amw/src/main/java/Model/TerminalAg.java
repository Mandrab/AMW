package Model;

import Controller.Mediator;
import jade.core.Agent;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;
import jason.asSyntax.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;

import static Controller.Mediator.CommunicationOntology.*;
import static Model.utils.ServiceType.ACCEPT_ORDER;
import static Model.utils.ServiceType.MANAGEMENT_ORDERS;

public class TerminalAg extends Agent implements ModelAgent {

	private Mediator mediator;

	// start the jade agent
	public static void startAg( Mediator mediator ) {
		try {

			Runtime rt = Runtime.instance(  );                                      // get a hold on JADE runtime

			Profile p = new ProfileImpl(  );                                        // create a default profile

			ContainerController cc = rt.createAgentContainer( p );                  // create a new non-main container

			AgentController agent =                                                 // Create a new agent
					cc.createNewAgent( "terminal-ag",
							TerminalAg.class.getCanonicalName( ),
							new Object[]{ mediator } );

			agent.start(  );                                                        // fire up the agent

		} catch ( StaleProxyException e ) {
			System.err.println( "TerminalAgent: exception in startAg" );
			e.printStackTrace( );
		}
	};

	@Override
	protected void setup() {
		this.mediator = ( Mediator ) getArguments()[ 0 ];
		mediator.setAgent( this );
	}

	// action of send a message TODO -> makeOrder
	private void sendAction( String input ) {
		DFAgentDescription template = new DFAgentDescription();                     // create a "service provider" template
		ServiceDescription sd = new ServiceDescription();
		sd.setName( MANAGEMENT_ORDERS.toString() );
		sd.setType( ACCEPT_ORDER.toString() );
		template.addServices( sd );

		Executors.newCachedThreadPool().submit(() -> {
			DFAgentDescription[] result = new DFAgentDescription[ 0 ];         // an array containing all the agents that matches the template
			try {
				result = DFService.search(this, template);

				if ( result.length == 0 ) {
					mediator.commandView( ERROR, "Info", "Error, no device able to handle the command!" );
					return;
				}

				ACLMessage cfp = new ACLMessage( ACLMessage.CFP );                        // create a "call for propose" message
				for ( int i = 0; i < result.length; i++ )
					cfp.addReceiver( result[i].getName(  ) );                             // add message's receiver

				// TODO
				Literal literal = new LiteralImpl( new Atom( "order" ) );

				Map<String, String> clientDatas = new HashMap<>(  );
				clientDatas.put( "client", "TODO Paolo" );
				clientDatas.put( "address", "TODO Via XYZ" );
				literal.addTerm( new ObjectTermImpl( clientDatas ) );

				literal.addAnnots( Arrays.asList( new StringTermImpl( "Item 1" ), new StringTermImpl( "Item 2" ) ) );

				cfp.setContentObject( literal );

				//cfp.setContent( input );                                                 // the command

				cfp.setConversationId( getAID() + "command forward" );                  // conversation's id TODO need to be unique?
				cfp.setReplyWith( "cfp" + System.currentTimeMillis(  ) );                 // unique value (suggested practice: pag 19 -> https://jade.tilab.com/doc/tutorials/JADEProgramming-Tutorial-for-beginners.pdf)

				send( cfp );                                                          // send the cfp to all ability sellers

				ACLMessage msg = blockingReceive( 5000 );

				mediator.commandView( INPUT, msg.getSender().getName().replaceAll( "@(\n|.)*", "" ), msg.getContent(  ) );
			} catch ( FIPAException | IOException e ) {
				e.printStackTrace( );
			}
		});
	}

	@Override
	public void command ( Mediator.CommunicationOntology c, String... args ) {
		if ( c == END ) {
			takeDown(  );
		} else if ( c == SEND ) {
			sendAction( args[ 0 ] );
		}
	}
}
