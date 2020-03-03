package asl_actions;

import jason.asSyntax.*;
import org.junit.Test;
import utils.MailReader;

import javax.mail.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

public class TestActions {

	private static final String SBJ_200_CODE = "Order ready";
	private static final String SBJ_40x_CODE = "Order fail";
	private static final String MSG_404_CODE = "At least an item of your order has not been found";
	private static final String MSG_409_CODE = "Due to another buy, at least one item of your order is not present in sufficient quantity";

	/*@Test TODO
	public void testFuse( ) {
		fail( "To do" );
	}

	@Test TODO
	public void testLabelize( ) {
		fail( "To do" );
	}*/

	@Test
	public void testSendFeedback( ) throws Exception {
		BufferedReader reader = new BufferedReader( new FileReader( "testmaillogin" ) );

		String mail = reader.readLine( );
		String pass = reader.readLine( );

		Term[] terms = new Term[] { new StringTermImpl( mail ), new NumberTermImpl( 404 ) };
		new send_feedback( ).execute( null, null, terms );

		terms = new Term[] { new StringTermImpl( mail ), new NumberTermImpl( 409 ) };
		new send_feedback( ).execute( null, null, terms );

		ListTerm items = new ListTermImpl( );
		items.addAll( Arrays.asList( new StringTermImpl( "Item 0" ), new StringTermImpl( "Item 1" ), new StringTermImpl( "Item 2" ) ) );
		terms = new Term[] { new StringTermImpl( mail ), new NumberTermImpl( 200 ), new StringTermImpl( "ORDER ID" ), items };
		new send_feedback( ).execute( null, null, terms );

		List<Message> msgs = new LinkedList<>(  );
		Folder folder = MailReader.getMail( "imap.gmail.com", mail, pass, MailReader.GmailFolder.INBOX.getEng( ), msgs );

		msgs = msgs.stream( ).filter( msg -> {
			try {
				return msg.getFrom( )[ 0 ].toString( ).equals( mail );
			} catch ( MessagingException ignored ) { } return false;
		} ).collect( Collectors.toList( ) );
		msgs.forEach( msg -> {
			try {
				msg.setFlag( Flags.Flag.DELETED, true );
			} catch ( MessagingException ignored ) { }
		} );

		assertEquals( 1, msgs.stream( ).filter( msg -> {
			try {
				return msg.getSubject( ).equals( SBJ_200_CODE );
			} catch ( MessagingException ignored ) { }
			return false;
		} ).count( ) );
		assertEquals( 1, msgs.stream( ).filter( msg -> {
			try {
				System.out.println( msg.getContent( ) );
				return msg.getSubject( ).equals( SBJ_40x_CODE ) && ( (String) msg.getContent( ) ).contains( MSG_404_CODE );
			} catch ( MessagingException | IOException ignored ) { }
			return false;
		} ).count( ) );
		assertEquals( 1, msgs.stream( ).filter( msg -> {
			try {
				return msg.getSubject( ).equals( SBJ_40x_CODE ) && ( (String) msg.getContent( ) ).contains( MSG_409_CODE );
			} catch ( MessagingException | IOException ignored ) { }
			return false;
		} ).count( ) );

		folder.close( true );
	}
}
