package model.agents;

import interpackage.RequestDispatcher;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.io.IOException;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;

public abstract class TerminalAgentImpl extends Agent implements TerminalAgent {

	private static final int RESPONSE_TIME = 50000;

	private List<MessageTemplate> outdatedMsgTemplate;

	protected RequestDispatcher dispatcher;

	@Override
	protected void setup( ) {
		dispatcher = ( RequestDispatcher ) getArguments( )[ 0 ];
		dispatcher.register( this );

		outdatedMsgTemplate = new LinkedList<>(  );
		addBehaviour( collectOutdatedResponses( ) );
	}

	protected Behaviour collectOutdatedResponses( ) {
		return new OneShotBehaviour( ) {
			@Override
			public void action ( ) {
				if ( outdatedMsgTemplate.size( ) > 0 ) receive( outdatedMsgTemplate.remove( 0 ) );                             // TODO nel caso di molti messaggi non blocca per troppo tempo l'esecuzione
				block( );
			}
		};
	}

	@Override
	public abstract <T> T askFor ( Request request, String... args );

	public class MessageSender {

		private boolean askToAll = false;
		private ACLMessage message;
		private String msgID;
		private DFAgentDescription receiverTemplate;
		private long timeout = RESPONSE_TIME;                   // -1 = disabled

		public MessageSender( ) { }

		public MessageSender( DFAgentDescription template, int performative, Serializable content ) {
			setReceiver( template );
			setMessage( performative, content );
		}

		public MessageSender( String serviceName, String serviceType, int performative, Serializable content ) {
			setReceiver( serviceName, serviceType );
			setMessage( performative, content );
		}

		public MessageSender askToAll( boolean active ) {
			askToAll = active;
			return this;
		}

		public MessageSender setMessage( int performative, Serializable content ) {
			message = new ACLMessage( performative );          // create a "call for propose" message
			try {
				if ( content instanceof String ) message.setContent( ( String ) content );
				else message.setContentObject( content );
			} catch ( IOException e ) {
				e.printStackTrace( );
			}
			return this;
		}

		public MessageSender setMsgID( String id ) {
			msgID = id;System.out.println( msgID);
			return this;
		}

		public MessageSender setReceiver( String serviceName, String serviceType ) {
			receiverTemplate = new DFAgentDescription( );            // create a "service provider" template
			ServiceDescription sd = new ServiceDescription( );
			sd.setName( serviceName );
			sd.setType( serviceType );
			receiverTemplate.addServices( sd );
			return this;
		}

		public MessageSender setReceiver ( DFAgentDescription template ) {
			receiverTemplate = template;
			return this;
		}

		public MessageSender setTimeout( long duration ) {
			timeout = duration;
			return this;
		}

		public void send( Agent sender ) {
			if ( receiverTemplate == null ) throw new IllegalStateException( );

			Executors.newCachedThreadPool( ).submit( ( ) -> {
				try {
					DFAgentDescription[] result = DFService.search( sender, receiverTemplate ); // an array containing all the agents that matches the template

					if ( result.length == 0 ) return;

					for ( int i = 0; ( i == 0 || askToAll ) && i < result.length; i++ )
						message.addReceiver( result[i].getName( ) );                    // add message's receiver

					if ( msgID != null ) message.setReplyWith( msgID );

					sender.send( message );                                                 // send the cfp to all ability sellers
				} catch ( FIPAException e ) {
					e.printStackTrace( );
				}
			} );
		}

		public CompletableFuture<ACLMessage> require( Agent sender ) {
			CompletableFuture<ACLMessage> returnValue = new CompletableFuture<>( );

			String msgId = java.time.LocalDateTime.now( ).toString( ) + String.format( "%.10f", Math.random( ) );
			message.setReplyWith( msgId );

			send( sender );

			Executors.newCachedThreadPool( ).submit( ( ) -> {
				ACLMessage s = timeout != -1 ? blockingReceive( MessageTemplate.MatchInReplyTo( msgId ), RESPONSE_TIME )
						: blockingReceive( MessageTemplate.MatchInReplyTo( msgId ) );

				if ( s == null ) outdatedMsgTemplate.add( MessageTemplate.MatchInReplyTo( msgId ) ); // collect delayed message and cancel it

				returnValue.complete( s );
			} );

			return returnValue;
		}
	}
}
