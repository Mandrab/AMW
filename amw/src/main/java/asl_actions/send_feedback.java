package asl_actions;

import jason.NoValueException;
import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.*;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Properties;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

public class send_feedback extends DefaultInternalAction {

	private static final String ORDER_CONFIRM = "Order confirm";
	private static final String ORDER_READY = "Order ready";
	private static final String ORDER_FAIL = "Order fail";
	private static final String ITEM_NOT_FOUND = "At least an item of your order has not been found";
	private static final String ITEM_RESERVED = "Due to another buy, at least one item of your order is not present in sufficient quantity";
	private static final BiFunction<String, List<String>, String> ORDER_INFO = ( orderId, items ) -> "Your order with id: " + orderId + " including the following items:\n\n"
			+ String.join( "\n", items ) + "\n\n has been confirmed";

	@Override
	public Object execute( TransitionSystem ts, Unifier un, Term[] args) throws NoValueException, IOException {

		String mailTo = args[ 0 ].toString( );
		String mailSubject, mailMsg;
		switch ( ( int ) ( ( NumberTerm ) args[ 1 ] ).solve( ) ) {
			case 200:
				mailSubject = ORDER_READY;
				mailMsg = ORDER_INFO.apply( args[ 2 ].toString( ), ( ( ListTerm ) args[ 3 ] ).stream( )
						.map( Term::toString ).collect( Collectors.toList( ) ) );
				break;
			case 202:
				mailSubject = ORDER_CONFIRM;
				mailMsg = ORDER_INFO.apply( args[ 2 ].toString( ), ( ( ListTerm ) args[ 3 ] ).stream( )
						.map( Term::toString ).collect( Collectors.toList( ) ) );
				break;
			case 404:
				mailSubject = ORDER_FAIL;
				mailMsg = ITEM_NOT_FOUND;
				break;
			case 409:
				mailSubject = ORDER_FAIL;
				mailMsg = ITEM_RESERVED;
				break;
			default:
				mailSubject = "";
				mailMsg = "";
		}

		BufferedReader reader = new BufferedReader( new FileReader( "testmaillogin" ) );

		Properties properties = new Properties();

		// Setup mail server
		properties.setProperty( "mail.host", "smtp.gmail.com" );
		properties.setProperty( "mail.smtp.port", "587" );
		properties.setProperty( "mail.smtp.auth", "true" );
		properties.setProperty( "mail.smtp.starttls.enable", "true" );

		String mailFrom = reader.readLine( );
		String pass = reader.readLine( );

		Session session = Session.getInstance( properties, new javax.mail.Authenticator( ) {
			protected PasswordAuthentication getPasswordAuthentication( ) {
				return new PasswordAuthentication( mailFrom, pass );
			}
		} );

		try {
			// Create a default MimeMessage object.
			MimeMessage message = new MimeMessage( session );

			// Set From: header field of the header.
			message.setFrom( new InternetAddress( mailFrom ) );

			// Set To: header field of the header.
			message.addRecipient( Message.RecipientType.TO, new InternetAddress( mailTo ) );

			// Set Subject: header field
			message.setSubject( mailSubject );

			// Now set the actual message
			message.setText( mailMsg );

			// Send message
			Transport.send( message );
		} catch ( MessagingException mex ) {
			mex.printStackTrace( );
			return false;
		}

		return true;
	}

}
