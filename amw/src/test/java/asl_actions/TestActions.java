package asl_actions;

import jason.NoValueException;
import jason.asSyntax.*;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;

import static org.junit.Assert.*;

public class TestActions {

	private static final String FAKE_MAIL = "fakemailgenerator@teleworm.us";

	/*@Test TODO
	public void testFuse( ) {
		fail( "To do" );
	}*/

	@Test
	public void testSendFeedback( ) throws NoValueException, IOException {
		Term[] terms = new Term[] { new StringTermImpl( FAKE_MAIL ), new NumberTermImpl( 404 ) };
		assertNull( new send_feedback( ).execute( null, null, terms ) );

		terms = new Term[] { new StringTermImpl( FAKE_MAIL ), new NumberTermImpl( 409 ) };
		assertNull( new send_feedback( ).execute( null, null, terms ) );

		ListTerm items = new ListTermImpl( );
		items.addAll( Arrays.asList( new StringTermImpl( "Item 0" ), new StringTermImpl( "Item 1" ), new StringTermImpl( "Item 2" ) ) );
		terms = new Term[] { new StringTermImpl( FAKE_MAIL ), new NumberTermImpl( 200 ), new StringTermImpl( "ORDER ID" ), items };
		assertNull( new send_feedback( ).execute( null, null, terms ) );
	}
}
